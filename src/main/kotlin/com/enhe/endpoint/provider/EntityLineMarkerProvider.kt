// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-28

package com.enhe.endpoint.provider

import com.enhe.endpoint.consts.MP_TABLE_NAME
import com.enhe.endpoint.consts.PLUGIN_NAME
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PsiNavigateUtil

/**
 * Entity 的标记
 */
class EntityLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier) {
            return null
        }
        val psiClass = element.parent
        if (psiClass !is PsiClass) {
            return null
        }
        val hasTable = psiClass.hasAnnotation(MP_TABLE_NAME)
        if (!hasTable) {
            return null
        }
        val qualifiedName = psiClass.qualifiedName
        if (qualifiedName.isNullOrBlank()) {
            return null
        }
        val module = ModuleUtil.findModuleForPsiElement(element)
        return LineMarkerInfo(element,
            element.textRange,
            AllIcons.Javaee.PersistenceEntity,
            { "Go to mapper" },
            {_,em -> gotoMapper(em.project, module, qualifiedName) },
            GutterIconRenderer.Alignment.LEFT,
            { PLUGIN_NAME }
        )
    }

    /**
     * 跳转到 mapper
     */
    private fun gotoMapper(project: Project, module: Module?, qualifiedName: String) {
        // TODO 正则校验检查一次
        val mapperQualified = qualifiedName.replace(".entity.", ".mapper.")
            .replace("Entity", "Mapper")
        val scope = module?.let { GlobalSearchScope.moduleScope(module) } ?: GlobalSearchScope.projectScope(project)
        val mapperClass = JavaPsiFacade.getInstance(project).findClass(mapperQualified, scope)
        PsiNavigateUtil.navigate(mapperClass)
    }
}