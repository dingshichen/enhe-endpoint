// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-07

package com.enhe.endpoint.database.model

import com.enhe.endpoint.extend.ModuleItem
import com.enhe.endpoint.extend.replaceToEmpty
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType

data class PersistentState(
    val tableId: EFColumn?,
    val persistentModuleItem: ModuleItem,
    val entityName: String,
    val entityPackageName: String,
    val mapperPackageName: String,
) {
    val persistentModule by lazy { persistentModuleItem.module }
    val baseName by lazy { entityName.replaceToEmpty("Entity") }
    val entityQualified by lazy { "$entityPackageName.$entityName" }
    val entityFileName by lazy { "$entityName.${JavaFileType.INSTANCE.defaultExtension}" }
    val mapperName by lazy { baseName + "Mapper" }
    val mapperQualified by lazy { "$mapperPackageName.$mapperName" }
    val mapperFileName by lazy { "$mapperName.${JavaFileType.INSTANCE.defaultExtension}" }
    val xmlFileName by lazy { "$mapperName.${XmlFileType.INSTANCE.defaultExtension}" }

    fun isId(column: EFColumn) = column.name == tableId?.name
}

data class ControlServiceState(
    val persistent: PersistentState,
    val controlModuleItem: ModuleItem,
    val clientModuleItem: ModuleItem,
    val serviceImplModuleItem: ModuleItem,
    val controlPackageName: String,
    val clientPackageName: String,
    val serviceImplPackageName: String,
) {
    val controlModule by lazy { controlModuleItem.module }
    val controlName by lazy { persistent.baseName + "Controller" }
    val controlQualified by lazy { "$controlPackageName.$controlName" }
    val controlFileName by lazy { "$controlName.${JavaFileType.INSTANCE.defaultExtension}" }

    val clientModule by lazy { clientModuleItem.module }
    val clientName by lazy { persistent.baseName + "Service" }
    val clientQualified by lazy { "$clientPackageName.$clientName" }
    val clientFileName by lazy { "$clientName.${JavaFileType.INSTANCE.defaultExtension}" }

    val serviceImplModule by lazy { serviceImplModuleItem.module }
    val serviceImplName by lazy { persistent.baseName + "ServiceImpl" }
    val serviceImplQualified by lazy { "$serviceImplPackageName.$serviceImplName" }
    val serviceImplFileName by lazy { "$serviceImplName.${JavaFileType.INSTANCE.defaultExtension}" }

    val excelServiceName by lazy { persistent.baseName + "ExcelService" }
    val excelServiceQualified by lazy { "$serviceImplPackageName.$excelServiceName" }
    val excelServiceFileName by lazy { "$excelServiceName.${JavaFileType.INSTANCE.defaultExtension}" }
}

data class ImplTempState(
    val persistent: PersistentState,
    val controlService: ControlServiceState,
    val enable: Boolean,
    val enablePage: Boolean,
    val enableListAll: Boolean,
    val enableSelect: Boolean,
    val enableFill: Boolean,
    val enableLoad: Boolean,
    val enableInsert: Boolean,
    val enableUpdate: Boolean,
    val enableDelete: Boolean,
    val enableImp: Boolean,
    val enableExp: Boolean,
) {

    val beanPackageName by lazy { "com.enhe.dagp.${controlService.clientModuleItem}.bean" }

    val baseBeanQualified by lazy { "$beanPackageName.${persistent.baseName}" }
    val baseBeanFileName by lazy { "${persistent.baseName}.${JavaFileType.INSTANCE.defaultExtension}" }

    val itemName by lazy { persistent.baseName + "Item" }
    val itemQualified by lazy { "$beanPackageName.$itemName" }
    val itemFileName by lazy { "${itemName}.${JavaFileType.INSTANCE.defaultExtension}" }

    val queryName by lazy { persistent.baseName + "Query" }
    val queryQualified by lazy { "$beanPackageName.$queryName" }
    val queryFileName by lazy { "${queryName}.${JavaFileType.INSTANCE.defaultExtension}" }

    val optionName by lazy { persistent.baseName + "Option" }
    val optionQualified by lazy { "$beanPackageName.$optionName" }
    val optionFileName by lazy { "$optionName.${JavaFileType.INSTANCE.defaultExtension}" }

    val selectName by lazy { persistent.baseName + "SelectQuery" }
    val selectQualified by lazy { "$beanPackageName.$selectName" }
    val selectFileName by lazy { "$selectName.${JavaFileType.INSTANCE.defaultExtension}" }

    val impInfoName by lazy { persistent.baseName + "ImpInfo" }
    val impInfoQualified by lazy { "$beanPackageName.$impInfoName" }
    val impInfoFileName by lazy { "$impInfoName.${JavaFileType.INSTANCE.defaultExtension}" }

    val impParamName by lazy { persistent.baseName + "ImpParam" }
    val impParamQualified by lazy { "$beanPackageName.$impParamName" }
    val impParamFileName by lazy { "$impParamName.${JavaFileType.INSTANCE.defaultExtension}" }

    val expInfoName by lazy { persistent.baseName + "ExpInfo" }
    val expInfoQualified by lazy { "$beanPackageName.$expInfoName" }
    val expInfoFileName by lazy { "$expInfoName.${JavaFileType.INSTANCE.defaultExtension}" }

    val excelName by lazy { persistent.baseName + "Excel" }
    val excelQualified by lazy { "$beanPackageName.$excelName" }
    val excelFileName by lazy { "$excelName.${JavaFileType.INSTANCE.defaultExtension}" }

    var needItem = false
    var needQuery = false
    var needBase = false
    var needOption = false
    var needSelectQuery = false
    var needImpInfo = false
    var needImpParam = false
    var needExpInfo = false
    var needExcel = false
}