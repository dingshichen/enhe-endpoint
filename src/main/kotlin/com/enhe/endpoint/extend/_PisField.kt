// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-29

package com.enhe.endpoint.extend

import com.enhe.endpoint.consts.JSON_ALIAS
import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiField

/**
 * 获取属性序列化的名字
 */
fun PsiField.getFieldSerialName(): String {
    return AnnotationUtil.findAnnotation(this, JSON_ALIAS)?.findValueAttributeRealValue() ?: name
}