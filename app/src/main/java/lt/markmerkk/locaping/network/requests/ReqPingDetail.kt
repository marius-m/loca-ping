package lt.markmerkk.locaping.network.requests

import com.fasterxml.jackson.annotation.JsonProperty
import org.joda.time.DateTime

data class ReqPingDetail(
    @JsonProperty("coordLat") val coordLat: Double,
    @JsonProperty("coordLong") val coordLong: Double,
    @JsonProperty("dtLastPing") val dtLastPing: String,
    @JsonProperty("dtCurrent") val dtCurrent: String,
    @JsonProperty("extras") val extras: String,
)
