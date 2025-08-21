package de.ciupka.jeopardy.game.questions.reveal;

public class Step {

    private StepType type;
    private Object content;
    private boolean revealed;

    public Step(StepType type, Object content) {
        this.type = type;
        this.content = content;
    }

    public StepType getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public static class VideoData {
        private String video;
        private double blurDuration;

        public VideoData(String video, double blurDuration) {
            this.video = video;
            this.blurDuration = blurDuration;
        }

        public String getVideo() {
            return video;
        }

        public double getBlurDuration() {
            return blurDuration;
        }

    }

}
