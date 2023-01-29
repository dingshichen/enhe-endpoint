// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-29

package com.enhe.endpoint.extend

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile

fun PsiFile.getFirstPsiClass(): PsiClass? = this.children.find { it is PsiClass }?.let { it as PsiClass }