package sample;

import java.util.Comparator;

public class Question{

    String paper;
    String questionNumber;
    int scollLocation;

    public Question(String paper, String questionNumber, int scollLocation) {
        this.paper = paper;
        this.questionNumber = questionNumber;
        this.scollLocation = scollLocation;
    }

    public String getPaper() {
        return paper;
    }

    public void setPaper(String paper) {
        this.paper = paper;
    }

    public String getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(String questionNumber) {
        this.questionNumber = questionNumber;
    }

    public int getScollLocation() {
        return scollLocation;
    }

    public void setScollLocation(int scollLocation) {
        this.scollLocation = scollLocation;
    }
    @Override
    public String toString() {
        return questionNumber + ". " + paper;
    }

}
