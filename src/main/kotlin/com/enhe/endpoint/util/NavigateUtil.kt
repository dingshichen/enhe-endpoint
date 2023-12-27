package com.enhe.endpoint.util

import com.intellij.codeInsight.navigation.getPsiElementPopup
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.PsiNavigateUtil
import java.awt.event.MouseEvent;

object NavigateUtil {

    @JvmStatic
    fun openTargetsMethod(elements: List<PsiElement>, title: String, e: MouseEvent) {
        if (elements.size == 1) {
            PsiNavigateUtil.navigate(elements.first());
        } else {
            getPsiElementPopup(elements.toTypedArray(), title).show(RelativePoint(e))
        }
    }
}