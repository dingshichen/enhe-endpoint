// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.window

import com.enhe.endpoint.WINDOW_PANE
import com.enhe.endpoint.window.tree.EndpointTree
import com.enhe.endpoint.window.tree.RootNode
import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.AppUIUtil
import com.intellij.ui.PopupHandler
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.SimpleTreeStructure
import javax.swing.BorderFactory
import javax.swing.tree.TreeSelectionModel

class EndpointPanel(
        private val project: Project,
        private val toolWindow: ToolWindow,
) : SimpleToolWindowPanel(true, true), DataProvider, Disposable {

    private val rootNode = RootNode()

    private var treeModel : StructureTreeModel<AbstractTreeStructure>

    private var catalogTree: EndpointTree

    init {
        val actionManager = ActionManager.getInstance()
        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR,
            actionManager.getAction("enhe.endpoint.window.toolbar.action") as ActionGroup, true)
        actionToolbar.targetComponent = component
        toolbar = actionToolbar.component
        treeModel = StructureTreeModel(object : SimpleTreeStructure() {
            override fun getRootElement() = rootNode
        }, null, this)
        catalogTree = EndpointTree(AsyncTreeModel(treeModel, this))
        initCatalogTree()
        setContent(ScrollPaneFactory.createScrollPane(catalogTree))
        updateCatalogTree()
    }

    override fun getData(dataId: String): Any? {
        if (WINDOW_PANE.`is`(dataId)) {
            return this
        }
        return super.getData(dataId)
    }

    override fun dispose() {
        // TODO
    }

    /**
     * 初始化目录树
     */
    private fun initCatalogTree() {
        catalogTree.isRootVisible = true
        catalogTree.showsRootHandles = true
        catalogTree.emptyText.clear()
        catalogTree.border = BorderFactory.createEmptyBorder()
        catalogTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        PopupHandler.installPopupMenu(catalogTree, "enhe.endpoint.window.catalog.action", ActionPlaces.TOOLWINDOW_CONTENT)
    }

    /**
     * 更新目录树
     */
    fun updateCatalogTree() {
        DumbService.getInstance(project).smartInvokeLater {
            if (toolWindow.isDisposed || !toolWindow.isVisible) {
                toolWindow.show { this.doUpdateCatalogTree() }
            } else {
                doUpdateCatalogTree()
            }
        }
    }

    private fun doUpdateCatalogTree() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Enhe Endpoints Searching...") {
            override fun run(indicator: ProgressIndicator) {
                AppUIUtil.invokeOnEdt {
                    rootNode.updateNode(project)
                    treeModel.invalidate()
                }
            }
        })
    }


}