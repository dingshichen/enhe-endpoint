// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.window

import com.enhe.endpoint.ui.SQLTransferForm
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class EndpointToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ApplicationManager.getApplication().getService(ContentFactory::class.java)
        contentFactory.createContent(EndpointPanel(project, toolWindow), null, false).apply {
            toolWindow.contentManager.addContent(this)
        }
    }
}

class SQLTransferWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ApplicationManager.getApplication().getService(ContentFactory::class.java)
        contentFactory.createContent(SQLTransferForm().root, null, false).apply {
            toolWindow.contentManager.addContent(this)
        }
    }
}