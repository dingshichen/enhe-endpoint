// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.extend

import com.intellij.psi.PsiAnnotation

/**
 * 获取注解里 value 属性的值
 */
fun PsiAnnotation.findValueAttributeRealValue(): String {
    val value = findAttributeRealValue("value")
    if (value.isNullOrBlank()) {
        val st = this.text.indexOf("{\"") + 2
        val ed = this.text.indexOf("\"}")
        return if (st < 0 || ed < 0 || st >= ed) {
            ""
        } else {
            this.text.substring(st, ed)
        }
    } else {
        return value
    }
}

/**
 * 获取注解的值
 */
fun PsiAnnotation.findAttributeRealValue(attribute: String): String? {
    return this.findDeclaredAttributeValue(attribute)?.resolveRealValue()
}
