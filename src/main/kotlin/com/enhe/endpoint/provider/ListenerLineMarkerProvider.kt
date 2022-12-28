// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-27

package com.enhe.endpoint.provider

import com.enhe.endpoint.ANNOTATION_TASK_SERVICE
import com.enhe.endpoint.BKG_TASK_EXECUTOR
import com.enhe.endpoint.PLUGIN_NAME
import com.enhe.endpoint.SERVICE
import com.enhe.endpoint.psi.findAdapterValue
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.lang.jvm.JvmMethod
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsMethodImpl
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.PsiNavigateUtil

/**
 * 事件监听的标记
 */
@Suppress("UnstableApiUsage")
class ListenerLineMarkerProvider : LineMarkerProvider {

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

        // 暂时不检查泛型
//        val psiType = psiClass.superClassType?.typeArguments()?.firstOrNull() ?: return null
//        if (psiType !is PsiType) {
//            return null
//        }

        return LineMarkerInfo(element,
            element.textRange,
            AllIcons.CodeWithMe.CwmForceFollowMe,
            { "Go to task publisher" },
            {_,_ -> gotoTaskPublisher(element.project, adapter) },
            GutterIconRenderer.Alignment.LEFT,
            { PLUGIN_NAME }
        )
    }

    /**
     * 跳转去任务发布
     */
    private fun gotoTaskPublisher(project: Project, adapter: String) {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val methods = mutableSetOf<JvmMethod>()
        project.modules.forEach {
            val findMethods = javaPsiFacade.findClass(BKG_TASK_EXECUTOR, GlobalSearchScope.moduleWithLibrariesScope(it))
                    ?.findMethodsByName("doTask") ?: return@forEach
            if (findMethods.size != 3) {
                return@forEach
            }
            methods += findMethods[1]
            methods += findMethods[2]
        }
        methods.forEach { method ->
            if (method !is ClsMethodImpl) {
                return@forEach
            }
            ReferencesSearch.search(method.originalElement).find {
                when (val methodCall = it.element.parent) {
                    is PsiMethodCallExpression -> {
                        val expressionTypes = methodCall.argumentList.expressionTypes
                        if (expressionTypes.size !in 3..4) {
                            return@find false
                        }
                        val lastArg = expressionTypes.last()
                        if (lastArg !is PsiClassReferenceType) {
                            return@find false
                        }
                        lastArg.resolve()?.let { pc ->
                            val findAdapterValue = pc.findAdapterValue()
                            return@find findAdapterValue == adapter
                        }
                    }
                }
                return@find false
            }?.let {
                PsiNavigateUtil.navigate(it.element.parent)
                return
            }
        }
    }


}