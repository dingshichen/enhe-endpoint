// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode

abstract class BaseNode(parentNode: SimpleNode? = null) : CachingSimpleNode(parentNode) {

    abstract fun updateNode(project: Project)
}