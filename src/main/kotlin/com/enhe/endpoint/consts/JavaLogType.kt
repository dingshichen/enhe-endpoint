// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2024-01-06

package com.enhe.endpoint.consts

enum class JavaLogType(
    val qualifierName: String,
) {
    SLF4J("org.slf4j.Logger"),
    UTIL("java.util.logging.Logger"),
    CLASSIC("ch.qos.logback.classic.Logger"),
    LOG4J("org.apache.logging.log4j.Logger"),
    IBATIS("org.apache.ibatis.logging.Log"),
    ;
}