// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-31

package com.enhe.endpoint.extend

import com.enhe.endpoint.SERIAL_UID
import com.intellij.psi.PsiField

/**
 * 获取属性的注释文本
 */
fun PsiField.getFieldDescription(): String? {
    return childrenDocComment()
        ?.filter { it.isCommentData() }
        ?.joinToString("<br>") { it.commentText() }
}

/**
 * 是否是序列化ID
 */
fun PsiField.isSerialUID(): Boolean = name == SERIAL_UID