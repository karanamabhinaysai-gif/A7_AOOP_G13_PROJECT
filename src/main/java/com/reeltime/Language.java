package com.reeltime;

public enum Language {
    TELUGU("Telugu"), TAMIL("Tamil"), HOLLYWOOD("Hollywood");
    private final String label;
    Language(String label) { this.label = label; }
    public String label() { return label; }
    @Override public String toString() { return label; }
}
