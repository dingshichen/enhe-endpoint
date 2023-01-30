// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-30

package com.enhe.endpoint.extend

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaParserFacade

fun PsiJavaParserFacade.addFieldFromText(text: String, context: PsiElement) {
    createFieldFromText(text, context)
        .also { context.add(it) }
}

fun PsiJavaParserFacade.addMethodFromText(text: String, context: PsiElement) {
    createMethodFromText(text, context)
        .also { context.add(it) }
}