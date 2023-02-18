// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-02-18

package com.enhe.endpoint.provider

import com.intellij.icons.AllIcons
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

/**
 * Icon + element + className.methodName
 */
class EventPublishCellRenderer : DefaultPsiElementCellRenderer() {

    override fun getIcon(element: PsiElement): Icon {
        return AllIcons.CodeWithMe.CwmFollowMe
    }

    override fun getContainerText(element: PsiElement, name: String): String {
        val psiClassName = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)?.name
        val psiMethodName = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)?.name
        if (psiClassName.isNullOrBlank() || psiMethodName.isNullOrBlank()) {
            return ""
        }
        return "$psiClassName.$psiMethodName"
    }
}