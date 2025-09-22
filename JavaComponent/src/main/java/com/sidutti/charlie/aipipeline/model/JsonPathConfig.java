package com.sidutti.charlie.aipipeline.model;

import java.util.Map;

public class JsonPathConfig {
    private String corpusTextPath;
    private Map<String, String> metadataPaths;
    private String idPath;
    private String timestampPath;
    private String timestampFormat;
    private boolean flattenArrays;

    public JsonPathConfig() {}

    public String getCorpusTextPath() { return corpusTextPath; }
    public void setCorpusTextPath(String corpusTextPath) { this.corpusTextPath = corpusTextPath; }

    public Map<String, String> getMetadataPaths() { return metadataPaths; }
    public void setMetadataPaths(Map<String, String> metadataPaths) { this.metadataPaths = metadataPaths; }

    public String getIdPath() { return idPath; }
    public void setIdPath(String idPath) { this.idPath = idPath; }

    public String getTimestampPath() { return timestampPath; }
    public void setTimestampPath(String timestampPath) { this.timestampPath = timestampPath; }

    public String getTimestampFormat() { return timestampFormat; }
    public void setTimestampFormat(String timestampFormat) { this.timestampFormat = timestampFormat; }

    public boolean isFlattenArrays() { return flattenArrays; }
    public void setFlattenArrays(boolean flattenArrays) { this.flattenArrays = flattenArrays; }

    @Override
    public String toString() {
        return "JsonPathConfig{" +
                "corpusTextPath='" + corpusTextPath + '\'' +
                ", metadataPaths=" + metadataPaths +
                ", idPath='" + idPath + '\'' +
                ", timestampPath='" + timestampPath + '\'' +
                ", timestampFormat='" + timestampFormat + '\'' +
                ", flattenArrays=" + flattenArrays +
                '}';
    }
}