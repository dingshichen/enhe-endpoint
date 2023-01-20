// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-05

package com.enhe.endpoint.action

import com.enhe.endpoint.database.EFCodeGenerateService
import com.enhe.endpoint.database.EFColumn
import com.enhe.endpoint.database.EFTable
import com.enhe.endpoint.database.MysqlColumnType
import com.enhe.endpoint.dialog.MybatisGeneratorDialog
import com.enhe.endpoint.notifier.EnheNotifier
import com.enhe.endpoint.util.toUpperCamelCase
import com.intellij.database.psi.DbTable
import com.intellij.database.types.typeName
import com.intellij.database.util.DasUtil
import com.intellij.ide.util.PackageUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.webSymbols.utils.NameCaseUtils
import org.jetbrains.jps.model.java.JavaSourceRootType

/**
 * Mybatis 生成工具按钮
 */
class MybatisGenerateAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val table = e.getData(LangDataKeys.PSI_ELEMENT) ?: return
        when (table) {
            is DbTable -> {
                val resolveObjects = DasUtil.getPrimaryKey(table)?.columnsRef?.resolveObjects()
                DasUtil.getColumns(table).map {
                    EFColumn(
                        it.name,
                        MysqlColumnType.of(it.dasType.typeName),
                        it.isNotNull,
                        it.comment.orEmpty(),
                        resolveObjects?.any { pk -> pk.name == it.name } ?: false,
                        it.toString().contains("auto_increment"),
                        resolveObjects?.first()?.name == it.name
                    )
                }.toList().run {
                    showGeneratorDialog(project, EFTable(table.name, table.comment.orEmpty(), this))
                }
            }
        }
    }

    private fun showGeneratorDialog(project: Project, table: EFTable) {
        MybatisGeneratorDialog(project, table).apply {
            if (showAndGet()) {
                val persistentModule = getPersistentModule()

                val entityPackageName = getEntityPackageName()
                val mapperPackageName = getMapperPackageName()

                val sourceDir = findSourceDir(project, persistentModule) ?: return
                val entityDir = findOrCreateDir(project, persistentModule, entityPackageName, sourceDir) ?: return
                val mapperDir = findOrCreateDir(project, persistentModule, mapperPackageName, sourceDir) ?: return

                val tableId: EFColumn? = getTableId()

                val upperCamelCaseName = NameCaseUtils.toUpperCamelCase(table.name)
                val entityName = "${upperCamelCaseName}Entity"
                val mapperName = "${upperCamelCaseName}Mapper"

                ApplicationManager.getApplication().runWriteAction {
                    CommandProcessor.getInstance().executeCommand(project, {
                        EFCodeGenerateService.getInstance(project).run {
                            executeGenerateEntity(project, entityDir, table, tableId, entityPackageName, entityName)
                            executeGenerateMapper(project, mapperDir, mapperPackageName, entityPackageName, entityName, mapperName)
                            executeGenerateXml(project, entityPackageName, entityName, mapperPackageName, mapperName, mapperDir, table, tableId)
                        }
                    }, "MybatisPlusGeneratePersistent", null)
                }

                if (isEnableControlService()) {
                    val controlModule = getControlModule()
                    val clientModule = getClientModule()
                    val serviceImplModule = getServiceImplModule()

                    val controlPackageName = getControlPackageName()
                    val clientPackageName = getClientPackageName()
                    val serviceImplPackageName = getServiceImplPackageName()

                    val controlSourceDir = findSourceDir(project, controlModule) ?: return
                    val clientSourceDir = findSourceDir(project, clientModule) ?: return
                    val serviceImplSourceDir = findSourceDir(project, serviceImplModule) ?: return

                    val controlDir = findOrCreateDir(project, controlModule, controlPackageName, controlSourceDir) ?: return
                    val clientDir = findOrCreateDir(project, clientModule, clientPackageName, clientSourceDir) ?: return
                    val serviceImplDir = findOrCreateDir(project, serviceImplModule, serviceImplPackageName, serviceImplSourceDir) ?: return

                    val controlName = "${upperCamelCaseName}Controller"
                    val clientName = "${upperCamelCaseName}Service"
                    val serviceImplName = "${upperCamelCaseName}ServiceImpl"

                    ApplicationManager.getApplication().runWriteAction {
                        CommandProcessor.getInstance().executeCommand(project, {
                            EFCodeGenerateService.getInstance(project).run {
                                executeGenerateClient(project, clientModule, clientPackageName, clientName, clientDir, table)
                                executeGenerateServiceImpl(project, serviceImplPackageName, serviceImplName,
                                    clientPackageName, clientName, mapperPackageName, mapperName, serviceImplDir)
                                executeGenerateController(project, controlPackageName, controlName, clientPackageName, clientName, controlDir, table)
                            }
                        }, "MybatisPlusGenerateControlService", null)
                    }
                }
            }
        }
    }

    /**
     * 获取模块的资源目录
     */
    private fun findSourceDir(project: Project, persistentModule: Module): PsiDirectory? {
        val sourceRoots = ModuleRootManager.getInstance(persistentModule).getSourceRoots(JavaSourceRootType.SOURCE)
        if (sourceRoots.isEmpty()) {
            EnheNotifier.error(project, "获取不到模块内的 Java 资源目录")
            return null
        }
        val psiManager = PsiManager.getInstance(project)
        val sourceDir = psiManager.findDirectory(sourceRoots[0])
        if (sourceDir == null) {
            EnheNotifier.error(project, "获取不到资源目录")
        }
        return sourceDir
    }

    /**
     * 寻找或创建目录
     */
    private fun findOrCreateDir(project: Project, module: Module, packageName: String, baseDir: PsiDirectory): PsiDirectory? {
        val dir = PackageUtil.findOrCreateDirectoryForPackage(module, packageName, baseDir,
            false, true)
        if (dir == null) {
            EnheNotifier.error(project, "$packageName 包目录创建失败")
        }
        return dir
    }



}