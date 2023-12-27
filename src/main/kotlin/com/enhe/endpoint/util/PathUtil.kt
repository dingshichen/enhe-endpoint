package com.enhe.endpoint.util

object PathUtil {

    fun subParentPath(parentPath: String): String {
        val index = parentPath.indexOf("/")
        if (index == -1) {
            return parentPath.replace("//", "/")
        }
        return parentPath.substring(index + 1).replace("//", "/")
    }

}