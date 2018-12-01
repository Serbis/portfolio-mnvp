package ru.serbis.mnvp.tests;

import ru.serbis.mnvp.general.Node;

public class TestThread {
    private boolean alive = true;
    private Node node;
    private String logPath = "/home/serbis/tmp/";

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
