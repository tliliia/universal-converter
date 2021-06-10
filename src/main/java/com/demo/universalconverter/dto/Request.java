package com.demo.universalconverter.dto;

public class Request {
    private String from;
    private String to;

    public Request(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String fromValue) {
        this.from = fromValue;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String toValue) {
        this.to = toValue;
    }
}
