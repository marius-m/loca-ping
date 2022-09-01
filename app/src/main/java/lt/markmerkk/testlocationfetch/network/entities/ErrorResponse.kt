package lt.markmerkk.testlocationfetch.network.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    @JsonProperty("message") val message: String,
    @JsonProperty("code") val code: Int,
)
