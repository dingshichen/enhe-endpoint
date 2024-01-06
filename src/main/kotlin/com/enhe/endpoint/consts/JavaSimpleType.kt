// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-28

package com.enhe.endpoint.consts

/**
 * Java 的一些常见简单类型
 */
enum class JavaSimpleType(
    val qualifiedName: String,
    val listName: String,
) {
    STRING("java.lang.String", "java.util.List<java.lang.String>"),
    BIG_DECIMAL("java.math.BigDecimal", "java.util.List<java.math.BigDecimal>"),
    DATE("java.util.Date", "java.util.List<java.util.Date>"),
    LOCAL_DATE("java.time.LocalDate", "java.util.List<java.time.LocalDate>"),
    LOCAL_TIME("java.time.LocalTime", "java.util.List<java.time.LocalTime>"),
    LOCAL_DATE_TIME("java.time.LocalDateTime", "java.util.List<java.time.LocalDateTime>"),
    ;
}