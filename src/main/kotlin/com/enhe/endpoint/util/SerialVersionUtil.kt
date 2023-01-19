// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint.util

import java.security.MessageDigest

object SerialVersionUtil {

    private const val ALGORITHM = "SHA"

    fun generateUID(): Long {
        val digest = MessageDigest.getInstance(ALGORITHM)
        val digestBytes = digest.digest(hashCode().toString().toByteArray())
        var serialVersionUID = 0L
        for (i in digestBytes.size.coerceAtMost(8) - 1 downTo 0) {
            serialVersionUID = serialVersionUID shl 8 or (digestBytes[i].toInt() and 0xFF).toLong()
        }
        return serialVersionUID
    }
}