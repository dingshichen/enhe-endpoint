// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-13

package com.enhe.endpoint.window

import com.intellij.icons.AllIcons.Icons
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.util.EditSourceOnDoubleClickHandler
import com.intellij.util.PsiNavigateUtil
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.Icon
import javax.swing.JTree
import javax.swing.tree.TreeModel

class EndpointTree(treeModel: TreeModel) : SimpleTree(treeModel) {

    init {
        addMouseListener(object : MouseListener {

            override fun mouseClicked(e: MouseEvent?) {
                when (val selected = selectedNode) {
                    is EndpointNode -> PsiNavigateUtil.navigate(selected.method)
                }
            }

            override fun mousePressed(e: MouseEvent?) {}

            override fun mouseReleased(e: MouseEvent?) {}

            override fun mouseEntered(e: MouseEvent) {}

            override fun mouseExited(e: MouseEvent?) {}
        })

    }

}