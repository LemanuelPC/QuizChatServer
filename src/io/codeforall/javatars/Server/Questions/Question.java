package io.codeforall.javatars.Server.Questions;

public abstract class Question {
    protected String questionText;

    public Question(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionText() {
        return questionText;
    }

    public abstract boolean checkAnswer(String answer);
    public abstract String formatQuestion();
    public abstract String getAnswer();
}

