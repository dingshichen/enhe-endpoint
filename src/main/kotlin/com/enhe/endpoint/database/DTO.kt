// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-07

package com.enhe.endpoint.database

data class EFTable(
    val name: String,
    val comment: String,
    val columns: List<EFColumn>,
) {
    fun getPrimaryKeys() = columns.filter { it.isPrimaryKey }
}

data class EFColumn(
    val name: String,
    val type: MysqlColumnType,
    val nullable: Boolean,
    val comment: String,
    val isPrimaryKey: Boolean,
    val isAutoIncrement: Boolean,
    val isLeftPrimaryKey: Boolean,
) {
    /**
     * 包装保留字
     */
    fun getWrapName() = MySQLReservedWord.wrapReservedWord(name)

    override fun toString() = name
}

/**
 * 表名转换成接口路径
 */
fun EFTable.getPath(): String {
    return "/" + name.replace("_", "/")
}