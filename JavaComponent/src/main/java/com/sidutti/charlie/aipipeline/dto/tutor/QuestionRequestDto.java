package com.sidutti.charlie.aipipeline.dto.tutor;

import java.util.List;

public class QuestionRequestDto {
    private String sessionId;
    private ChatSession.Subject subject;
    private int grade;
    private Integer difficulty;
    private List<String> previousQuestions;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ChatSession.Subject getSubject() {
        return subject;
    }

    public void setSubject(ChatSession.Subject subject) {
        this.subject = subject;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getPreviousQuestions() {
        return previousQuestions;
    }

    public void setPreviousQuestions(List<String> previousQuestions) {
        this.previousQuestions = previousQuestions;
    }
}