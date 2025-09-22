package com.siduuti.aipipeline.dto;

import java.util.List;
import java.util.Map;

public record Page(List<Element> elements, List<Section> sections, Map<String, String> formFields, int pageNumber,
                   String pageTitle) {
}
