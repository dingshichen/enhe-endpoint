// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-27

package com.enhe.endpoint.provider

import com.enhe.endpoint.consts.ANNOTATION_TASK_SERVICE
import com.enhe.endpoint.consts.BKG_TASK_EXECUTOR
import com.enhe.endpoint.consts.PLUGIN_NAME
import com.enhe.endpoint.consts.SERVICE
import com.enhe.endpoint.extend.findAdapterValue
import com.enhe.endpoint.extend.getModules
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator
import com.intellij.icons.AllIcons
import com.intellij.lang.jvm.JvmMethod
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import java.awt.event.MouseEvent

/**
 * 任务事件监听的标记
 */
@Suppress("UnstableApiUsage", "DialogTitleCapitalization")
class TaskListenerLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier) {
            return null
        }
        val psiClass = element.parent
        if (psiClass !is PsiClass) {
            return null
        }
        psiClass.supers.find { it.qualifiedName == ANNOTATION_TASK_SERVICE } ?: return null
        if (!psiClass.hasAnnotation(SERVICE)) {
            return null
        }
        val adapter = psiClass.findAdapterValue() ?: return null
        return LineMarkerInfo(element,
            element.textRange,
            AllIcons.CodeWithMe.CwmForceFollowMe,
            { "Go to task publisher" },
            { e,_ -> gotoTaskPublisher(e, element.project, adapter) },
            GutterIconRenderer.Alignment.LEFT,
            { PLUGIN_NAME }
        )
    }

    /**
     * 跳转去任务发布
     */
    private fun gotoTaskPublisher(e: MouseEvent, project: Project, adapter: String) {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val methods = mutableSetOf<JvmMethod>()
        project.getModules().forEach {
            val findMethods = javaPsiFacade.findClass(BKG_TASK_EXECUTOR, GlobalSearchScope.moduleWithLibrariesScope(it))
                    ?.findMethodsByName("doTask") ?: return@forEach
            if (findMethods.size != 3) {
                return@forEach
            }
            methods += findMethods[1]
            methods += findMethods[2]
        }
        val elements = mutableListOf<PsiMethodCallExpressionImpl>()
        methods.forEach { method ->
            if (method !is PsiMethod) {
                return@forEach
            }
            elements += ReferencesSearch.search(method.originalElement).filter {
                when (val methodCall = it.element.parent) {
                    is PsiMethodCallExpression -> {
                        val expressionTypes = methodCall.argumentList.expressionTypes
                        if (expressionTypes.size !in 3..4) {
                            return@filter false
                        }
                        val lastArg = expressionTypes.last()
                        if (lastArg !is PsiClassReferenceType) {
                            return@filter false
                        }
                        lastArg.resolve()?.let { pc ->
                            val findAdapterValue = pc.findAdapterValue()
                            return@filter findAdapterValue == adapter
                        }
                    }
                }
                return@filter false
            }.mapNotNull {
                PsiTreeUtil.getParentOfType(it.element, PsiMethodCallExpressionImpl::class.java)
            }
        }
        if (elements.isNotEmpty()) {
            PsiElementListNavigator.openTargets(e, elements.toTypedArray(), "Task event publisher",
                "Task event publisher", EventPublishCellRenderer())
        }
    }


}