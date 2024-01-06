// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.extract

import com.enhe.endpoint.consts.CONTROL
import com.enhe.endpoint.consts.SK_API
import com.enhe.endpoint.extend.findFeignClass
import com.enhe.endpoint.extend.findValueAttributeRealValue
import com.intellij.psi.PsiClass
import com.intellij.psi.search.searches.ClassInheritorsSearch

object PsiClassApiExtractor {

    /**
     * 提取 Controller 上的 @Api 注解内容
     */
    fun extractApiFolder(psiClass: PsiClass): String {
        val ps = if (psiClass.isInterface) { psiClass } else psiClass.findFeignClass() ?: return ""
        return ClassInheritorsSearch.search(ps).find { it.hasAnnotation(CONTROL) }?.let {
            it.getAnnotation(SK_API)?.findValueAttributeRealValue()
        }.orEmpty()
    }
}