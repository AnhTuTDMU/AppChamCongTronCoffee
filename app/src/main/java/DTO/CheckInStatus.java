package DTO;

import java.util.HashMap;
import java.util.Map;

public class CheckInStatus {
    private boolean overallCheckIn;
    private Map<String, Boolean> userCheckIns;

    public CheckInStatus() {
        this.overallCheckIn = false;
        this.userCheckIns = new HashMap<>();
    }

    // Example getter and setter for overallCheckIn
    public boolean isOverallCheckIn() {
        return overallCheckIn;
    }

    public void setOverallCheckIn(boolean overallCheckIn) {
        this.overallCheckIn = overallCheckIn;
    }

    // Example getter and setter for userCheckIns
    public Map<String, Boolean> getUserCheckIns() {
        return userCheckIns;
    }

    public void setUserCheckIns(Map<String, Boolean> userCheckIns) {
        this.userCheckIns = userCheckIns;
    }
}
