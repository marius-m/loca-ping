package lt.markmerkk.locaping.entities

enum class LocationSource {
    UNKNOWN,
    FOREGROUND_SERVICE,
    PUSH_NOTIFICATION_WORKER,
    ;

    companion object {
        fun fromRaw(rawSource: String): LocationSource {
            return values()
                .firstOrNull { it.name.equals(rawSource, ignoreCase = true) } ?: UNKNOWN
        }
    }
}