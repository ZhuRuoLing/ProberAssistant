package icu.takeneko.proberassist.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val username:String,
    val password:String
)