// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-05-25

package com.enhe.endpoint.window.search

import com.enhe.endpoint.window.EndpointModel
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.ide.util.gotoByName.GotoActionModel
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.render.IconCompOptionalCompPanel
import com.intellij.util.Processor
import com.intellij.util.PsiNavigateUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.accessibility.AccessibleContext
import javax.swing.JList
import javax.swing.ListCellRenderer

class EndpointSearchContributor : WeightedSearchEverywhereContributor<EndpointModel> {

    override fun getSearchProviderId(): String {
        return this.javaClass.simpleName
    }

    override fun getGroupName(): String {
        return "Endpoints"
    }

    override fun getSortWeight(): Int {
        return 600
    }

    override fun showInFindResults(): Boolean {
        return true
    }

    override fun isShownInSeparateTab(): Boolean {
        return true
    }

    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<EndpointModel>>
    ) {
        if (StringUtil.isEmptyOrSpaces(pattern)) {
            return
        }
        ProgressManager.getInstance().executeProcessUnderProgress({
            EndpointItemProvider.filterElements(pattern) {
                if (progressIndicator.isCanceled) {
                    return@filterElements false
                }
                val descriptor = FoundItemDescriptor(it, it.matchingDegree)
                return@filterElements consumer.process(descriptor)
            }
        }, progressIndicator)
    }

    override fun getElementsRenderer(): ListCellRenderer<in EndpointModel> {
        return object : ListCellRenderer<EndpointModel> {
            override fun getListCellRendererComponent(
                list: JList<out EndpointModel>,
                value: EndpointModel,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val component = SimpleColoredComponent()
                val panel: IconCompOptionalCompPanel<SimpleColoredComponent> = object : IconCompOptionalCompPanel<SimpleColoredComponent>(component) {
                    override fun getAccessibleContext(): AccessibleContext {
                        return component.accessibleContext
                    }
                }
                panel.border = JBUI.Borders.empty(2)
                panel.isOpaque = true
                panel.background = UIUtil.getListBackground(isSelected, cellHasFocus)
                panel.setIcon(value.icon)
                component.font = list.font
                component.isOpaque = false
                component.append(value.path, SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, GotoActionModel.defaultActionForeground(isSelected, cellHasFocus, null)))
                return panel
            }

        }
    }

    override fun getDataForItem(element: EndpointModel, dataId: String): Any? {
        return null
    }

    override fun processSelectedItem(selected: EndpointModel, modifiers: Int, searchText: String): Boolean {
        PsiNavigateUtil.navigate(selected.method)
        return true
    }

}


class EndpointSearchContributorFactory : SearchEverywhereContributorFactory<EndpointModel> {

    override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<EndpointModel> {
        return EndpointSearchContributor()
    }

}