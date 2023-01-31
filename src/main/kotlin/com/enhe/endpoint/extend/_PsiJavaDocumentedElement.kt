// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-31

package com.enhe.endpoint.extend

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaDocumentedElement

/**
 * 直接获取 Java 注释子元素
 */
fun PsiJavaDocumentedElement.childrenDocComment(): Array<PsiElement>? = docComment?.children

/**
 * 是否有注释
 */
fun PsiJavaDocumentedElement.hasDocComment(): Boolean = this.docComment != null