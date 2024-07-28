package icu.takeneko.proberassist.network

import android.content.Context
import android.util.Log
import icu.takeneko.proberassist.App.Companion.TAG
import icu.takeneko.proberassist.App.Companion.application
import icu.takeneko.proberassist.R
import icu.takeneko.proberassist.util.PreferencesStorage
import icu.takeneko.proberassist.util.sendNotification
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ProberAccess {
    private const val LOGIN_URL = "https://www.diving-fish.com/api/maimaidxprober/login"
    val MAI_DIFFS = arrayOf("Basic", "Advanced", "Expert", "Master", "Re: MASTER")
    val CHUNI_DIFFS = arrayOf("Basic", "Advanced", "Expert", "Master", "Ultima", "World's End", "Best 10")
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

    private suspend fun parseMaiResult(content: String): String {
        val resp = client.post("http://www.diving-fish.com:8089/page") {
            cookie(jwtToken)
            setBody(content)
            contentType(ContentType.Text.Plain)
        }
        return resp.bodyAsText()
    }

    suspend fun updateMaiRecord(index: Int, encoded: ByteArray) {
        try {
            val recordParseResult = parseMaiResult(encoded.decodeToString())
            Log.i(TAG, "updateMaiRecord: $recordParseResult")
            val resp =
                client.post("https://www.diving-fish.com/api/maimaidxprober/player/update_records") {
                    setBody(recordParseResult)
                    contentType(ContentType.Application.Json)
                    cookie(jwtToken)
                }
            Log.i(TAG, "updateMaiRecord: ${resp.status} $index")
            if (resp.status == HttpStatusCode.OK) {
                application.sendNotification(
                    R.string.status,
                    application.getString(R.string.upload_result_succeed, MAI_DIFFS[index])
                )
            } else {
                application.sendNotification(
                    R.string.status,
                    application.getString(R.string.upload_result_failed, MAI_DIFFS[index])
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateMaiRecord: ", e)
            application.sendNotification(
                R.string.status,
                application.getString(R.string.upload_result_failed_reason, MAI_DIFFS[index], e.stackTraceToString())
            )
        }
    }

    suspend fun updateChuniRecord(index: Int, encoded: ByteArray){
        try {
            val content = encoded.decodeToString()
            var url = "https://www.diving-fish.com/api/chunithmprober/player/update_records_html"
            if (index == 6){
                url += "?recent=1"
            }
            val resp = client.post(url){
                setBody(content)
                cookie(jwtToken)
            }
            if (resp.status == HttpStatusCode.OK) {
                application.sendNotification(
                    R.string.status,
                    application.getString(R.string.upload_result_succeed, CHUNI_DIFFS[index])
                )
            } else {
                application.sendNotification(
                    R.string.status,
                    application.getString(R.string.upload_result_failed, CHUNI_DIFFS[index])
                )
            }
        }catch (e:Exception){
            Log.e(TAG, "updateChuniRecord: ", e)
            application.sendNotification(
                R.string.status,
                application.getString(R.string.upload_result_failed_reason, CHUNI_DIFFS[index], e.stackTraceToString())
            )
        }
    }
}