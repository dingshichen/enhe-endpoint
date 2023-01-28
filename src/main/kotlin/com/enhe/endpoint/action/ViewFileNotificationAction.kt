// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-20

package com.enhe.endpoint.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import com.intellij.util.PsiNavigateUtil

open class ViewFileNotificationAction(private val text: String, private val psiElement: PsiElement) : NotificationAction(text) {

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        PsiNavigateUtil.navigate(psiElement)
    }
}