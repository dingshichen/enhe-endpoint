// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-05

package com.enhe.endpoint.action

import com.enhe.endpoint.database.EFColumn
import com.enhe.endpoint.database.EFTable
import com.enhe.endpoint.database.MysqlColumnType
import com.enhe.endpoint.dialog.MybatisGeneratorDialog
import com.enhe.endpoint.notifier.EnheNotifier
import com.enhe.endpoint.util.PluginVersionUtil
import com.enhe.endpoint.util.SerialVersionUtil
import com.enhe.endpoint.util.toUpperCamelCase
import com.intellij.database.psi.DbTable
import com.intellij.database.types.typeName
import com.intellij.database.util.DasUtil
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.util.PackageUtil
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
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
                val tableId = getTableId()
                // 生成文件名
                val upperCamelCaseName = NameCaseUtils.toUpperCamelCase(table.name)
                val entityName = "${upperCamelCaseName}Entity"
                val mapperName = "${upperCamelCaseName}Mapper"

                val entityPackageName = getEntityPackageName()
                val mapperPackageName = getMapperPackageName()

                val sourceRoots = ModuleRootManager.getInstance(persistentModule).getSourceRoots(JavaSourceRootType.SOURCE)
                if (sourceRoots.isEmpty()) {
                    EnheNotifier.error(project, "获取不到模块内的 Java 资源目录")
                    return
                }
                val psiManager = PsiManager.getInstance(project)
                val sourceDir = psiManager.findDirectory(sourceRoots[0])
                if (sourceDir == null) {
                    EnheNotifier.error(project, "获取不到资源目录")
                    return
                }
                val entityDirectory = PackageUtil.findOrCreateDirectoryForPackage(persistentModule, entityPackageName, sourceDir, false, true)
                if (entityDirectory == null) {
                    EnheNotifier.error(project, "Entity 包目录创建失败")
                    return
                }
                val mapperDirectory = PackageUtil.findOrCreateDirectoryForPackage(persistentModule, mapperPackageName, sourceDir, false, true)
                if (mapperDirectory == null) {
                    EnheNotifier.error(project, "Mapper 包目录创建失败")
                    return
                }
                ApplicationManager.getApplication().runWriteAction {
                    CommandProcessor.getInstance().executeCommand(project, {
                        executeGenerateEntity(project, entityDirectory, table, tableId, entityPackageName, entityName)
                        executeGenerateMapper(project, mapperDirectory, mapperPackageName, entityPackageName, entityName, mapperName)
                        executeGenerateXml(project, entityPackageName, entityName, mapperPackageName, mapperName, mapperDirectory, table, tableId)
                    }, "MybatisPlusGenerate", null)
                }

            }
        }
    }

    private fun executeGenerateEntity(
        project: Project,
        directory: PsiDirectory,
        table: EFTable,
        tableId: EFColumn,
        entityPackageName: String,
        entityName: String
    ) {
        val entityText = """
                    package $entityPackageName;
                    
                    /**
                     * ${table.comment}
                     * <br> Created By Enhe-Endpoint ${PluginVersionUtil.getVersion()}
                     */
                    @lombok.Getter
                    @lombok.Setter
                    @lombok.ToString(callSuper = true)
                    @lombok.experimental.Accessors(chain = true)
                    @lombok.experimental.SuperBuilder
                    @lombok.NoArgsConstructor
                    @lombok.AllArgsConstructor
                    @com.baomidou.mybatisplus.annotation.TableName(value = "${table.name}", resultMap = "defaultResultMap")
                    public class $entityName implements java.io.Serializable {
                    
                        private static final long serialVersionUID = ${SerialVersionUtil.generateUID()}L;
                    
                    }
                """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("$entityName.${JavaFileType.INSTANCE.defaultExtension}", JavaLanguage.INSTANCE, entityText)

        psiFile.children.find { it is PsiClass }?.let {
            table.columns.forEach { column ->
                val field = JavaPsiFacade.getInstance(project).parserFacade.createFieldFromText(
                    """
                                    /**
                                     * ${column.comment}
                                     */
                                     ${if (column.name == tableId.name) "@com.baomidou.mybatisplus.annotation.TableId(value = \"${column.name}\", type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)" else "@com.baomidou.mybatisplus.annotation.TableField(\"${column.name}\")"}
                                    private ${column.type.toJavaType().canonicalName} ${if (column.name == tableId.name) "id" else NameCaseUtils.toCamelCase(column.name)};
                            """.trimIndent(), it
                )
                it.add(field)
            }
        }
        // 格式化
        val shortenedFile = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformattedFile = CodeStyleManager.getInstance(project).reformat(shortenedFile)
        directory.add(reformattedFile)
    }

    private fun executeGenerateMapper(
        project: Project,
        directory: PsiDirectory,
        mapperPackageName: String,
        entityPackageName: String,
        entityName: String,
        mapperName: String
    ) {
        val mapperText = """
            package $mapperPackageName;

            /**
             * Created By Enhe-Endpoint ${PluginVersionUtil.getVersion()}
             */
            public interface $mapperName extends com.enhe.core.mp.mapper.EFMapper<$entityPackageName.$entityName> {
            
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("$mapperName.${JavaFileType.INSTANCE.defaultExtension}", JavaLanguage.INSTANCE, mapperText)

        val shortenedFile = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformattedFile = CodeStyleManager.getInstance(project).reformat(shortenedFile)
        directory.add(reformattedFile)
    }

    private fun executeGenerateXml(
        project: Project,
        entityPackageName: String,
        entityName: String,
        mapperPackageName: String,
        mapperName: String,
        directory: PsiDirectory,
        table: EFTable,
        tableId: EFColumn
    ) {

        val idTag = "<id column=\"${tableId.name}\" jdbcType=\"${tableId.type}\" property=\"id\"/>"
        val resultTag = buildString {
            table.columns.filter { it.name != tableId.name }.forEach {
                this.append("\n<result column=\"${it.name}\" jdbcType=\"${it.type}\" property=\"${NameCaseUtils.toCamelCase(it.name)}\"/>")
            }
        }.replaceFirst("\n", "")
        val columnTag = table.columns.joinToString(", ")
        val xmlText = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
            <!-- Created By Enhe-Endpoint ${PluginVersionUtil.getVersion()} -->
            <mapper namespace="$mapperPackageName.$mapperName">
                <resultMap id="defaultResultMap" type="$entityPackageName.$entityName" autoMapping="true">
                    <!--@Table ${table.name}-->
                    $idTag
                    $resultTag
                </resultMap>
                <sql id="Base_Column_List">
                    $columnTag
                </sql>
            </mapper>
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("$mapperName.${XmlFileType.INSTANCE.defaultExtension}", XMLLanguage.INSTANCE, xmlText)

        val reformattedFile = CodeStyleManager.getInstance(project).reformat(psiFile)
        directory.add(reformattedFile)
    }

}