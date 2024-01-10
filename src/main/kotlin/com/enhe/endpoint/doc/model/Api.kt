// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.model

import com.enhe.endpoint.extend.*
import com.google.gson.JsonObject
import com.intellij.database.util.isNotNullOrEmpty
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
    // 请求 path 参数
    var pathParams: List<ApiParam>? = null,
    // 请求 url 参数
    var urlParams: List<ApiParam>? = null,
    // 请求 body 参数
    var bodyParams: List<ApiParam>? = null,
    // 响应返回值入参
    var responseParams: List<ApiParam>,
) {

    val fileName: String by lazy {
        url.splitToSmallHump("/")
    }

    val pathText: String by lazy {
        buildString {
            append("|参数|类型|必填|描述|\n|:-----|:-----|:-----|:-----|\n")
            pathParams?.forEach {
                bodyAppend("", it)
            }
        }
    }

    val urlText: String by lazy {
        buildString {
            append("|参数|类型|必填|描述|\n|:-----|:-----|:-----|:-----|\n")
            urlParams?.forEach {
                bodyAppend("", it)
            }
        }
    }

    val bodyText: String by lazy {
        buildString {
            append("|参数|类型|必填|描述|\n|:-----|:-----|:-----|:-----|\n")
            bodyParams?.forEach {
                bodyAppend("", it)
            }
        }
    }

    val bodyExample: String by lazy {
        buildJsonString {
            bodyParams?.forEach {
                putParamExample(it)
            }
        }
    }

    val responseBody: String by lazy {
        buildString {
            append("|参数|类型|描述|\n|:-----|:-----|:-----|\n")
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
        var text = "## $folder\n\n**$name**\n\n**URL:** `$url`\n\n**Type:** `${httpMethod.name}`\n\n**Content-Type:** `$contentType`\n\n"
        if (description.isNotNullOrEmpty && description != name) {
            text = "$text**Description:** $description\n\n"
        }
        pathParams?.let {
            text = "$text**Path-Params:**\n\n$pathText\n\n"
        }
        urlParams?.let {
            text = "$text**Url-Params:**\n\n$urlText\n\n"
        }
        bodyParams?.let {
            text = "$text**Body-Params:**\n\n$bodyText\n\n**Body-Example:**\n```json\n$bodyExample\n```\n\n"
        }
        return@lazy "$text**Response-Params:**\n\n$responseBody\n\n**Response-Example:**\n```json\n$responseExample\n```\n\n"
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
        LangDataType.TIMESTAMP,
        LangDataType.ARRAY,
        LangDataType.ARRAY_STRING,
        LangDataType.ARRAY_BOOL,
        LangDataType.ARRAY_BYTE,
        LangDataType.ARRAY_INT,
        LangDataType.ARRAY_LONG,
        LangDataType.ARRAY_FLOAT,
        LangDataType.ARRAY_TIMESTAMP,
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

private fun StringBuilder.bodyAppend(prefix: String, param: ApiParam) {
    append("| $prefix${param.name} | ${param.type.value} | ${param.requiredText} | ${param.description ?: ""} | \n")
    param.children?.forEach {
        bodyAppend(if (prefix == "") "└─" else "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$prefix",
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