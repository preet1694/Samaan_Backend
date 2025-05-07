package org.samaan.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatRequest {
    private String senderEmail;
    private String carrierEmail;
}
