package com.spbpu.schedule.RuzApi.models;

public class RuzApiException extends Exception {
    public RuzApiException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }
}
