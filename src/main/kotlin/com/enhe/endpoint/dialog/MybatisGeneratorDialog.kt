// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-08

package com.enhe.endpoint.dialog

import com.enhe.endpoint.consts.MYBATIS_GENERATOR
import com.enhe.endpoint.database.model.ControlServiceState
import com.enhe.endpoint.database.model.EFTable
import com.enhe.endpoint.database.model.ImplTempState
import com.enhe.endpoint.database.model.PersistentState
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
        val persistent = getPersistentState()
        if (!entityNamePattern.matcher(persistent.entityName).matches()) {
            result += ValidationInfo("Entity 命名必须是以 Entity 结尾的大驼峰格式", form.entityName)
        }
        if (!packagePattern.matcher(persistent.entityPackageName).matches()) {
            result += ValidationInfo("Entity 包目录不符合规范", form.entityPackage)
        }
        if (!packagePattern.matcher(persistent.mapperPackageName).matches()) {
            result += ValidationInfo("Mapper 目录不符合规范", form.mapperPackage)
        }
        if (isEnableControlService()) {
            val controlService = getControlServiceState()
            if (!packagePattern.matcher(controlService.controlPackageName).matches()) {
                result += ValidationInfo("Controller 包目录不符合规范", form.controlPackage)
            }
            if (!packagePattern.matcher(controlService.clientPackageName).matches()) {
                result += ValidationInfo("Client 包目录不符合规范", form.clientPackage)
            }
            if (!packagePattern.matcher(controlService.serviceImplPackageName).matches()) {
                result += ValidationInfo("ServiceImpl 包目录不符合规范", form.serviceImplPackage)
            }
            val implTemp = getImplTempState()
            if (persistent.tableId == null && implTemp.enable) {
                if (implTemp.enableLoad) {
                    result += ValidationInfo("MybatisPlus 无设置主键情况下暂不能实现此模版接口", form.loadCheckBox)
                }
                if (implTemp.enableFill) {
                    result += ValidationInfo("MybatisPlus 无设置主键情况下暂不能实现此模版接口", form.fillCheckBox)
                }
                if (implTemp.enableSelect) {
                    result += ValidationInfo("MybatisPlus 无设置主键情况下暂不能实现此模版接口", form.selectCheckBox)
                }
                if (implTemp.enableInsert) {
                    result += ValidationInfo("MybatisPlus 无设置主键情况下暂不能实现此模版接口", form.insertCheckBox)
                }
                if (implTemp.enableDelete) {
                    result += ValidationInfo("MybatisPlus 无设置主键情况下暂不能实现此模版接口", form.deleteCheckBox)
                }
                if (implTemp.enableUpdate) {
                    result += ValidationInfo("MybatisPlus 无设置主键情况下暂不能实现此模版接口", form.updateCheckBox)
                }
            }
        }
        return result
    }

    fun isEnableControlService(): Boolean = form.isEnableControlService

    fun getPersistentState(): PersistentState = form.persistentState

    fun getControlServiceState(): ControlServiceState = form.controlServiceState

    fun getImplTempState(): ImplTempState = form.implTempState

    private fun servicesModules(): List<ModuleItem> {
        return modules
            .map { ModuleItem(it) }
            .sortedBy { it.toString() }

    }
}