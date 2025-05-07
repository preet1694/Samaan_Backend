package org.samaan.dto;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class PayoutRequestDTO {
    private String name;
    private String email;
    private String upi;
    private int amount;
}
