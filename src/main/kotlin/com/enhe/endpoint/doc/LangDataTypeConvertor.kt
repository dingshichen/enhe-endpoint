// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc

import com.enhe.endpoint.doc.model.LangDataType
import com.intellij.openapi.project.Project

/**
 * 语言的数据类型转换器
 */
interface LangDataTypeConvertor {

    fun convert(original: String): LangDataType
}