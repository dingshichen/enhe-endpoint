// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc

import com.enhe.endpoint.doc.model.Api
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod

interface DocService {

    companion object {

        @JvmStatic
        fun instance(project: Project): DocService = project.getService(DocService::class.java)
    }

    /**
     * 构建文档
     */
    fun buildApi(project: Project, psiClass: PsiClass, psiMethod: PsiMethod): Api

    /**
     * 导出文档
     */
    fun export(project: Project, fileName: String, text: String)
}