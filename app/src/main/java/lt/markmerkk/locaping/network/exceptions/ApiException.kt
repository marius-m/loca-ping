package lt.markmerkk.locaping.network.exceptions

class ApiException : Exception {

    override val message: String
        get() = super.message!!

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
