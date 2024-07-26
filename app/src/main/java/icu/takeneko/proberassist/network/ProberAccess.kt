package icu.takeneko.proberassist.network

import android.content.Context
import icu.takeneko.proberassist.util.PreferencesStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ProberAccess {
    val LOGIN_URL = "https://www.diving-fish.com/api/maimaidxprober/login"
    var loginStatus = false
    lateinit var username: String
    lateinit var password: String
    private lateinit var jwtToken: Cookie
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                prettyPrint = false
                ignoreUnknownKeys = true
            })
        }
    }

    fun load(context: Context) {
        PreferencesStorage.withContext(context).use {
            username = it.getString("Username", "")
            password = it.getString("Password", "")
        }
    }

    fun updateAccount(username: String, password: String, context: Context) {
        ProberAccess.username = username
        ProberAccess.password = password
        PreferencesStorage.withContext(context).use {
            it.putString("Username", username)
            it.putString("Password", password)
        }
    }

    suspend fun login(): Boolean {
        val loginResp = client.post(LOGIN_URL) {
            contentType(ContentType.Application.Json)
            setBody(LoginData(username, password))
        }
        if (loginResp.status != HttpStatusCode.OK) {
            return false
        }
        val cookies = loginResp.setCookie()
        cookies.forEach {
            println(it.name to it.value)
        }
        jwtToken = cookies.find { it.name == "jwt_token" } ?: return false
        loginStatus = true
        return true
    }


}