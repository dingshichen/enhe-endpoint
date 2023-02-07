// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-02-07

package com.enhe.endpoint.database.model

import com.enhe.endpoint.database.MySQLReservedWord
import com.enhe.endpoint.database.MysqlColumnType

interface EColumn

data class EFColumn(
    val name: String,
    val type: MysqlColumnType,
    val nullable: Boolean,
    val comment: String,
    val isPrimaryKey: Boolean,
    val isAutoIncrement: Boolean,
    val isLeftPrimaryKey: Boolean,
) : EColumn {
    /**
     * 包装保留字
     */
    fun getWrapName() = MySQLReservedWord.wrapReservedWord(name)

    override fun toString() = name
}

class EColumnOption : EColumn {

    override fun toString() = "<选择主键>"
}



