// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-29

package com.enhe.endpoint.extend

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiModifierListOwner


fun PsiModifierListOwner.isAnnotated(annotation: String) = AnnotationUtil.isAnnotated(this, annotation, 0)

fun PsiModifierListOwner.isAnnotated(annotations: List<String>) = AnnotationUtil.isAnnotated(this, annotations, 0)

fun PsiModifierListOwner.findAnnotation(annotation: String) = AnnotationUtil.findAnnotation(this, annotation)

fun PsiModifierListOwner.findAnnotation(annotations: List<String>) = AnnotationUtil.findAnnotation(this, annotations)