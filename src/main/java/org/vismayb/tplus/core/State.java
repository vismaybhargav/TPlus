package org.vismayb.tplus.core;

import org.apache.commons.csv.CSVRecord;

import java.io.File;

public class State {
    private static State instance;

    private State() {
        // Private constructor to prevent instantiation
    }

    public static State getInstance() {
        if (instance == null) {
            instance = new State();
        }
        return instance;
    }

    public File file = new File("D:\\Downloads\\FL0.CSV");
    public String currentFilePath = "";
    public Iterable<CSVRecord> logRecords = null;
}
