// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.util

object PathStringUtil {

    /**
     * 格式化路径，使其变成 xxx 、 xxx/yyy 的形式
     */
    fun formatPath(path: String?): String {
        if (path.isNullOrBlank() || path == "/") {
            return ""
        }
        if (path.length < 2) {
            return formatPathSuffix(path)
        }
        if (path.startsWith("/")) {
            return formatPathSuffix(path.substring(1))
        }
        return formatPathSuffix(path)
    }

    /**
     * 格式化路径的后缀，将最后一个 / 去除
     */
    private fun formatPathSuffix(path: String): String {
        if (path.isEmpty() || path == "/") {
            return ""
        }
        return if (path.endsWith("/")) {
            path.substring(0, path.length - 1)
        } else {
            path
        }
    }
}