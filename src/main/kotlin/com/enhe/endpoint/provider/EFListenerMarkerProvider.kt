// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-04-04

package com.enhe.endpoint.provider

import com.enhe.endpoint.consts.*
import com.enhe.endpoint.extend.findAdapterValue
import com.enhe.endpoint.extend.getModules
import com.enhe.endpoint.navigate.NavigateUtil.openTargetsMethod
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
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

@Suppress("UnstableApiUsage")
class EFListenerMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return when (element) {
            is PsiMethod -> {
                val psiAnnotation = element.getAnnotation(EVENT_LISTENER) ?: return null
                val value = psiAnnotation.findAttributeValue("value") ?: return null
                if (value !is PsiClassObjectAccessExpression) {
                    return null
                }
                return value.operand.type.let {
                    val eventType = if (it.superTypes.any { s -> s.canonicalText.startsWith(LOCAL_EVENT) }) {
                        EventType.LOCAL
                    } else if (it.superTypes.any { s -> s.canonicalText.startsWith(QUEUE_EVENT) }) {
                        EventType.QUEUE
                    } else return null
                    LineMarkerInfo(psiAnnotation,
                        psiAnnotation.textRange,
                        AllIcons.CodeWithMe.CwmForceFollowMe,
                        { eventType.callEvent },
                        { e, listenerAnnotation -> openTargets(eventType, e, listenerAnnotation.project, it) },
                        GutterIconRenderer.Alignment.RIGHT,
                        { PLUGIN_NAME }
                    )
                }
            }
            is PsiIdentifier -> {
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
                    { e,em -> gotoTaskPublisher(e, em.project, adapter) },
                    GutterIconRenderer.Alignment.LEFT,
                    { PLUGIN_NAME }
                )
            }
            else -> null
        }
    }

    private fun openTargets(eventType: EventType, e: MouseEvent, project: Project, psiType: PsiType) {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val methods = mutableSetOf<JvmMethod>()
        project.getModules().forEach {
            val findMethods = javaPsiFacade.findClass(PUBLISHER, GlobalSearchScope.moduleWithLibrariesScope(it))
                ?.findMethodsByName(eventType.publishMethod) ?: return@forEach
            if (findMethods.size != 1) {
                return@forEach
            }
            methods += findMethods.first()
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
                        if (expressionTypes.size != 1) {
                            return@filter false
                        }
                        if (expressionTypes.first().canonicalText == psiType.canonicalText) {
                            return@filter true
                        }
                    }
                }
                return@filter false
            }.mapNotNull {
                PsiTreeUtil.getParentOfType(it.element, PsiMethodCallExpressionImpl::class.java)
            }
        }
        if (elements.isNotEmpty()) {
            openTargetsMethod(elements.toList(), eventType.toEventTitle, e)
        }
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
            openTargetsMethod(elements.toList(), "Task event publisher", e)
        }
    }
}