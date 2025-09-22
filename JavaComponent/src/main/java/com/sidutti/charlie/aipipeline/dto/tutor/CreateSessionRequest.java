package com.sidutti.charlie.aipipeline.dto.tutor;

import java.time.LocalDateTime;

public class CreateSessionRequest {
    private String studentName;
    private ChatSession.Subject subject;
    private int grade;
    private LocalDateTime timestamp;

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public ChatSession.Subject getSubject() { return subject; }
    public void setSubject(ChatSession.Subject subject) { this.subject = subject; }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}