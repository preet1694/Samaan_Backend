package org.samaan.dto;
import org.samaan.dto.PayoutRequestDTO;
import lombok.Getter;
import lombok.Setter;

import org.json.JSONObject;
@Getter
@Setter
public class ContactResponseDTO {
    private String id;
    private String name;
    private String email;
    private String type;

    public ContactResponseDTO(JSONObject json) {
        this.id = json.getString("id");
        this.name = json.optString("name");
        this.email = json.optString("email");
        this.type = json.optString("type");
    }

}

