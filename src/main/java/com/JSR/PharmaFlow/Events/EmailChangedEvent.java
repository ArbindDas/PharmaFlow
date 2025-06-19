package com.JSR.PharmaFlow.Events;


import lombok.Getter;

@Getter
public class EmailChangedEvent {
    private final String oldEmail;
    private final String newEmail;

    public EmailChangedEvent(String oldEmail, String newEmail) {
        this.oldEmail = oldEmail;
        this.newEmail = newEmail;
    }

}