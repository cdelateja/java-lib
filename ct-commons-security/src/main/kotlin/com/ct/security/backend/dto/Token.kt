package com.ct.security.backend.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

@JsonIgnoreProperties(ignoreUnknown = true)
class Token(@JsonProperty("access_token")
            val accessToken: String,
            @JsonProperty("token_type")
            var tokenType: String,
            @JsonProperty("refresh_token")
            var refreshToken: String?,
            @JsonProperty("expires_in")
            var expiresIn: Number,
            @JsonProperty("scope")
            var scope: String,
            @JsonProperty("compania")
            var company: String,
            @JsonProperty("jti")
            var jti: String) {

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE)
    }
}
