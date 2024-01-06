// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.extract

import com.enhe.endpoint.consts.*
import com.enhe.endpoint.doc.LangDataTypeConvertor
import com.enhe.endpoint.doc.mock.LangDataTypeMocker
import com.enhe.endpoint.doc.model.ApiParam
import com.enhe.endpoint.doc.model.ApiParamWhere
import com.enhe.endpoint.doc.model.LangDataType
import com.enhe.endpoint.extend.*
import com.enhe.endpoint.util.PathStringUtil
import com.enhe.endpoint.util.PathUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.util.net.HTTPMethod
import org.apache.http.entity.ContentType

object PsiMethodApiExtractor {

    /**
     * 提取方法上的 @ApiOperation 注解 value 内容
     */
    fun extractApiName(psiMethod: PsiMethod): String {
        val psm = if (psiMethod.hasAnnotation(SK_API_OPT)) {
            psiMethod
        } else {
            psiMethod.findSuperMethods().find { it.hasAnnotation(SK_API_OPT) }
        }
        return psm?.let { it.getAnnotation(SK_API_OPT)?.findValueAttributeRealValue() }.orEmpty()
    }

    /**
     * 提取方法上的 @ApiOperation 注解 note 内容
     */
    fun extractApiDescription(psiMethod: PsiMethod): String {
        val psm = if (psiMethod.hasAnnotation(SK_API_OPT)) {
            psiMethod
        } else {
            psiMethod.findSuperMethods().find { it.hasAnnotation(SK_API_OPT) }
        }
        return psm?.let { it.getAnnotation(SK_API_OPT)?.findAttributeRealValue("note") }.orEmpty()
    }

    /**
     * 提取接口路径
     */
    fun extractApiUrl(psiClass: PsiClass, psiMethod: PsiMethod): String {
        return if (psiClass.isInterface) {
            val parentPath = psiClass.getAnnotation(FEIGN_CLIENT)?.findValueAttributeRealValue()?.let { PathUtil.subParentPath(it) }.orEmpty()
            val path = psiMethod.annotations.find { it.qualifiedName in REST_MAPPINGS }?.let { PathStringUtil.formatPath(it.findValueAttributeRealValue()) }.orEmpty()
            "$parentPath/$path"
        } else {
            val parentPath = psiClass.findFeignClass()?.getAnnotation(FEIGN_CLIENT)?.findValueAttributeRealValue()?.let { PathUtil.subParentPath(it) }.orEmpty()
            psiMethod.findSuperMethods().forEach {
                val annotation = it.getAnnotation(GET_MAPPING) ?: it.getAnnotation(POST_MAPPING) ?: it.getAnnotation(PUT_MAPPING)?: it.getAnnotation(DELETE_MAPPING) ?: return@forEach
                return "$parentPath/${annotation.findValueAttributeRealValue()}"
            }
            return parentPath
        }
    }

    /**
     * 提取接口是否被 @Deprecated 注解标记
     */
    fun extractApiDeprecated(psiClass: PsiClass, psiMethod: PsiMethod): Boolean {
        if (psiMethod.hasAnnotation(DEPRECATED)) {
            return true
        }
        if (psiClass.hasAnnotation(DEPRECATED)) {
            return true
        }
        if (psiMethod.findSuperMethods().find { it.hasAnnotation(DEPRECATED) } != null) {
            return true
        }
        if (psiClass.supers.find { it.hasAnnotation(DEPRECATED) } != null) {
            return true
        }
        return false
    }

    /**
     * 提取接口上的 SpringMVC Mapping 注解
     */
    fun extractApiHttpMethod(psiMethod: PsiMethod): HTTPMethod {
        val methods = psiMethod.findSuperMethods().toMutableList().also { it += psiMethod }
        methods.forEach {
            if (it.hasAnnotation(GET_MAPPING)) {
                return HTTPMethod.GET
            } else if (it.hasAnnotation(POST_MAPPING)) {
                return HTTPMethod.POST
            } else if (it.hasAnnotation(PUT_MAPPING)) {
                return HTTPMethod.PUT
            } else if (it.hasAnnotation(DELETE_MAPPING)) {
                return HTTPMethod.DELETE
            }
        }
        return HTTPMethod.GET
    }

