package sample;

public class Activity {
    int time;
    String activity;
    String description;

    public Activity(int time, String activity, String description) {
        this.time = time;
        this.activity = activity;
        this.description = description;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
