// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.model

/**
 * API 接口参数
 */
data class ApiParam(
    // 字段名称
    val name: String,
    // 字段类型
    val type: LangDataType,
    // 参数位置
    var where: ApiParamWhere,
    // 是否必须，1：是，0：否
    val required: Boolean,
    // 描述
    var description: String? = null,
    // 示例值，示例值的类型随字段类型而变化
    var example: ApiParamExample,
    // 父节点
    var parentId: String? = null,
    // 子节点
    var children: List<ApiParam>? = null,
) {

    val requiredText: String
        get() {
            return if (required) "true" else ""
        }

    /**
     * 获取示例值
     */
    fun getExampleText() = if (type.isBaseType) getExample(this).toString() else ""
}