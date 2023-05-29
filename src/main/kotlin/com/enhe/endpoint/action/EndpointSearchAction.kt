// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-05-25

package com.enhe.endpoint.action

import com.enhe.endpoint.window.search.EndpointSearchContributor
import com.intellij.ide.actions.SearchEverywhereBaseAction
import com.intellij.openapi.actionSystem.AnActionEvent

class EndpointSearchAction : SearchEverywhereBaseAction() {

    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }
        val tabID = EndpointSearchContributor::class.java.simpleName
        showInSearchEverywherePopup(tabID, e, true, true)
    }
}