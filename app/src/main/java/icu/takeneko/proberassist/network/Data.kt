package icu.takeneko.proberassist.network

import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.renderCookieHeader
import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val username:String,
    val password:String
)

fun HttpMessageBuilder.cookie(
    cookie: Cookie
): Unit { // ktlint-disable no-unit-return
    val renderedCookie = cookie.let(::renderCookieHeader)

    if (HttpHeaders.Cookie !in headers) {
        headers.append(HttpHeaders.Cookie, renderedCookie)
        return
    }
    // Client cookies are stored in a single header "Cookies" and multiple values are separated with ";"
    headers[HttpHeaders.Cookie] = headers[HttpHeaders.Cookie] + "; " + renderedCookie
}