package com.doordeck.simplegpio.gpio.event;

public class BasicPollResult implements PollResult {

    private final byte[] data;

    public BasicPollResult(byte... data) {
        this.data = data;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }
}
