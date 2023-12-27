// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.extend

import com.enhe.endpoint.consts.DELETE_MAPPING
import com.enhe.endpoint.consts.GET_MAPPING
import com.enhe.endpoint.consts.POST_MAPPING
import com.enhe.endpoint.consts.PUT_MAPPING
import com.intellij.util.net.HTTPMethod

fun ofHttpMethod(qualifiedName: String?): HTTPMethod? {
    return when (qualifiedName) {
        GET_MAPPING -> HTTPMethod.GET
        POST_MAPPING -> HTTPMethod.POST
        PUT_MAPPING -> HTTPMethod.PUT
        DELETE_MAPPING -> HTTPMethod.DELETE
        else -> null
    }
}