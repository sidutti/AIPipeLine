package com.sidutti.charlie.aipipeline.controller;

import com.sidutti.charlie.aipipeline.dto.tutor.AnswerRequestDto;
import com.sidutti.charlie.aipipeline.dto.tutor.CreateSessionRequest;
import com.sidutti.charlie.aipipeline.dto.tutor.HintRequest;
import com.sidutti.charlie.aipipeline.dto.tutor.QuestionRequestDto;
import com.sidutti.charlie.aipipeline.dto.tutor.SessionActionRequest;
import com.sidutti.charlie.aipipeline.service.TutorService;
import com.siduuti.charlie.aipipeline.dto.tutor.EndSessionRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @Bean
    public RouterFunction<ServerResponse> tutorRoutes() {
        return RouterFunctions
                .route(GET("/api/tutor/health"), request ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("status", "ok")))

                .andRoute(POST("/api/tutor/session/create").and(accept(MediaType.APPLICATION_JSON)), request ->
                        request.bodyToMono(CreateSessionRequest.class)
                                .flatMap(req -> tutorService.createSession(req.getStudentName(), req.getSubject(), req.getGrade()))
                                .flatMap(session -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(session)))

                .andRoute(POST("/api/tutor/session/end").and(accept(MediaType.APPLICATION_JSON)), request ->
                        request.bodyToMono(EndSessionRequest.class)
                                .flatMap(req -> tutorService.endSession(req.getSessionId()))
                                .flatMap(summary -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(summary)))

                .andRoute(POST("/api/tutor/session/pause").and(accept(MediaType.APPLICATION_JSON)), request ->
                        request.bodyToMono(SessionActionRequest.class)
                                .flatMap(req -> tutorService.pauseSession(req.getSessionId()))
                                .flatMap(success -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of("success", success))))

                .andRoute(POST("/api/tutor/session/resume").and(accept(MediaType.APPLICATION_JSON)), request ->
                        request.bodyToMono(SessionActionRequest.class)
                                .flatMap(req -> tutorService.resumeSession(req.getSessionId()))
                                .flatMap(success -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of("success", success))))

                .andRoute(GET("/api/tutor/session/history/{studentName}"), request ->
                        tutorService.getSessionHistory(request.pathVariable("studentName"))
                                .flatMap(history -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(history)))

                .andRoute(GET("/api/tutor/session/{sessionId}"), request ->
                        tutorService.getSessionDetails(request.pathVariable("sessionId"))
                                .flatMap(session -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(session)))

                .andRoute(POST("/api/tutor/question/generate").and(accept(MediaType.APPLICATION_JSON)), request ->
                        request.bodyToMono(QuestionRequestDto.class)
                                .flatMap(tutorService::generateQuestion)
                                .flatMap(question -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(question)))

                .andRoute(POST("/api/tutor/answer/submit").and(accept(MediaType.APPLICATION_JSON)), request ->
                        request.bodyToMono(AnswerRequestDto.class)
                                .flatMap(tutorService::submitAnswer)
                                .flatMap(answer -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(answer)))

                .andRoute(POST("/api/tutor/question/hint").and(accept(MediaType.APPLICATION_JSON)), request ->
                        request.bodyToMono(HintRequest.class)
                                .flatMap(req -> tutorService.getHint(req.getQuestionId(), req.getSessionId()))
                                .flatMap(hint -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of("hint", hint))));
    }
}