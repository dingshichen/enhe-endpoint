// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-30

package com.enhe.endpoint.database

import com.enhe.endpoint.consts.PO_BASE
import com.enhe.endpoint.consts.PO_SIMPLE
import com.enhe.endpoint.consts.PO_STABLE

sealed interface EFEntityBaseField {

    fun getName(): String

    fun getColumnName(): String

    fun getJavaType(): JavaMapperType
}

object CreateUserId : EFEntityBaseField {

    override fun getName() = "createUserId"

    override fun getColumnName() = "crt_usr_id"

    override fun getJavaType() = JavaMapperType.LONG
}

object CreateTime : EFEntityBaseField {

    override fun getName() = "createTime"

    override fun getColumnName() = "crt_tm"

    override fun getJavaType() = JavaMapperType.DATE
}

object LatestUpdateUserId : EFEntityBaseField {

    override fun getName() = "latestUpdateUserId"

    override fun getColumnName() = "latest_update_usr_id"

    override fun getJavaType() = JavaMapperType.LONG
}

object LatestUpdateTime : EFEntityBaseField {

    override fun getName() = "latestUpdateTime"

    override fun getColumnName() = "latest_update_tm"

    override fun getJavaType() = JavaMapperType.DATE
}

object IsDeleted : EFEntityBaseField {

    override fun getName() = "isDeleted"

    override fun getColumnName() = "is_deleted"

    override fun getJavaType() = JavaMapperType.INTEGER
}

fun EFEntityBaseField.isSuper(column: EFColumn): Boolean {
    return column.name == getColumnName() && column.type.toJavaType() == getJavaType()
}

fun EFTable.getEFEntityBase(): String? {
    val createUserId = columns.any { CreateUserId.isSuper(it) }
    val createTime = columns.any { CreateTime.isSuper(it) }
    val latestUpdateUserId = columns.any { LatestUpdateUserId.isSuper(it) }
    val latestUpdateTime = columns.any { LatestUpdateTime.isSuper(it) }
    val isDeleted = columns.any { IsDeleted.isSuper(it) }
    return if (createUserId && createTime && latestUpdateUserId && latestUpdateTime && isDeleted) {
        PO_SIMPLE
    } else if (createUserId && createTime && latestUpdateUserId && latestUpdateTime) {
        PO_BASE
    } else if (createUserId && createTime) {
        PO_STABLE
    } else null
}