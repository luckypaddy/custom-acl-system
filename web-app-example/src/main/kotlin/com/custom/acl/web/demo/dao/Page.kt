package com.custom.acl.web.demo.dao

data class Page(val size: Int, val number: Int) {
    fun offset(): Long = number.toLong() * size.toLong()
}