package com.example.busmap.FindRouteHelper;

import android.text.SpannableString;

public class instruction {
    private SpannableString step;
    private String distance;
    private String timeEstimate;

    public instruction(SpannableString step, String distance, String timeEstimate) {
        this.step = step;
        this.distance = distance;
        this.timeEstimate = timeEstimate;
    }

    public SpannableString getStep() {
        return step;
    }

    public void setStep(SpannableString step) {
        this.step = step;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTimeEstimate() {
        return timeEstimate;
    }

    public void setTimeEstimate(String timeEstimate) {
        this.timeEstimate = timeEstimate;
    }
}
