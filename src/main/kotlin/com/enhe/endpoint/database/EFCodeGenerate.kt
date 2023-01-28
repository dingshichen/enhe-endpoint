// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint.database

import com.enhe.endpoint.extend.findStringFieldRealValue
import com.enhe.endpoint.extend.lowerCamel
import com.enhe.endpoint.extend.or
import com.enhe.endpoint.extend.replaceFirstToEmpty
import com.enhe.endpoint.util.PluginVersionUtil
import com.enhe.endpoint.util.SerialVersionUtil
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.GlobalSearchScope

interface EFCodeGenerateService {

    companion object {

        @JvmStatic
        fun getInstance(project: Project): EFCodeGenerateService = project.getService(EFCodeGenerateService::class.java)
    }

    fun executeGenerateEntity(project: Project,
        directory: PsiDirectory,
        table: EFTable,
        tableId: EFColumn?,
        entityPackageName: String,
        entityName: String
    )

    fun executeGenerateMapper(
        project: Project,
        directory: PsiDirectory,
        mapperPackageName: String,
        entityPackageName: String,
        entityName: String,
        mapperName: String
    )

    fun executeGenerateXml(
        project: Project,
        entityPackageName: String,
        entityName: String,
        mapperPackageName: String,
        mapperName: String,
        directory: PsiDirectory,
        table: EFTable,
        tableId: EFColumn?
    )

    fun executeGenerateClient(
        project: Project,
        module: Module,
        clientPackageName: String,
        clientName: String,
        directory: PsiDirectory,
        table: EFTable,
    )

    fun executeGenerateServiceImpl(
        project: Project,
        serviceImplPackageName: String,
        serviceImplName: String,
        clientPackageName: String,
        clientName: String,
        mapperPackageName: String,
        mapperName: String,
        directory: PsiDirectory,
    )

    fun executeGenerateController(
        project: Project,
        controllerPackageName: String,
        controllerName: String,
        clientPackageName: String,
        clientName: String,
        directory: PsiDirectory,
        table: EFTable,
    )
}

/**
 * 使用 PSI 的能力来生成
 */
class EFCodeGenerateServiceImpl : EFCodeGenerateService {

