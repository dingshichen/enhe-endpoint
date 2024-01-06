// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-28

package com.enhe.endpoint.consts

/**
 * Java 的基本类型
 */
enum class JavaBaseType(
    val baseName: String,
    val qualifiedName: String,
    val listName: String,
) {
    BOOLEAN("boolean", "java.lang.Boolean", "java.util.List<java.lang.Boolean>"),
    BYTE("byte", "java.lang.Byte", "java.util.List<java.lang.Byte>"),
    SHORT("short", "java.lang.Short", "java.util.List<java.lang.Short>"),
    INT("int", "java.lang.Integer", "java.util.List<java.lang.Integer>"),
    LONG("long", "java.lang.Integer", "java.util.List<java.lang.Integer>"),
    CHAR("char", "java.lang.Character", "java.util.List<java.lang.Character>"),
    FLOAT("float", "java.lang.Float", "java.util.List<java.lang.Float>"),
    DOUBLE("double", "java.lang.Double", "java.util.List<java.lang.Double>"),
    ;

    companion object {

        @JvmStatic
        fun of(name: String): JavaBaseType? {
            return entries.find { it.baseName == name } ?: entries.find { it.qualifiedName == name }
        }
    }
}