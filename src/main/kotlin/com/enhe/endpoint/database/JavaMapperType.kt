// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint.database

enum class JavaMapperType(val canonicalName: String) {

    // 布尔
    BOOLEAN("Boolean"),
    // 数字类型
    INTEGER("Integer"),
    LONG("Long"),
    FLOAT("Float"),
    DOUBLE("Double"),
    BIGDECIMAL("java.math.BigDecimal"),
    // 字符串
    STRING("String"),
    // 时间
    DATE("java.util.Date"),
    // Json
    JSONOBJECT("com.alibaba.fastjson.JSONObject"),

}