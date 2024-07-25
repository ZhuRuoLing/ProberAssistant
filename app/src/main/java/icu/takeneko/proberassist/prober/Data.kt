package icu.takeneko.proberassist.prober

import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val username:String,
    val password:String
)