package com.sidutti.charlie.aipipeline.dto.tutor;

public class HintRequest {
    private String questionId;
    private String sessionId;

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}