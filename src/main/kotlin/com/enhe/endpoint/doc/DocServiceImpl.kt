// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc

import com.enhe.endpoint.doc.extract.PsiClassApiExtractor
import com.enhe.endpoint.doc.extract.PsiMethodApiExtractor
import com.enhe.endpoint.doc.model.Api
import com.enhe.endpoint.notifier.EnheNotifier
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import java.io.File
import java.io.IOException

class DocServiceImpl : DocService {

    override fun buildApi(project: Project, psiClass: PsiClass, psiMethod: PsiMethod): Api {
        // 以下提取都要考虑两个场景：提取 service 或者提取 serviceImpl
        return Api(
            folder = PsiClassApiExtractor.extractApiFolder(psiClass),
            name = PsiMethodApiExtractor.extractApiName(psiMethod),
            description = PsiMethodApiExtractor.extractApiDescription(psiMethod),
            url = PsiMethodApiExtractor.extractApiUrl(psiClass, psiMethod),
            deprecated = PsiMethodApiExtractor.extractApiDeprecated(psiClass, psiMethod),
            httpMethod = PsiMethodApiExtractor.extractApiHttpMethod(psiMethod),
            contentType = PsiMethodApiExtractor.extractApiContentType(psiMethod),
            pathParams = PsiMethodApiExtractor.extractApiPathParams(project, psiMethod),
            urlParams = PsiMethodApiExtractor.extractApiUrlParams(project, psiMethod),
            bodyParams = PsiMethodApiExtractor.extractApiBodyParams(project, psiMethod),
            responseParams = PsiMethodApiExtractor.extractApiResponseParams(project, psiMethod),
        )
    }

    override fun export(project: Project, fileName: String, text: String) {
        val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        fileChooserDescriptor.isForcedToUseIdeaFileChooser = true
        FileChooser.chooseFile(fileChooserDescriptor, project, null) {
            try {
                val file = File("${it.path}/$fileName.md")
                FileUtil.writeToFile(file, text)
                EnheNotifier.info(project, "接口文档导出完成")
            } catch (ioException: IOException) {
                EnheNotifier.error(project, "接口文档导出失败")
            }
        }
    }

}