// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-30

package com.enhe.endpoint.action

import com.enhe.endpoint.consts.*
import com.enhe.endpoint.extend.getFieldDescription
import com.enhe.endpoint.extend.hasDocComment
import com.enhe.endpoint.extend.isSerialUID
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifierList
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PsiTreeUtil

class CommentToSwaggerAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val el = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (el != null) {
            PsiTreeUtil.getParentOfType(el, PsiClass::class.java)?.let { replaceToSwagger(project, it) }
        } else {
            e.getData(CommonDataKeys.PSI_FILE)?.let {
                PsiTreeUtil.getChildOfType(it, PsiClass::class.java)?.let { s -> replaceToSwagger(project, s) }
            }
        }
    }

    private fun replaceToSwagger(project: Project, psiClass: PsiClass) {
        val parser = JavaPsiFacade.getInstance(project).parserFacade
        // 跳过不需要替换的属性
        val fields = psiClass.fields.filter {
            !it.hasAnnotation(SK_API_PROP) && it.hasDocComment() && !it.isSerialUID()
        }
        ApplicationManager.getApplication().runWriteAction {
            CommandProcessor.getInstance().executeCommand(project, {
                fields.forEach {
                    it.getFieldDescription()?.let { comment ->
                        val annotation = parser.createAnnotationFromText("@$SK_API_PROP(\"$comment\")", it)
                        // 添加到第一个注释
                        PsiTreeUtil.getChildOfType(it, PsiModifierList::class.java)?.let { pml ->
                            pml.addBefore(annotation, pml.firstChild)
                        }
                        // 删除注释
                        it.docComment?.delete()
                        // 删除几个 Entity 里的注解
                        it.getAnnotation(ANNO_TRANSFORMS)?.delete()
                        it.getAnnotation(ANNO_TRANS)?.delete()
                        it.getAnnotation(MP_TABLE_ID)?.delete()
                        it.getAnnotation(MP_TABLE_FIELD)?.delete()
                    }
                }
                val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiClass.containingFile)
                CodeStyleManager.getInstance(project).reformat(shortened)
            }, "Doc Comment to Swagger Annotation", null)
        }
    }
}