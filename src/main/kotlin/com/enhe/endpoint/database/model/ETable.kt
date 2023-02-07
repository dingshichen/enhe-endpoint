// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-02-07

package com.enhe.endpoint.database.model

interface ETable

data class EFTable(
    val name: String,
    val comment: String,
    val columns: List<EFColumn>,
) : ETable {
    fun getPrimaryKeys() = columns.filter { it.isPrimaryKey }

    /**
     * 表名转换成接口路径
     */
    fun getPath() = "/" + name.replace("_", "/")

    fun getCommentWithoutSuffix() = if (comment.endsWith("表")) comment.substring(0, comment.length - 1) else comment
}