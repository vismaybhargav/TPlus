package org.vismayb.tplus.core;

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

    public boolean file = false;
    public String currentFilePath = "";
}
