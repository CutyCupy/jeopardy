package de.ciupka.jeopardy.game.questions;

public class VideoQuestion extends AbstractQuestion {
    private String path;
    
    public VideoQuestion(String question, int points, String answer, String path) {
        super(question, points, answer, Type.VIDEO);
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

}
