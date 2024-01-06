// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.extend

import com.enhe.endpoint.doc.model.ApiParamExample
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun buildJsonString(builder: JsonObject.() -> Unit): String {
    val json = JsonObject()
    json.builder()
    return json.toString()
}

fun ofJsonObject(builder: JsonObject.() -> Unit) = JsonObject().apply { builder() }

fun ofArrayJson(vararg elements: JsonElement): JsonArray {
    return JsonArray().apply {
        if (elements.isNotEmpty()) {
            elements.forEach { this.add(it) }
        }
    }
}

fun JsonObject.addProperty(name: String, example: ApiParamExample) {
    when (val value = example.value) {
        is String -> this.addProperty(name, value)
        is Boolean -> this.addProperty(name, value)
        is Number -> this.addProperty(name, value)
        is JsonElement -> this.add(name, value)
    }
}