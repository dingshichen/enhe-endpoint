// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.model

import com.enhe.endpoint.extend.*
import com.google.gson.JsonObject
import com.intellij.util.net.HTTPMethod

/**
 * API 接口
 */
data class Api(
    // 文件夹
    val folder: String,
    // 文档名称
    val name: String,
    // 文档概述
    var description: String? = null,
    // 访问URL
    val url: String,
    // 废弃信息
    val deprecated: Boolean? = null,
    // HTTP 请求方式
    val httpMethod: HTTPMethod,
    // HTTP ContentType
    val contentType: String,
    // 请求入参
    var requestParams: List<ApiParam>,
    // 响应返回值入参
    var responseParams: List<ApiParam>,
) {

    val fileName: String by lazy {
        url.splitToSmallHump("/")
    }

    val requestBody: String by lazy {
        buildString {
            append("|参数名|类型|是否必填|描述|\n|:-----|:-----|:-----|:-----|\n")
            requestParams.forEach {
                requestAppend("", it)
            }
        }
    }

    val requestExample: String by lazy {
        buildJsonString {
            requestParams.forEach {
                putParamExample(it)
            }
        }
    }

    val responseBody: String by lazy {
        buildString {
            append("|参数名|类型|描述|\n|:-----|:-----|:-----|\n")
            responseParams.forEach {
                responseAppend("", it)
            }
        }
    }

    val responseExample: String by lazy {
        buildJsonString {
            responseParams.forEach {
                putParamExample(it)
            }
        }
    }

    val markdownText: String by lazy {
        "**$name**\n\n" +
                "**URL:** `$url`\n\n" +
                "**Type:** `${httpMethod.name}`\n\n" +
                "**Content-Type:** `$contentType`\n\n" +
                "**Description:** $description\n\n" +
                "**Body-parameters:**\n\n$requestBody\n\n" +
                "**Request-example:**\n```json\n$requestExample\n```\n\n" +
                "**Response-fields:**\n\n$responseBody\n\n" +
                "**Response-example:**\n```json\n$responseExample\n```\n\n"
    }

}

fun JsonObject.putParamExample(param: ApiParam) {
    addProperty(param.name, getExample(param))
}

fun getExample(param: ApiParam): ApiParamExample {
    return when (param.type) {
        LangDataType.STRING,
        LangDataType.BYTE,
        LangDataType.INT,
        LangDataType.LONG,
        LangDataType.FLOAT,
        LangDataType.BOOL,
        LangDataType.ARRAY,
        LangDataType.ARRAY_STRING,
        LangDataType.ARRAY_BOOL,
        LangDataType.ARRAY_BYTE,
        LangDataType.ARRAY_INT,
        LangDataType.ARRAY_LONG,
        LangDataType.ARRAY_FLOAT,
        LangDataType.FILE -> param.example
        LangDataType.OBJECT -> {
            val json = ofJsonObject {
                param.children?.forEach {
                    addProperty(it.name, getExample(it))
                }
            }
            return ApiParamExample(json)
        }
        LangDataType.ARRAY_OBJECT -> {
            val jsonArray = ofArrayJson(
                ofJsonObject {
                    param.children?.forEach {
                        addProperty(it.name, getExample(it))
                    }
                }
            )
            return ApiParamExample(jsonArray)
        }
    }
}

private fun StringBuilder.requestAppend(prefix: String, param: ApiParam) {
    append("| $prefix${param.name} | ${param.type.value} | ${param.requiredText} | ${param.description ?: ""} | \n")
    param.children?.forEach {
        requestAppend(if (prefix == "") "└─" else "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$prefix",
            it
        )
    }
}

private fun StringBuilder.responseAppend(prefix: String, param: ApiParam) {
    append("| $prefix${param.name} | ${param.type.value} | ${param.description ?: ""} | \n")
    param.children?.forEach {
        responseAppend(
            if (prefix == "") "└─" else "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$prefix",
            it
        )
    }
}