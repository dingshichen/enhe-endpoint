// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-08

package com.enhe.endpoint.dialog

import com.enhe.endpoint.MYBATIS_GENERATOR
import com.enhe.endpoint.database.EFColumn
import com.enhe.endpoint.database.EFTable
import com.enhe.endpoint.psi.ModuleItem
import com.enhe.endpoint.psi.getModules
import com.enhe.endpoint.ui.MybatisGeneratorForm
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.Action
import javax.swing.JComponent

class MybatisGeneratorDialog(
    private val project: Project,
    private val table: EFTable
) : DialogWrapper(true) {

    private lateinit var modules: List<Module>

    private lateinit var form: MybatisGeneratorForm

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

    fun getTableId(): EFColumn? = form.selectedTableId

    fun isEnableControlService(): Boolean = form.isEnableControlService

    fun getModule(): Module = form.selectedModuleItem.module

    fun getPersistentModule(): Module = form.selectedPersistentModuleItem.module

    fun getControlModule(): Module = form.selectedControlModuleItem.module

    fun getClientModule(): Module = form.selectedClientModuleItem.module

    fun getServiceImplModule(): Module = form.selectedServiceImplModuleItem.module

    fun getEntityName(): String = form.entityName

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