// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-09-26

package com.enhe.endpoint.action

import com.enhe.endpoint.consts.DING_DOC_BACKEND
import com.enhe.endpoint.consts.DING_PLUGIN_BACKEND
import com.enhe.endpoint.consts.GITHUB_BACKEND
import com.intellij.ide.ui.IdeUiService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class PluginDocAction : AnAction() {

    @Suppress("UnstableApiUsage")
    override fun actionPerformed(e: AnActionEvent) {
        IdeUiService.getInstance().let {
            when (e.presentation.text) {
                "开发指南" -> it.browse(DING_DOC_BACKEND)
                "插件教程" -> it.browse(DING_PLUGIN_BACKEND)
                "Star" -> it.browse(GITHUB_BACKEND)
            }
        }
    }
}