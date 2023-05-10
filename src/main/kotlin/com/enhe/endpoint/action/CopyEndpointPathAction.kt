// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-05-09

package com.enhe.endpoint.action

import com.enhe.endpoint.consts.WINDOW_PANE
import com.enhe.endpoint.window.tree.ControllerNode
import com.enhe.endpoint.window.tree.EndpointNode
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.wm.WindowManager
import java.awt.datatransfer.StringSelection

/**
 * 复制接口路径到粘贴板
 */
class CopyEndpointPathAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        e.getData(WINDOW_PANE)?.getSelected()?.let {
            when (it) {
                is EndpointNode -> {
                    val parent = it.parent
                    if (parent is ControllerNode) {
                        CopyPasteManager.getInstance().setContents(StringSelection(parent.getMajorText() + it.getMajorText()))
                        WindowManager.getInstance().getStatusBar(project)?.let { statusBar ->
                            statusBar.info = "Endpoint path has been copied"
                        }
                    }
                }
            }
        }
    }
}