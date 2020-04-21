package com.custom.acl.web.demo

import io.ktor.locations.Location

@Location("/login")
data class Login(val userName: String = "", val password: String = "")

@Location("/logout")
class Logout()
