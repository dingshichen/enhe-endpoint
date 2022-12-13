// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.action

import com.enhe.endpoint.WINDOW_PANE
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class WindowRefreshAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.getData(WINDOW_PANE)?.updateCatalogTree()
    }
}