    /**
     * 提取接口参数上是否有 @RequestBody 注解以及是否有 MultipartFile 类型的参数
     */
    fun extractApiContentType(psiMethod: PsiMethod): String {
        val methods = psiMethod.findSuperMethods().toMutableList().also { it += psiMethod }
        methods.forEach { m ->
            m.parameterList.parameters.forEach { p ->
                if (p.hasAnnotation(REQUEST_BODY)) {
                    return ContentType.APPLICATION_JSON.mimeType
                }
                if (p.type.isMultipartFileType()) {
                    return ContentType.MULTIPART_FORM_DATA.mimeType
                }
            }
        }
        return ContentType.APPLICATION_FORM_URLENCODED.mimeType
    }

    /**
     * 提取请求参数，请求参数可能在 url 中、body 里、表单里
     */
    fun extractApiRequestParams(project: Project, psiMethod: PsiMethod): List<ApiParam> {
        val dataTypeConvertor = project.getService(LangDataTypeConvertor::class.java)
        val method = psiMethod.findDeepestSuperMethod() ?: psiMethod
        val apiParams = mutableListOf<ApiParam>()
        method.parameterList.parameters
            .filter { it.type.canonicalText !in listOf(HTTP_SER_REQ, HTTP_SER_RES) }
            .forEach {
                if (it.hasAnnotation(PATH_VAR)) {
                    // 参数在 path 中，一定是简单类型
                    val dataType = dataTypeConvertor.convert(it.type.canonicalText)
                    apiParams += ApiParam(
                        name = it.name,
                        type = dataType,
                        where = ApiParamWhere.PATH,
                        required = it.getAnnotation(PATH_VAR).findRequiredAttributeRealValue(),
                        description = it.getAnnotation(SK_API_PROP)?.findValueAttributeRealValue(),
                        example = LangDataTypeMocker.generateValue(dataType),
                    )
                } else if (it.hasAnnotation(REQUEST_BODY)) {
                    // 参数在 body 中，一定是一个集合带泛型的类型或者是一个自定义的类型
                    val dataType = dataTypeConvertor.convert(it.type.canonicalText)
                    // 判断是一个自定义的对象类型，递归查询其属性的类型
                    if (dataType == LangDataType.OBJECT && it.type is PsiClassType) {
                        apiParams += PsiClassTypeApiExtractor.extractApiParam(
                            project = project,
                            psiClassType = it.type as PsiClassType,
                            paramWhere = ApiParamWhere.BODY,
                        )
                    }
                } else if (it.hasAnnotation(MULTIPART_FILE)) {
                    // 参数是上传的文件流
                    val paramAn = it.getAnnotation(REQUEST_PARAM)
                    apiParams += ApiParam(
                        name = paramAn?.findValueAttributeRealValue() ?: "file",
                        type = LangDataType.FILE,
                        where = ApiParamWhere.URL,
                        required = paramAn.findRequiredAttributeRealValue(),
                        description = it.getAnnotation(SK_API_PROP)?.findValueAttributeRealValue(),
                        example = LangDataTypeMocker.generateValue(LangDataType.FILE),
                    )
                } else {
                    // 参数在 url 里
                    // 分两种情况。一种是标识了 @ApiParam 的简单类型，一种是没有添加标识但是一个自定义的对象类型
                    val paramAn = it.getAnnotation(REQUEST_PARAM)
                    val dataType = dataTypeConvertor.convert(it.type.canonicalText)
                    if (paramAn == null) {
                        // 判断是一个自定义的对象类型，递归查询其属性的类型
                        if (dataType == LangDataType.OBJECT && it.type is PsiClassType) {
                            apiParams += PsiClassTypeApiExtractor.extractApiParam(
                                project = project,
                                psiClassType = it.type as PsiClassType,
                                paramWhere = ApiParamWhere.URL,
                            )
                        }
                    } else {
                        apiParams += ApiParam(
                            name = paramAn.findValueAttributeRealValue(),
                            type = dataType,
                            where = ApiParamWhere.URL,
                            required = paramAn.findRequiredAttributeRealValue(),
                            description = it.getAnnotation(SK_API_PROP)?.findValueAttributeRealValue(),
                            example = LangDataTypeMocker.generateValue(dataType),
                        )
                    }
                }
            }
        return apiParams
    }

    /**
     * 提取返回值
     */
    fun extractApiResponseParams(project: Project, psiMethod: PsiMethod): List<ApiParam> {
        return psiMethod.returnTypeElement?.type?.let {
            PsiClassTypeApiExtractor.extractApiParam(
                project = project,
                psiClassType = it as PsiClassType,
                paramWhere = ApiParamWhere.BODY
            )
        } ?: emptyList()
    }
}