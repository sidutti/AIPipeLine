package com.sidutti.charlie.aipipeline.dto;

import java.util.List;

public record Section(List<Element> elements, String sectionOrder, int sectionNumber) {
}
