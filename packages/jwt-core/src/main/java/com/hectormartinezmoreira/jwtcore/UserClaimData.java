package com.hectormartinezmoreira.jwtcore;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class UserClaimData {
    private UUID idUser;
    private String email;
    private List<String> roles;

    // Builder pattern
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID idUser;
        private String email;
        private List<String> roles;

        public Builder idUser(UUID idUser){ this.idUser = idUser; return this; }
        public Builder email(String email){ this.email = email; return this; }
        public Builder roles(List<String> roles){ this.roles = roles; return this; }
        public UserClaimData build(){
            UserClaimData data = new UserClaimData();
            data.idUser = this.idUser;
            data.email = this.email;
            data.roles = this.roles;
            return data;
        }
    }
}
