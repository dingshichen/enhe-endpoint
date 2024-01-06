// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-28

package com.enhe.endpoint.doc.mock

import com.enhe.endpoint.doc.model.ApiParamExample
import com.enhe.endpoint.doc.model.LangDataType
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * 数据类型 mock
 */
object LangDataTypeMocker {

    /**
     * 生成示例值
     */
    fun generateValue(dataType: LangDataType): ApiParamExample {
        return when (dataType) {
            LangDataType.BYTE,
            LangDataType.INT,
            LangDataType.LONG -> ApiParamExample(1)
            LangDataType.BOOL -> ApiParamExample(false)
            LangDataType.FLOAT -> ApiParamExample(0.5)
            LangDataType.STRING -> ApiParamExample("enhe")
            LangDataType.ARRAY -> ApiParamExample(JsonArray())
            LangDataType.OBJECT -> ApiParamExample(JsonObject())
            LangDataType.ARRAY_INT,
            LangDataType.ARRAY_BYTE,
            LangDataType.ARRAY_LONG -> ApiParamExample(JsonArray().apply { add(1) })
            LangDataType.ARRAY_STRING -> ApiParamExample(JsonArray().apply { add("dagp") })
            LangDataType.ARRAY_BOOL -> ApiParamExample(JsonArray().apply { add(true) })
            LangDataType.ARRAY_FLOAT -> ApiParamExample(JsonArray().apply { add(0.2) })
            LangDataType.ARRAY_OBJECT -> ApiParamExample(JsonArray().apply { add(JsonObject()) })
            LangDataType.FILE -> ApiParamExample("file")
        }
    }
}