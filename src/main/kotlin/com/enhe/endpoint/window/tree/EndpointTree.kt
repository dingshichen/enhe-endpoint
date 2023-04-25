// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-13

package com.enhe.endpoint.window.tree

import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.util.PsiNavigateUtil
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.TreeModel

class EndpointTree(treeModel: TreeModel) : SimpleTree(treeModel) {

    init {
        addMouseListener(object : MouseAdapter() {

            override fun mouseClicked(e: MouseEvent?) {
                when (val selected = selectedNode) {
                    is EndpointNode -> PsiNavigateUtil.navigate(selected.getMethod())
                }
            }
        })
    }

}