package com.sidutti.charlie.aipipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidutti.charlie.aipipeline.dto.tutor.AnswerRequestDto;
import com.sidutti.charlie.aipipeline.dto.tutor.AnswerResponseDto;
import com.sidutti.charlie.aipipeline.dto.tutor.ChatMessage;
import com.sidutti.charlie.aipipeline.dto.tutor.ChatSession;
import com.sidutti.charlie.aipipeline.dto.tutor.ChatSessionDto;
import com.sidutti.charlie.aipipeline.dto.tutor.QuestionRequestDto;
import com.sidutti.charlie.aipipeline.dto.tutor.QuestionResponseDto;
import com.sidutti.charlie.aipipeline.dto.tutor.SessionSummaryDto;
import com.sidutti.charlie.aipipeline.dto.tutor.repository.ChatSessionRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TutorService {

    private final ChatSessionRepository sessionRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public TutorService(ChatSessionRepository sessionRepository,
            ChatModel chatModel,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    public Mono<ChatSessionDto> createSession(String studentName, ChatSession.Subject subject, int grade) {
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(
                sessionId,
                studentName,
                subject,
                grade,
                LocalDateTime.now(),
                null,
                new ArrayList<>(),
                null,
                null,
                null,
                ChatSession.SessionStatus.active);

        return sessionRepository.save(session)
                .map(this::toDto);
    }

    public Mono<SessionSummaryDto> endSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    ChatSession endedSession = new ChatSession(
                            session.id(),
                            session.studentName(),
                            session.subject(),
                            session.grade(),
                            session.startTime(),
                            LocalDateTime.now(),
                            session.messages(),
                            session.score(),
                            session.totalQuestions(),
                            session.correctAnswers(),
                            ChatSession.SessionStatus.completed);

                    return sessionRepository.save(endedSession)
                            .map(this::toSessionSummary);
                });
    }

    public Mono<Boolean> pauseSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    ChatSession pausedSession = new ChatSession(
                            session.id(),
                            session.studentName(),
                            session.subject(),
                            session.grade(),
                            session.startTime(),
                            session.endTime(),
                            session.messages(),
                            session.score(),
                            session.totalQuestions(),
                            session.correctAnswers(),
                            ChatSession.SessionStatus.paused);
                    return sessionRepository.save(pausedSession);
                })
                .map(session -> true)
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> resumeSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    ChatSession resumedSession = new ChatSession(
                            session.id(),
                            session.studentName(),
                            session.subject(),
                            session.grade(),
                            session.startTime(),
                            session.endTime(),
                            session.messages(),
                            session.score(),
                            session.totalQuestions(),
                            session.correctAnswers(),
                            ChatSession.SessionStatus.active);
                    return sessionRepository.save(resumedSession);
                })
                .map(session -> true)
                .defaultIfEmpty(false);
    }

    public Mono<List<ChatSessionDto>> getSessionHistory(String studentName) {
        return sessionRepository.findByStudentNameOrderByStartTimeDesc(studentName)
                .map(this::toDto)
                .collectList();
    }

    public Mono<ChatSessionDto> getSessionDetails(String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(this::toDto);
    }

    public Mono<QuestionResponseDto> generateQuestion(QuestionRequestDto request) {
        String systemPrompt = createSystemPrompt(request.getSubject(), request.getGrade(), request.getDifficulty());
        String userPrompt = createQuestionPrompt(request.getPreviousQuestions());

        return Mono.fromCallable(() -> {
            String response = chatModel.call(systemPrompt + "\n\n" + userPrompt);
            String cleanedResponse = formatLLMResponse(response);
            return parseQuestionResponse(cleanedResponse, request.getSubject());
        });
    }

    public Mono<AnswerResponseDto> submitAnswer(AnswerRequestDto request) {
        String systemPrompt = "You are an educational tutor. Evaluate the student's answer and provide feedback.";
        String userPrompt = String.format(
                "Student answered: '%s' for question ID: %s. " +
                        "Provide feedback in JSON format with fields: isCorrect, correctAnswer, explanation, score, feedback, nextQuestionAvailable",
                request.getUserAnswer(), request.getQuestionId());

        return Mono.fromCallable(() -> {
            String response = chatModel.call(systemPrompt + "\n\n" + userPrompt);
            String cleanedResponse = formatLLMResponse(response);
            return parseAnswerResponse(cleanedResponse);
        });
    }

    public Mono<String> getHint(String questionId, String sessionId) {
        String systemPrompt = "You are a helpful tutor. Provide a hint for the given question without giving away the answer.";
        String userPrompt = String.format("Provide a hint for question ID: %s", questionId);

        return Mono.fromCallable(() -> {
            String response = chatModel.call(systemPrompt + "\n\n" + userPrompt);
            return formatLLMResponse(response);
        });
    }

    private String createSystemPrompt(ChatSession.Subject subject, int grade, Integer difficulty) {
        return String.format(
                "You are an expert %s tutor for grade %d students. " +
                        "Generate educational questions with appropriate difficulty level %s. " +
                        "Respond in JSON format with fields: question, questionId, questionType, options (if multiple choice), "
                        +
                        "correctAnswer, explanation, difficulty (1-10), topic",
                subject, grade, difficulty != null ? difficulty : "medium");
    }

    private String createQuestionPrompt(List<String> previousQuestions) {
        if (previousQuestions == null || previousQuestions.isEmpty()) {
            return "Generate a new question to assess the student's knowledge.";
        }
        return "Generate a new question that is different from these previous questions: " +
                String.join("; ", previousQuestions);
    }

    private String formatLLMResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return response;
        }

        // Remove common LLM prefixes and suffixes
        String cleaned = response.trim();

        // Remove markdown code blocks if present
        cleaned = cleaned.replaceAll("```json\\s*", "");
        cleaned = cleaned.replaceAll("```\\s*", "");

        // Remove common prefixes
        cleaned = cleaned.replaceAll("^(Here's|Here is|I'll|Let me|The answer is|Response:|Answer:)\\s*", "");

        // Clean up any trailing explanations after JSON
        if (cleaned.contains("{") && cleaned.contains("}")) {
            int firstBrace = cleaned.indexOf("{");
            int lastBrace = cleaned.lastIndexOf("}");
            if (firstBrace < lastBrace) {
                cleaned = cleaned.substring(firstBrace, lastBrace + 1);
            }
        }

        return cleaned.trim();
    }

    private QuestionResponseDto parseQuestionResponse(String response, ChatSession.Subject subject) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            String question = jsonNode.path("question").asText();
            String questionId = jsonNode.path("questionId").asText(UUID.randomUUID().toString());
            String questionTypeStr = jsonNode.path("questionType").asText("open_ended");

            ChatMessage.QuestionType questionType = parseQuestionType(questionTypeStr);

            List<String> options = new ArrayList<>();
            JsonNode optionsNode = jsonNode.path("options");
            if (optionsNode.isArray()) {
                optionsNode.forEach(option -> options.add(option.asText()));
            }

            String correctAnswer = jsonNode.path("correctAnswer").asText("");
            String explanation = jsonNode.path("explanation").asText("");
            int difficulty = jsonNode.path("difficulty").asInt(5);
            String topic = jsonNode.path("topic").asText(subject.toString());

            return new QuestionResponseDto(
                    question,
                    questionId,
                    questionType,
                    options.isEmpty() ? null : options,
                    correctAnswer,
                    explanation,
                    difficulty,
                    topic);

        } catch (JsonProcessingException e) {
            // Fallback: generate a simple question if JSON parsing fails
            return generateFallbackQuestion(subject);
        }
    }

    private AnswerResponseDto parseAnswerResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            boolean isCorrect = jsonNode.path("isCorrect").asBoolean(false);
            String correctAnswer = jsonNode.path("correctAnswer").asText("");
            String explanation = jsonNode.path("explanation").asText("No explanation provided");
            int score = jsonNode.path("score").asInt(isCorrect ? 10 : 0);
            String feedback = jsonNode.path("feedback").asText(isCorrect ? "Good job!" : "Try again");
            boolean nextQuestionAvailable = jsonNode.path("nextQuestionAvailable").asBoolean(true);

            return new AnswerResponseDto(
                    isCorrect,
                    correctAnswer,
                    explanation,
                    score,
                    feedback,
                    nextQuestionAvailable);

        } catch (JsonProcessingException e) {
            // Fallback response
            return new AnswerResponseDto(
                    false,
                    "Unable to parse answer",
                    "There was an issue processing your answer. Please try again.",
                    0,
                    "Please try again",
                    true);
        }
    }

    private ChatMessage.QuestionType parseQuestionType(String questionTypeStr) {
        return switch (questionTypeStr.toLowerCase().replace("-", "_")) {
            case "multiple_choice", "multiplechoice" -> ChatMessage.QuestionType.multiple_choice;
            case "open_ended", "openended" -> ChatMessage.QuestionType.open_ended;
            case "calculation" -> ChatMessage.QuestionType.calculation;
            default -> ChatMessage.QuestionType.open_ended;
        };
    }

    private QuestionResponseDto generateFallbackQuestion(ChatSession.Subject subject) {
        // Generate subject-specific fallback questions
        return switch (subject) {
            case math -> new QuestionResponseDto(
                    "What is 15 × 8?",
                    UUID.randomUUID().toString(),
                    ChatMessage.QuestionType.calculation,
                    null,
                    "120",
                    "Multiply 15 by 8 to get the answer",
                    4,
                    "Multiplication");
            case physics -> new QuestionResponseDto(
                    "What is the formula for calculating speed?",
                    UUID.randomUUID().toString(),
                    ChatMessage.QuestionType.multiple_choice,
                    Arrays.asList("v = d/t", "v = d × t", "v = t/d", "v = d + t"),
                    "v = d/t",
                    "Speed is distance divided by time",
                    3,
                    "Kinematics");
        };
    }

    private ChatSessionDto toDto(ChatSession session) {
        return new ChatSessionDto(
                session.id(),
                session.studentName(),
                session.subject(),
                session.grade(),
                session.startTime(),
                session.endTime(),
                session.messages(),
                session.score(),
                session.totalQuestions(),
                session.correctAnswers(),
                session.status());
    }

    private SessionSummaryDto toSessionSummary(ChatSession session) {
        return new SessionSummaryDto(
                session.id(),
                session.totalQuestions() != null ? session.totalQuestions() : 0,
                session.correctAnswers() != null ? session.correctAnswers() : 0,
                session.score() != null ? session.score().intValue() : 0,
                (int) Duration.between(session.startTime(),
                        session.endTime() != null ? session.endTime() : LocalDateTime.now()).toMinutes(),
                Arrays.asList("Problem solving", "Mathematical reasoning"),
                Arrays.asList("Speed calculation", "Complex formulas"),
                Arrays.asList("Practice more word problems", "Review basic concepts"));
    }
}