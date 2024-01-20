// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint.notifier

import com.enhe.endpoint.consts.DING_PLUGIN_BACKEND
import com.enhe.endpoint.consts.GITHUB_BACKEND
import com.intellij.notification.BrowseNotificationAction
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import javax.swing.Icon

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

    @JvmStatic
    fun notifyStartup(project: Project, icon: Icon) {
        NOTIFIER.createNotification("如果遇到麻烦、或者有什么需求和建议，可以直接联系开发者", NotificationType.INFORMATION)
            .setTitle("Enhe Endpoint Java 开发者工具")
            .setIcon(icon)
            .addAction(BrowseNotificationAction("帮助文档", DING_PLUGIN_BACKEND))
            .addAction(BrowseNotificationAction("Star", GITHUB_BACKEND))
            .notify(project)
    }
}