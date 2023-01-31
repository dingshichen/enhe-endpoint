// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.extend

import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.impl.compiled.ClsFieldImpl

/**
 * 元素是否是注释内容
 */
fun PsiElement.isCommentData() = toString() == "PsiDocToken:DOC_COMMENT_DATA"

/**
 * 获取元素注释内容
 */
fun PsiElement.commentText() = text.replaceToEmpty("*").trim()

/**
 * 获取注解属性的值
 */
fun PsiElement.resolveRealValue(): String? {
    return when (this) {
        is PsiBinaryExpression ->
            buildString {
                this@resolveRealValue.children.forEach {
                    if (it is PsiReferenceExpression) {
                        val resolve = it.resolve()
                        if (resolve is ClsFieldImpl) {
                            this@buildString.append(resolve.initializer?.resolveRealValue())
                        } else {
                            resolve?.children?.forEach { ot -> ot.resolveRealValue()?.let { s -> this@buildString.append(s) } }
                        }
                    } else if (it is PsiLiteralExpression) {
                        this@buildString.append(it.value.toString())
                    }
                }
            }
        is PsiReferenceExpression ->
            buildString {
                resolve()?.children?.forEach { ot -> ot.resolveRealValue()?.let { s -> this@buildString.append(s) } }
            }
        is PsiLiteralExpression -> value.toString()
        else -> null
    }
}