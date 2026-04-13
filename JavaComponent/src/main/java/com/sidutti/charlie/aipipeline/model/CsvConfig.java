package com.sidutti.charlie.aipipeline.model;

import java.util.List;

public class CsvConfig extends DataSourceConfig {
    private String filePath;
    private String delimiter;
    private String quoteCharacter;
    private String escapeCharacter;
    private boolean hasHeader;
    private List<String> columnMappings;
    private String encoding;
    private int skipLines;
    private int maxRecords;
    private String dateFormat;
    private List<String> requiredColumns;

    public CsvConfig() {
        super();
    }

    @Override
    public String getType() {
        return "csv";
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getQuoteCharacter() {
        return quoteCharacter;
    }

    public void setQuoteCharacter(String quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    public String getEscapeCharacter() {
        return escapeCharacter;
    }

    public void setEscapeCharacter(String escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public List<String> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(List<String> columnMappings) {
        this.columnMappings = columnMappings;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getSkipLines() {
        return skipLines;
    }

    public void setSkipLines(int skipLines) {
        this.skipLines = skipLines;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public List<String> getRequiredColumns() {
        return requiredColumns;
    }

    public void setRequiredColumns(List<String> requiredColumns) {
        this.requiredColumns = requiredColumns;
    }
}