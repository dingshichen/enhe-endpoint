// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.extract

import com.enhe.endpoint.consts.*
import com.enhe.endpoint.doc.LangDataTypeConvertor
import com.enhe.endpoint.doc.mock.LangDataTypeMocker
import com.enhe.endpoint.doc.model.ApiParam
import com.enhe.endpoint.doc.model.ApiParamExample
import com.enhe.endpoint.doc.model.ApiParamWhere
import com.enhe.endpoint.doc.model.LangDataType
import com.enhe.endpoint.extend.*
import com.enhe.endpoint.util.PathStringUtil
import com.enhe.endpoint.util.PathUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.util.childrenOfType
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
        return psm?.let { it.getAnnotation(SK_API_OPT)?.findAttributeRealValue("notes") }.orEmpty()
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
                val path = annotation.findValueAttributeRealValue().let { v -> PathStringUtil.formatPath(v) }
                return "$parentPath/$path"
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
     * 从 PATH 里提取参数
     */
    fun extractApiPathParams(project: Project, psiMethod: PsiMethod): List<ApiParam>? {
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
                }
            }
        return if (apiParams.isEmpty()) null else apiParams
    }

    /**
     * 从 url 里提取参数
     */
    fun extractApiUrlParams(project: Project, psiMethod: PsiMethod): List<ApiParam>? {
        val dataTypeConvertor = project.getService(LangDataTypeConvertor::class.java)
        val method = psiMethod.findDeepestSuperMethod() ?: psiMethod
        val apiParams = mutableListOf<ApiParam>()
        method.parameterList.parameters
            .filter { it.type.canonicalText !in listOf(HTTP_SER_REQ, HTTP_SER_RES) }
            .forEach {
                if (!it.hasAnnotation(PATH_VAR) && !it.hasAnnotation(REQUEST_BODY)) {
                    if (it.hasAnnotation(MULTIPART_FILE)) {
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
                            // TODO 需要排除掉 HttpRequest HttpResponse 这种直接注入的
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
            }
        return if (apiParams.isEmpty()) null else apiParams
    }

    /**
     * 从 body 里提取参数
     */
    fun extractApiBodyParams(project: Project, psiMethod: PsiMethod): List<ApiParam>? {
        val dataTypeConvertor = project.getService(LangDataTypeConvertor::class.java)
        val method = psiMethod.findDeepestSuperMethod() ?: psiMethod
        val apiParams = mutableListOf<ApiParam>()
        method.parameterList.parameters
            .filter { it.type.canonicalText !in listOf(HTTP_SER_REQ, HTTP_SER_RES) }
            .forEach {
                if (it.hasAnnotation(REQUEST_BODY)) {
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
                }
            }
        return if (apiParams.isEmpty()) null else apiParams
    }

    /**
     * 如果解析没有返回值，返回 {"code":200,"success":true,"msg":"操作成功"}
     * 如果是 boolean 值，返回 {"code":200,"success":true,"data":{"status":true},"msg":"操作成功"}
     * 如果是 list 值，返回 {"code":200,"success":true,"data":{"list":[]},"msg":"操作成功"}
     * 如果是 int 值，返回 {"code":200,"success":true,"data":{"value":1660},"msg":"操作成功"}
     * 如果是 Date 值 {"code":200,"success":true,"data":{"value":1704872266477},"msg":"操作成功"}
     * 如果是 string 值，返回 {"code":200,"success":true,"data":{"message":"20240110145534265t111100"},"msg":"操作成功"}
     * 如果是 分页或者其他自定义对象，返回 data json 格式
     *
     * 提取返回值
     */
    fun extractApiResponseParams(project: Project, psiMethod: PsiMethod): List<ApiParam> {
        val returnApiParams = mutableListOf(
            ApiParam(name = "code", type = LangDataType.INT, where = ApiParamWhere.RETURN, required = true, description = "服务状态码", example = ApiParamExample(200)),
            ApiParam(name = "success", type = LangDataType.BOOL, where = ApiParamWhere.RETURN, required = true, description = "服务处理状态", example = ApiParamExample(true)),
            ApiParam(name = "msg", type = LangDataType.INT, where = ApiParamWhere.RETURN, required = true, description = "服务状态信息", example = ApiParamExample("操作成功"))
        )
        val returnElement = psiMethod.returnTypeElement ?: return returnApiParams
        val psiType = returnElement.type
        // 集合类型的处理
        if (psiType.isJavaGenericList()) {
            return returnApiParams.apply {
                val genericPsiType = returnElement.children.first()?.childrenOfType<PsiReferenceParameterList>()?.first()?.typeArguments?.first()
                var listType: LangDataType = LangDataType.ARRAY
                var listChildren: List<ApiParam>? = null
                genericPsiType?.let {
                    listType = LangDataTypeConvertor.instance(project).convert(psiType.presentableText)
                    if (!it.isJavaBaseType() && !it.isJavaSimpleType()) {
                        listChildren = PsiClassTypeApiExtractor.extractApiParam(project = project, psiClassType = it as PsiClassType, paramWhere = ApiParamWhere.URL)
                    }
                    // TODO 有可能有泛型里面是集合还有泛型
                }
                this += ApiParam(
                    name = "data",
                    type = LangDataType.OBJECT,
                    where = ApiParamWhere.RETURN,
                    required = true,
                    description = "业务响应数据",
                    example = ApiParamExample(JsonObject()),
                    children = listOf(
                        // type 要根据泛型里的类型去解析
                        ApiParam(name = "list", type = listType, where = ApiParamWhere.RETURN, required = true, description = "业务响应数据列表", example = ApiParamExample(JsonArray()), children = listChildren),
                    )
                )
            }
        }
        return when (psiType.canonicalText) {
            JavaBaseType.BOOLEAN.qualifiedName -> returnApiParams.apply {
                this += ApiParam(
                    name = "data",
                    type = LangDataType.OBJECT,
                    where = ApiParamWhere.RETURN,
                    required = true,
                    description = "业务响应数据",
                    example = ApiParamExample(JsonObject()),
                    children = listOf(
                        ApiParam(name = "status", type = LangDataType.BOOL, where = ApiParamWhere.RETURN, required = true, description = "操作状态", example = ApiParamExample(true))
                    )
                )
            }
            JavaBaseType.INT.qualifiedName -> returnApiParams.apply {
                this += ApiParam(
                    name = "data",
                    type = LangDataType.OBJECT,
                    where = ApiParamWhere.RETURN,
                    required = true,
                    description = "业务响应数据",
                    example = ApiParamExample(JsonObject()),
                    children = listOf(
                        ApiParam(name = "value", type = LangDataType.INT, where = ApiParamWhere.RETURN, required = true, description = "值", example = ApiParamExample(1024))
                    )
                )
            }
            JavaSimpleType.STRING.qualifiedName -> returnApiParams.apply {
                this += ApiParam(
                    name = "data",
                    type = LangDataType.OBJECT,
                    where = ApiParamWhere.RETURN,
                    required = true,
                    description = "业务响应数据",
                    example = ApiParamExample(JsonObject()),
                    children = listOf(
                        ApiParam(name = "message", type = LangDataType.STRING, where = ApiParamWhere.RETURN, required = true, description = "文本内容", example = ApiParamExample("恩核"))
                    )
                )
            }
            JavaSimpleType.DATE.qualifiedName,
            JavaSimpleType.LOCAL_DATE.qualifiedName,
            JavaSimpleType.LOCAL_DATE_TIME.qualifiedName,
            JavaSimpleType.LOCAL_TIME.qualifiedName -> returnApiParams.apply {
                this += ApiParam(
                    name = "data",
                    type = LangDataType.OBJECT,
                    where = ApiParamWhere.RETURN,
                    required = true,
                    description = "业务响应数据",
                    example = ApiParamExample(JsonObject()),
                    children = listOf(
                        ApiParam(name = "value", type = LangDataType.TIMESTAMP, where = ApiParamWhere.RETURN, required = true, description = "时间戳", example = ApiParamExample(System.currentTimeMillis()))
                    )
                )
            }
            else -> returnApiParams.apply {
                this += ApiParam(
                    name = "data",
                    type = LangDataType.OBJECT,
                    where = ApiParamWhere.RETURN,
                    required = true,
                    description = "业务响应数据",
                    example = ApiParamExample(JsonObject()),
                    children = PsiClassTypeApiExtractor.extractApiParam(project = project, psiClassType = psiType as PsiClassType, paramWhere = ApiParamWhere.URL)
                )
            }
        }
    }
}