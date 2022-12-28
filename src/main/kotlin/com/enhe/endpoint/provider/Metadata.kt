// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-28

package com.enhe.endpoint.provider

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiIdentifier

class EventMetadata(
    private val adapter: String,
) {

    fun isEqualsAdapter(adapter: String) = adapter == this.adapter
}

class ListenerMetadata(
    val adapter: String,
    val dto: PsiClass,
    val listener: PsiIdentifier,
)