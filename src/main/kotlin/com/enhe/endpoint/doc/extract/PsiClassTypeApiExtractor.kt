// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-28

package com.enhe.endpoint.doc.extract

import com.enhe.endpoint.consts.JSON_IGNORE
import com.enhe.endpoint.consts.SK_API_PROP
import com.enhe.endpoint.consts.TRANSIENT
import com.enhe.endpoint.doc.LangDataTypeConvertor
import com.enhe.endpoint.doc.mock.LangDataTypeMocker
import com.enhe.endpoint.doc.model.ApiParam
import com.enhe.endpoint.doc.model.ApiParamWhere
import com.enhe.endpoint.doc.model.FieldNode
import com.enhe.endpoint.extend.*
import com.enhe.endpoint.util.ApiStringUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiUtil

/**
 * TODO
 * 1. 解析泛型时，需要更准确的获取到泛型里的内容，有可能是多个泛型
 * 2. 解析 @ApiModelProperty 注解时，需要判断 hidden 属性来过滤
 * 3. 需要解析 @Setter(onMethod_ = {@ApiModelProperty(dataType = Base.CLASS_NAME, required = true)}) @Setter(onMethod_ = {@ApiModelProperty(dataType = Base.LIST_CLASS_NAME, required = true)})
 * 4. 要包装 Result 通用返回值结构
 * 5. 入参是集合，怎么描述参数？
 */
object PsiClassTypeApiExtractor {

    /**
     * 递归查询其属性
     */
    fun extractApiParam(
        project: Project,
        psiClassType: PsiClassType,
        parentField: PsiField? = null,
        childrenFields: Array<PsiField>? = null,
        fieldNode: FieldNode = newFiledNode(psiClassType),
        paramWhere: ApiParamWhere
    ): List<ApiParam> {
        val params = mutableListOf<ApiParam>()
        // 顺序查询父类属性，如果子类有相同名称的属性，把父类的属性给隐藏
        psiClassType.superTypes.filterIsInstance<PsiClassType>()
            .filter {
                !it.isJavaObjectType() && !it.isJavaSerializableType() && !it.isAnyJavaList()
            }.forEach {
                params += extractApiParam(project, it, parentField, childrenFields, fieldNode, paramWhere)
            }
        val psiClass = PsiUtil.resolveClassInClassTypeOnly(psiClassType) ?: return params
        val generics = getGenericsType(psiClass, psiClassType)
        val fields = childrenFields ?: psiClass.fields
        fields.forEach {
            if (it.name == "serialVersionUID" || it.type.isJavaLogType() || it.hasAnnotation(JSON_IGNORE) || it.hasAnnotation(TRANSIENT)) {
                // 序列化 ID || 忽略序列化
                return@forEach
            }
            var fieldType = it.type
            // 如果能获取到对应的真实类型，说明是范型
            generics[fieldType.presentableText]?.let { pt ->
                if ("?" == pt.presentableText) {
                    // 如果是通配符，说明没有此范型所在字段是没有定义的，跳过
                    return@forEach
                }
                // 替换成真实类型
                fieldType = pt
            }
            val fieldName = it.getFieldSerialName()
            val dataType = project.getService(LangDataTypeConvertor::class.java).run {
                convert(fieldType.presentableText)
            }
            val childNode = newFiledNode(fieldType)
            if (childNode.existFromDownToUp(fieldNode)) {
                // 防止无限递归
                return@forEach
            }
            childNode.parentNode = fieldNode
            fieldNode += childNode
            val propertyAn = it.getAnnotation(SK_API_PROP)
            params += ApiParam(
                name = fieldName,
                type = dataType,
                where = paramWhere,
                required = propertyAn.findRequiredAttributeRealValue(),
                description = propertyAn?.findAttributeRealValue("notes"),
                example = LangDataTypeMocker.generateValue(dataType),
                parentId = parentField?.name.orEmpty(),
                children = getChildren(
                    project = project,
                    psiField = it,
                    fieldType = fieldType,
                    generics = generics,
                    parentNode = childNode,
                    paramWhere = paramWhere
                )
            )
        }
        return params
    }

    /**
     * 获取泛型对应关系
     */
    private fun getGenericsType(psiClass: PsiClass, psiType: PsiClassType): Map<String, PsiType> {
        val generics: MutableMap<String, PsiType> = mutableMapOf()
        val typeParameters = psiClass.typeParameters
        val parameters = psiType.parameters
        typeParameters.forEachIndexed { i, psiTypeParameter ->
            if (parameters.size - 1 >= i) {
                psiTypeParameter.name?.let {
                    generics[it] = parameters[i]
                }
            }
        }
        return generics
    }

    /**
     * 获取子节点
     */
    private fun getChildren(
        project: Project,
        psiField: PsiField,
        fieldType: PsiType,
        generics: Map<String, PsiType>,
        parentNode: FieldNode,
        paramWhere: ApiParamWhere,
    ): List<ApiParam>? {
        if (fieldType !is PsiClassType) {
            return null
        }
        if (fieldType.isJavaBaseType() || fieldType.isJavaSimpleType() || fieldType.isJavaBaseList() || fieldType.isJavaSimpleList()) {
            // 基本类型、简单类型，不下钻
            return null
        }
        if (fieldType.hasParameters()) {
                generics[fieldType.parameters[0].presentableText]?.let {
                    PsiUtil.resolveClassInClassTypeOnly(it)?.allFields.let { fields ->
                        return extractApiParam(
                            project = project,
                            psiClassType = fieldType,
                            parentField = psiField,
                            childrenFields = fields,
                            fieldNode = parentNode,
                            paramWhere = paramWhere,
                        )
                    }
                }
            }
        val childrenFields = tryGetCollectionGenericsType(fieldType)
        return extractApiParam(
            project = project,
            psiClassType = fieldType,
            parentField = psiField,
            childrenFields = childrenFields,
            fieldNode = parentNode,
            paramWhere = paramWhere,
        )
    }

    /**
     * 如果类型是 List<T> Set<T>
     */
    private fun tryGetCollectionGenericsType(psiClassType: PsiClassType): Array<PsiField>? {
        if (psiClassType.isJavaGenericList()) {
            psiClassType.parameters.first {
                return PsiUtil.resolveClassInClassTypeOnly(it)?.allFields
            }
        }
        return null
    }


    private fun newFiledNode(psiType: PsiType) = psiType.presentableText.run {
        val type = if (ApiStringUtil.isJavaGenericCollection(this)) ApiStringUtil.subJavaGeneric(this) else this
        FieldNode(type)
    }
}