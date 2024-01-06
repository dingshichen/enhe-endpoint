// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.util

import com.enhe.endpoint.doc.model.LangDataType
import com.enhe.endpoint.extend.simpleSubstring

object ApiStringUtil {

    /**
     * 判断文本是不是 Java 集合泛型
     */
    fun isJavaGenericCollection(text: String): Boolean {
        return text.startsWith("List<")
                || text.startsWith("ArrayList<")
                || text.startsWith("LinkedList<")
                || text.startsWith("Set<")
                || text.startsWith("HashSet<")
    }

    /**
     * 截取出 Java 泛型
     */
    fun subJavaGeneric(text: String): String {
        return text.simpleSubstring("<", ">")
    }

    /**
     * 分割字符串转换成对应 type 集合
     */
    fun splitToTypeList(text: String, type: LangDataType): List<Any> {
        return text.split(",").map {
            it.trim().run {
                when (type) {
                    LangDataType.ARRAY -> toString()
                    LangDataType.ARRAY_STRING -> toString()
                    LangDataType.ARRAY_BYTE -> toInt()
                    LangDataType.ARRAY_INT -> toInt()
                    LangDataType.ARRAY_LONG -> toLong()
                    LangDataType.ARRAY_FLOAT -> toFloat()
                    else -> toString()
                }
            }
        }
    }
}