    override fun executeGenerateEntity(
        project: Project,
        directory: PsiDirectory,
        table: EFTable,
        tableId: EFColumn?,
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
                val columnAnnotationText = if (column.name == tableId?.name) {
                    "@com.baomidou.mybatisplus.annotation.TableId(value = \"${column.name}\", type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)"
                } else "@com.baomidou.mybatisplus.annotation.TableField(\"${column.name}\")"
                val field = JavaPsiFacade.getInstance(project).parserFacade.createFieldFromText(
                    """
                                    /**
                                     * ${column.comment}
                                     */
                                    $columnAnnotationText 
                                    private ${column.type.toJavaType().canonicalName} ${if (column.name == tableId?.name) "id" else column.name.lowerCamel()};
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

    override fun executeGenerateMapper(
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
             * <br> Created By Enhe-Endpoint ${PluginVersionUtil.getVersion()}
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

    override fun executeGenerateXml(
        project: Project,
        entityPackageName: String,
        entityName: String,
        mapperPackageName: String,
        mapperName: String,
        directory: PsiDirectory,
        table: EFTable,
        tableId: EFColumn?
    ) {

        val idTag = tableId?.let { "<id column=\"${it.getWrapName()}\" property=\"id\"/>" }.orEmpty()
        val resultTag = buildString {
            table.columns.filter { it.name != tableId?.name }.forEach {
                this.append("<result column=\"${it.getWrapName()}\" property=\"${it.name.lowerCamel()}\"/>\n        ")
            }
        }.replaceFirstToEmpty("\n")
        val columnTag = table.columns.joinToString(", ") { it.getWrapName() }
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

//        val reformattedFile = CodeStyleManager.getInstance(project).reformat(psiFile)
        directory.add(psiFile)
    }

    override fun executeGenerateClient(
        project: Project,
        module: Module,
        clientPackageName: String,
        clientName: String,
        directory: PsiDirectory,
        table: EFTable
    ) {
        // 前缀、路径
        val moduleApiClass = findModuleDefinitionApiClass(project, module, clientPackageName)
        val serviceNamePrefix = moduleApiClass?.let { "${it.qualifiedName}.SERVER_NAME + " }.or("/* TODO 未找到规则匹配的模块 API 定义 */ ")
        val apiPrefix = moduleApiClass?.let { "${it.qualifiedName}.API_PREFIX + " }.or("/* TODO 未找到规则匹配的模块 API 定义 */ ")
        val clientPath = getClientPath(moduleApiClass, table)
        val controllerText = """
            package $clientPackageName;

            /**
             * <br> Created By Enhe-Endpoint ${PluginVersionUtil.getVersion()}
             */
            @org.springframework.cloud.openfeign.FeignClient(value = $serviceNamePrefix$clientName.URL, primary = false)
            public interface $clientName {

                String URL = $apiPrefix"$clientPath";

            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("$clientName.${JavaFileType.INSTANCE.defaultExtension}", JavaLanguage.INSTANCE, controllerText)

        val shortenedFile = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformattedFile = CodeStyleManager.getInstance(project).reformat(shortenedFile)
        directory.add(reformattedFile)
    }

    override fun executeGenerateServiceImpl(
        project: Project,
        serviceImplPackageName: String,
        serviceImplName: String,
        clientPackageName: String,
        clientName: String,
        mapperPackageName: String,
        mapperName: String,
        directory: PsiDirectory
    ) {
        val serviceImplText = """
            package $serviceImplPackageName;

            /**
             * <br> Created By Enhe-Endpoint ${PluginVersionUtil.getVersion()}
             */
            @lombok.extern.slf4j.Slf4j
            @org.springframework.context.annotation.Primary
            @org.springframework.stereotype.Service
            public class $serviceImplName implements $clientPackageName.$clientName {

                @org.springframework.beans.factory.annotation.Autowired
                private $mapperPackageName.$mapperName ${mapperName.lowerCamel()};
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("$serviceImplName.${JavaFileType.INSTANCE.defaultExtension}", JavaLanguage.INSTANCE, serviceImplText)

        val shortenedFile = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformattedFile = CodeStyleManager.getInstance(project).reformat(shortenedFile)
        directory.add(reformattedFile)
    }

    override fun executeGenerateController(
        project: Project,
        controllerPackageName: String,
        controllerName: String,
        clientPackageName: String,
        clientName: String,
        directory: PsiDirectory,
        table: EFTable,
    ) {
        // 表注释截取作为接口注释
        val apiDesc = if (table.comment.endsWith("表")) table.comment.substring(0, table.comment.length - 1) else table.comment
        val controllerText = """
            package $controllerPackageName;

            /**
             * <br> Created By Enhe-Endpoint ${PluginVersionUtil.getVersion()}
             */
            @com.enhe.core.secure.annotation.PreAuth
            @com.github.xiaoymin.knife4j.annotations.ApiSupport(order = com.enhe.core.api.controller.ClientConstants.API_SORT)
            @io.swagger.annotations.Api(value = "$apiDesc", tags = "$apiDesc")
            @com.enhe.core.cloud.common.client.annotation.FeignController($clientPackageName.$clientName.URL)
            public interface $controllerName extends $clientPackageName.$clientName {

            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText("$controllerName.${JavaFileType.INSTANCE.defaultExtension}", JavaLanguage.INSTANCE, controllerText)

        val shortenedFile = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformattedFile = CodeStyleManager.getInstance(project).reformat(shortenedFile)
        directory.add(reformattedFile)
    }

    /**
     * 查找模块里定义 API 的那个类 TODO 后续改造 EF 框架，利用申明的符号精确查找
     */
    private fun findModuleDefinitionApiClass(project: Project, module: Module, clientPackageName: String): PsiClass? {
        // 1 通过全限定名直接查找
        val packageName = clientPackageName.run { substring(0, lastIndexOf(".")) }
        val className = module.name.run { first().uppercase() + substring(1, indexOf(".")) + "Api" }
        val psiClass = JavaPsiFacade.getInstance(project)
            .findClass("$packageName.$className", GlobalSearchScope.moduleScope(module))
        if (psiClass != null) {
            return psiClass
        }
//        val psiElements = PsiSearchHelper.getInstance(project)
//            .findCommentsContainingIdentifier("SERVER_NAME", GlobalSearchScope.moduleScope(module))
//        val find = psiElements.find {
//            it.parent.parent is PsiClass
//        }
//        if (find != null) {
//            return find.parent.parent as PsiClass
//        }
        return null
    }

    /**
     * 获取控制路由
     */
    private fun getClientPath(moduleApiClass: PsiClass?, table: EFTable): String {
        val apiPrefixValue = moduleApiClass?.findStringFieldRealValue("API_PREFIX").orEmpty()
        return if (apiPrefixValue.isEmpty()) {
            table.getPath()
        } else {
            if (apiPrefixValue.startsWith("/api/")) {
                table.getPath().replaceFirstToEmpty(apiPrefixValue.replaceFirstToEmpty("/api"))
            } else {
                table.getPath().replaceFirstToEmpty(apiPrefixValue)
            }
        }
    }
}