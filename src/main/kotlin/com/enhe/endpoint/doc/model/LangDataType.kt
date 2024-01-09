// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.model

enum class LangDataType(
    val value: String,
    val isBaseType: Boolean,
) {
    STRING("string", true),
    BOOL("boolean", true),
    BYTE("byte", true),
    INT("int", true),
    LONG("long", true),
    FLOAT("float", true),
    TIMESTAMP("timestamp", true),
    ARRAY("array", true),
    OBJECT("object", false),
    FILE("file",false),
    ARRAY_STRING("array<string>", true),
    ARRAY_BOOL("array<boolean>", true),
    ARRAY_BYTE("array<byte>", true),
    ARRAY_INT("array<int>", true),
    ARRAY_LONG("array<long>", true),
    ARRAY_FLOAT("array<float>", true),
    ARRAY_TIMESTAMP("array<timestamp>", true),
    ARRAY_OBJECT("array<object>", false),

}