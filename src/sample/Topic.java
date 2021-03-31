package sample;

import java.util.ArrayList;

public class Topic {
    String number;
    String topic;
    String subtopic;
    ArrayList<Question> questions = new ArrayList<Question>();

    public Topic(String number, String topic, String subtopic) {
        this.number = number;
        this.topic = topic;
        this.subtopic = subtopic;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getSubtopic() {
        return subtopic;
    }
    public void setSubtopic(String subtopic) {
        this.subtopic = subtopic;
    }

}
