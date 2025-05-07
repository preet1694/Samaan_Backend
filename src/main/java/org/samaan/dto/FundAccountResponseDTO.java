package org.samaan.dto;
import lombok.Getter;
import lombok.Setter;

import org.json.JSONObject;
@Setter
@Getter
public class FundAccountResponseDTO {
    private String id;
    private String contactId;
    private String accountType;
    private String vpa;

    public FundAccountResponseDTO(JSONObject json) {
        this.id = json.getString("id");
        this.contactId = json.getJSONObject("contact").getString("id");
        this.accountType = json.optString("account_type");
        this.vpa = json.optJSONObject("vpa").optString("address");
    }

    // Getters
}

