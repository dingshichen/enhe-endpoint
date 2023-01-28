// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-08

package com.enhe.endpoint.dialog

import com.enhe.endpoint.MYBATIS_GENERATOR
import com.enhe.endpoint.database.EFColumn
import com.enhe.endpoint.database.EFTable
import com.enhe.endpoint.extend.ModuleItem
import com.enhe.endpoint.extend.getModules
import com.enhe.endpoint.ui.MybatisGeneratorForm
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import java.util.regex.Pattern
import javax.swing.Action
import javax.swing.JComponent

class MybatisGeneratorDialog(
    private val project: Project,
    private val table: EFTable
) : DialogWrapper(true) {

    private lateinit var modules: List<Module>

    private lateinit var form: MybatisGeneratorForm

    private val entityNamePattern: Pattern = Pattern.compile("[A-Z][A-Za-z]*Entity")
    private val packagePattern: Pattern = Pattern.compile("[a-z][a-z.]+[a-z]")

    init {
        title = MYBATIS_GENERATOR
        init()
    }

    override fun createCenterPanel(): JComponent? {
        modules = project.getModules().toList()
        form = MybatisGeneratorForm(table, servicesModules())
        return form.root
    }

    override fun createActions(): Array<Action> {
        setOKButtonText("生成")
        setCancelButtonText("取消")
        return arrayOf(okAction, cancelAction)
    }

    override fun doValidateAll(): MutableList<ValidationInfo> {
        val result = mutableListOf<ValidationInfo>()
        if (!entityNamePattern.matcher(getEntityName()).matches()) {
            result += ValidationInfo("Entity 命名必须是以 Entity 结尾的大驼峰格式", form.entityName)
        }
        if (!packagePattern.matcher(getEntityPackageName()).matches()) {
            result += ValidationInfo("Entity 包目录不符合规范", form.entityPackage)
        }
        if (!packagePattern.matcher(getMapperPackageName()).matches()) {
            result += ValidationInfo("Mapper 目录不符合规范", form.mapperPackage)
        }
        if (!packagePattern.matcher(getControlPackageName()).matches()) {
            result += ValidationInfo("Controller 包目录不符合规范", form.controlPackage)
        }
        if (!packagePattern.matcher(getClientPackageName()).matches()) {
            result += ValidationInfo("Client 包目录不符合规范", form.clientPackage)
        }
        if (!packagePattern.matcher(getServiceImplPackageName()).matches()) {
            result += ValidationInfo("ServiceImpl 包目录不符合规范", form.serviceImplPackage)
        }
        return result
    }

    fun getTableId(): EFColumn? = form.selectedTableId

    fun isEnableControlService(): Boolean = form.isEnableControlService

    fun getModule(): Module = form.selectedModuleItem.module

    fun getPersistentModule(): Module = form.selectedPersistentModuleItem.module

    fun getControlModule(): Module = form.selectedControlModuleItem.module

    fun getClientModule(): Module = form.selectedClientModuleItem.module

    fun getServiceImplModule(): Module = form.selectedServiceImplModuleItem.module

    fun getEntityName(): String = form.entityNameText

    fun getEntityPackageName(): String = form.entityPackageName

    fun getMapperPackageName(): String = form.mapperPackageName

    fun getControlPackageName(): String = form.controlPackageName

    fun getClientPackageName(): String = form.clientPackageName

    fun getServiceImplPackageName(): String = form.serviceImplPackageName

    private fun servicesModules(): List<ModuleItem> {
        return modules
            .map { ModuleItem(it) }
            .sortedBy { it.toString() }

    }
}