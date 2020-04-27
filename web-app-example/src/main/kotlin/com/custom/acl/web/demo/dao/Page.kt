package com.custom.acl.web.demo.dao

/**
 * Page request for extracting slice of data wih DAO
 *
 * @property size
 * @property number
 */
data class Page(val size: Int, val number: Int) {
    fun offset(): Long = number.toLong() * size.toLong()
}