// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.window

import com.enhe.endpoint.consts.WINDOW_PANE
import com.enhe.endpoint.window.search.EndpointItemProvider
import com.enhe.endpoint.window.tree.EndpointContext
import com.enhe.endpoint.window.tree.EndpointNode
import com.enhe.endpoint.window.tree.RootNode
import com.intellij.ide.util.treeView.AbstractTreeStructure
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
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
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.ui.treeStructure.SimpleTreeStructure
import com.intellij.util.PsiNavigateUtil
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.tree.TreeSelectionModel

class EndpointPanel(
        private val project: Project,
        private val toolWindow: ToolWindow,
) : SimpleToolWindowPanel(true, true), DataProvider, Disposable {

    private val rootNode = RootNode()

    private var treeModel : StructureTreeModel<AbstractTreeStructure>

    private var catalogTree: SimpleTree

    private var updating = false

    private lateinit var popupMenu: ActionPopupMenu

    // 接口目录树中被选中的节点
    private var selectedEndpoint: SimpleNode? = null

    init {
        val actionManager = ActionManager.getInstance()
        val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR,
            actionManager.getAction("enhe.endpoint.window.toolbar.action") as ActionGroup, true)
        actionToolbar.targetComponent = component
        toolbar = actionToolbar.component
        treeModel = StructureTreeModel(object : SimpleTreeStructure() {
            override fun getRootElement() = rootNode
        }, null, this)
        catalogTree = SimpleTree(AsyncTreeModel(treeModel, this))
        catalogTree.isRootVisible = false
        initPopupMenu()
        initCatalogTree()
        setContent(ScrollPaneFactory.createScrollPane(catalogTree))
        // TODO 考虑是否不直接展示，让用户点击下刷新
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
     * 初始化树上的右键菜单
     */
    private fun initPopupMenu() {
        val actionManager = ActionManager.getInstance()
        popupMenu = actionManager.createActionPopupMenu("EndpointPopup",
            actionManager.getAction("enhe.endpoint.window.tree.popup.action") as ActionGroup)
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
        catalogTree.addMouseListener(object : MouseAdapter() {

            override fun mouseClicked(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1) {
                    when (val selected = catalogTree.selectedNode) {
                        is EndpointNode -> PsiNavigateUtil.navigate(selected.getMethod())
                    }
                }
            }

            override fun mousePressed(e: MouseEvent) {
                tryShowPopupMenu(e)
            }

            override fun mouseReleased(e: MouseEvent) {
                tryShowPopupMenu(e)
            }

            /**
             * 尝试打开菜单
             */
            private fun tryShowPopupMenu(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    when (val selected = catalogTree.selectedNode) {
                        is EndpointNode -> {
                            selectedEndpoint = selected
                            popupMenu.component.show(catalogTree, e.x, e.y)
                        }
                    }
                }
            }
        })
    }

    /**
     * 获取选中的断点
     */
    fun getSelected() = selectedEndpoint

    /**
     * 更新目录树
     */
    fun updateCatalogTree() {
        if (!updating) {
            project.getService(DumbService::class.java).smartInvokeLater {
                if (toolWindow.isDisposed || !toolWindow.isVisible) {
                    toolWindow.show { this.doUpdateCatalogTree() }
                } else {
                    doUpdateCatalogTree()
                }
            }
        }
    }

    private fun doUpdateCatalogTree() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Enhe endpoints searching...") {

            override fun run(indicator: ProgressIndicator) {
                updating = true
                EndpointItemProvider.getInstance(project).clear()
                AppUIUtil.invokeOnEdt {
                    EndpointContext.refresh(project)
                    rootNode.updateNode(project)
                    treeModel.invalidate()
                }
                updating = false
            }

            override fun onCancel() {
                super.onCancel()
                rootNode.clearAll()
            }

            override fun onThrowable(error: Throwable) {
                super.onThrowable(error)
                rootNode.clearAll()
            }
        })
    }


}