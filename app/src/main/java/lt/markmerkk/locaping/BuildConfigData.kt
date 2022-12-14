package lt.markmerkk.locaping

data class BuildConfigData(
    val applicationId: String,
    val isDebug: Boolean,
    val baseUrl: String,
    val versionName: String,
    val versionCode: Int,
)
