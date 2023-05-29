// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import javax.swing.Icon

abstract class BaseNode(parentNode: SimpleNode? = null) : CachingSimpleNode(parentNode) {

    abstract fun updateNode(project: Project)

    abstract fun getCusIcon(): Icon

    override fun update(presentation: PresentationData) {
        val newElement = updateElement()
        if (element !== newElement) {
            presentation.isChanged = true
        }
        if (newElement == null) return
        doUpdateV2(presentation)
        fillFallbackPropertiesV2(presentation)
    }

    open fun clearAll() {
        cleanUpCache()
    }

    open fun getMajorText(): String {
        return ""
    }

    protected open fun doUpdateV2(presentation: PresentationData) {

    }

    private fun fillFallbackPropertiesV2(presentation: PresentationData) {
        var text: String? = getColoredTextAsPlainTextV2(presentation)
        if (text == null) {
            text = presentation.presentableText
        }
        if (text == null) {
            text = myName
        }
        presentation.presentableText = text

        if (presentation.getIcon(false) == null) {
            presentation.setIcon(myClosedIcon)
        }

        if (presentation.forcedTextForeground == null) {
            presentation.forcedTextForeground = myColor
        }
    }

    private fun getColoredTextAsPlainTextV2(presentation: PresentationData): String? {
        if (presentation.coloredText.isNotEmpty()) {
            val result = StringBuilder()
            for (each in presentation.coloredText) {
                result.append(each.text)
            }
            return result.toString()
        }
        return null
    }
}