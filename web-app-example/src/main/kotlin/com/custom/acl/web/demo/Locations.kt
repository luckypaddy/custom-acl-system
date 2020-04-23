package com.custom.acl.web.demo

import io.ktor.locations.Location

@Location("/login")
data class Login(val userName: String = "", val error: String = "")
@Location("/logout")
class Logout()
@Location("/register")
data class Register(val userName: String = "", val error: String = "")
@Location("/change/password")
data class ChangePassword(val userName: String = "", val error: String = "")
