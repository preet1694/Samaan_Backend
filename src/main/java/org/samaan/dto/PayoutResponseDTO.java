package org.samaan.dto;
import org.json.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayoutResponseDTO {
    private String id;
    private String status;
    private int amount;
    private String mode;
    private String fundAccountId;

    public PayoutResponseDTO(JSONObject json) {
        this.id = json.getString("id");
        this.status = json.optString("status");
        this.amount = json.optInt("amount");
        this.mode = json.optString("mode");
        this.fundAccountId = json.getJSONObject("fund_account").getString("id");
    }

    // Getters
}
