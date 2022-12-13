// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.psi

import com.intellij.psi.PsiAnnotation

/**
 * 获取注解的值
 */
fun PsiAnnotation.findAttributeRealValue(attribute: String): String? {
    return this.findDeclaredAttributeValue(attribute)?.resolveRealValue()
}