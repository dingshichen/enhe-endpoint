// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint.notifier

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object EnheNotifier {

    private val NOTIFIER = NotificationGroupManager.getInstance().getNotificationGroup("Enhe.Notification.Group")

    @JvmStatic
    fun info(project: Project, content: String, vararg actions: NotificationAction = emptyArray()) {
        NOTIFIER.createNotification(content, NotificationType.INFORMATION).run {
            actions.forEach { addAction(it) }
            notify(project)
        }
    }

    @JvmStatic
    fun warn(project: Project, content: String) =
        NOTIFIER.createNotification(content, NotificationType.WARNING).notify(project)

    @JvmStatic
    fun error(project: Project, content: String) =
        NOTIFIER.createNotification(content, NotificationType.ERROR).notify(project)
}