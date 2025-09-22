package com.sidutti.charlie.aipipeline.dto;

import org.springframework.ai.document.Document;

import java.util.List;

public record TextAndDoc(TargetDocument doc, List<Document> docs) {
}
