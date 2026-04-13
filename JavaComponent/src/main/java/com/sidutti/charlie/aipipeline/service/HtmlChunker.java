package com.sidutti.charlie.aipipeline.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HtmlChunker {

    public static class Chunk {
        private final String content;
        private final String heading;
        private final int level;
        private final int size;

        public Chunk(String content, String heading, int level) {
            this.content = content;
            this.heading = heading;
            this.level = level;
            this.size = content.length();
        }

        public String getContent() {
            return content;
        }

        public String getHeading() {
            return heading;
        }

        public int getLevel() {
            return level;
        }

        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return String.format("Chunk{heading='%s', level=%d, size=%d}", heading, level, size);
        }
    }

    public List<Chunk> chunkByHeadings(String html) {
        Document doc = Jsoup.parse(html);
        List<Chunk> chunks = new ArrayList<>();

        Elements h1Elements = doc.select("h1");

        if (h1Elements.isEmpty()) {
            chunks.add(new Chunk(doc.text(), "Root", 0));
            return chunks;
        }

        for (int i = 0; i < h1Elements.size(); i++) {
            Element h1 = h1Elements.get(i);
            Element nextH1 = (i + 1 < h1Elements.size()) ? h1Elements.get(i + 1) : null;

            List<Element> h1Section = getElementsInH1Section(h1, nextH1);
            chunks.addAll(createChunksForH1Section(h1, h1Section));
        }

        return chunks;
    }

    private List<Element> getElementsInH1Section(Element h1, Element nextH1) {
        List<Element> elements = new ArrayList<>();
        Element current = h1;

        while (current != null && (!current.equals(nextH1))) {
            elements.add(current);
            current = current.nextElementSibling();
        }

        return elements;
    }

    private List<Chunk> createChunksForH1Section(Element h1, List<Element> sectionElements) {
        List<Chunk> chunks = new ArrayList<>();
        String h1Text = h1.text();

        StringBuilder h1Content = new StringBuilder();
        h1Content.append(h1Text).append("\n");

        List<Element> subHeadings = new ArrayList<>();

        for (Element element : sectionElements) {
            if (isSubHeading(element)) {
                subHeadings.add(element);
            } else if (subHeadings.isEmpty()) {
                h1Content.append(element.text()).append("\n");
            }
        }

        if (h1Content.toString().trim().length() > h1Text.length()) {
            chunks.add(new Chunk(h1Content.toString().trim(), h1Text, 1));
        }

        for (int i = 0; i < subHeadings.size(); i++) {
            Element subHeading = subHeadings.get(i);
            Element nextSubHeading = (i + 1 < subHeadings.size()) ? subHeadings.get(i + 1) : null;

            StringBuilder subContent = new StringBuilder();
            subContent.append(subHeading.text()).append("\n");

            Element current = subHeading.nextElementSibling();
            while (current != null && (!current.equals(nextSubHeading))) {
                if (!isSubHeading(current)) {
                    subContent.append(current.text()).append("\n");
                }
                current = current.nextElementSibling();
            }

            String fullHeading = h1Text + " > " + subHeading.text();
            chunks.add(new Chunk(subContent.toString().trim(), fullHeading, getHeadingLevel(subHeading)));
        }

        return chunks;
    }

    private boolean isSubHeading(Element element) {
        return element.tagName().matches("h[2-6]");
    }

    public List<Chunk> chunkBySize(String html, int maxSize) {
        Document doc = Jsoup.parse(html);
        String text = doc.text();
        List<Chunk> chunks = new ArrayList<>();

        if (text.length() <= maxSize) {
            chunks.add(new Chunk(text, "Single Chunk", 0));
            return chunks;
        }

        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentChunk = new StringBuilder();
        int chunkNumber = 1;

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() + 1 > maxSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(new Chunk(currentChunk.toString().trim(),
                            "Chunk " + chunkNumber++, 0));
                    currentChunk = new StringBuilder();
                }
            }

            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);
        }

        if (currentChunk.length() > 0) {
            chunks.add(new Chunk(currentChunk.toString().trim(),
                    "Chunk " + chunkNumber, 0));
        }

        return chunks;
    }

    public List<Chunk> chunkByElement(String html, String elementSelector) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select(elementSelector);
        List<Chunk> chunks = new ArrayList<>();

        int index = 1;
        for (Element element : elements) {
            String content = element.text();
            if (!content.trim().isEmpty()) {
                chunks.add(new Chunk(content,
                        elementSelector + " " + index++, 0));
            }
        }

        return chunks;
    }

    public List<Chunk> chunkHierarchically(String html) {
        Document doc = Jsoup.parse(html);
        List<Chunk> chunks = new ArrayList<>();

        processElementHierarchy(doc.body(), chunks, "", 0);

        return chunks;
    }

    private void processElementHierarchy(Element element, List<Chunk> chunks, String parentHeading, int level) {
        if (element == null) return;

        for (Element child : element.children()) {
            if (isHeading(child)) {
                String headingText = child.text();
                String content = extractContentUnderHeading(child);

                if (!content.trim().isEmpty()) {
                    chunks.add(new Chunk(content, headingText, getHeadingLevel(child)));
                }

                processElementHierarchy(child, chunks, headingText, level + 1);
            } else {
                processElementHierarchy(child, chunks, parentHeading, level);
            }
        }
    }

    private String extractContentUnderHeading(Element heading) {
        StringBuilder content = new StringBuilder();
        content.append(heading.text()).append("\n");

        Element sibling = heading.nextElementSibling();
        int headingLevel = getHeadingLevel(heading);

        while (sibling != null) {
            if (isHeading(sibling) && getHeadingLevel(sibling) <= headingLevel) {
                break;
            }

            if (!isHeading(sibling) || getHeadingLevel(sibling) > headingLevel) {
                content.append(sibling.text()).append("\n");
            }

            sibling = sibling.nextElementSibling();
        }

        return content.toString().trim();
    }

    private boolean isHeading(Element element) {
        return element.tagName().matches("h[1-6]");
    }

    private int getHeadingLevel(Element heading) {
        return Integer.parseInt(heading.tagName().substring(1));
    }
}