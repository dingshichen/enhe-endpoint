// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc

import com.enhe.endpoint.doc.model.LangDataType
import com.enhe.endpoint.util.ApiStringUtil

object JavaDataTypeConvertor : LangDataTypeConvertor {

    private val stringList = arrayOf("String")
    private val timeList = arrayOf("Date", "DateTime", "LocalDate", "LocalDateTime")
    private val boolList = arrayOf("Boolean", "boolean")
    private val byteList = arrayOf("byte", "Byte")
    private val intList = arrayOf("Integer", "int", "short", "Short")
    private val longList = arrayOf("Long", "long")
    private val floatList = arrayOf("float", "Float", "Double", "double", "BigDecimal")
    private val arrayList = arrayOf("List", "ArrayList", "LinkedList", "JSONArray")

    override fun convert(original: String): LangDataType = when (original) {
        in stringList -> LangDataType.STRING
        in timeList -> LangDataType.TIMESTAMP
        in boolList -> LangDataType.BOOL
        in byteList -> LangDataType.BYTE
        in intList -> LangDataType.INT
        in longList -> LangDataType.LONG
        in floatList -> LangDataType.FLOAT
        in arrayList -> LangDataType.ARRAY
        else -> if (ApiStringUtil.isJavaGenericCollection(original)) {
            // 获取泛型对应类型
            when (convert(ApiStringUtil.subJavaGeneric(original))) {
                LangDataType.STRING -> LangDataType.ARRAY_STRING
                LangDataType.BOOL -> LangDataType.ARRAY_BOOL
                LangDataType.BYTE -> LangDataType.ARRAY_BYTE
                LangDataType.INT -> LangDataType.ARRAY_INT
                LangDataType.LONG -> LangDataType.ARRAY_LONG
                LangDataType.FLOAT -> LangDataType.ARRAY_FLOAT
                LangDataType.TIMESTAMP -> LangDataType.ARRAY_TIMESTAMP
                else -> LangDataType.ARRAY_OBJECT
            }
        } else LangDataType.OBJECT
    }
}