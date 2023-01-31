// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-28

package com.enhe.endpoint.database

import com.enhe.endpoint.consts.*
import com.enhe.endpoint.extend.*
import com.enhe.endpoint.util.PluginVersionUtil
import com.enhe.endpoint.util.SerialVersionUtil
import com.intellij.ide.util.PackageUtil
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
import com.intellij.psi.util.PsiTreeUtil

/**
 * 使用 PSI 的能力来生成
 */
class EFCodeGenerateServiceImpl : EFCodeGenerateService {

    private val extendsPlaceholder = "%r%"

    override fun executeGenerateEntity(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        persistent: PersistentState,
        implTemp: ImplTempState
    ) {
        // 这里修正 PSI 整理导入的问题，手动补全
        val fixImports = if (implTemp.enable) {
            val imports = StringBuilder("")
            if (implTemp.enablePage || implTemp.enableListAll) {
                imports.append("import ${implTemp.itemQualified};")
            }
            if (implTemp.enableSelect || implTemp.enableFill) {
                imports.appendLine().append("import ${implTemp.optionQualified};")
            }
            if (implTemp.enableLoad || implTemp.enableInsert || implTemp.enableUpdate) {
                imports.appendLine().append("import ${implTemp.baseBeanQualified};")
            }
            imports.toString()
        } else ""
        // 如果表拥有创建属性和修改属性以及软删属性，可以继承封装好的基类
        val efEntityBase = table.getEFEntityBase()
        val extends = if (efEntityBase == null) "implements $IO_SERIAL" else "extends $efEntityBase"
        val entityText = """
            package ${persistent.entityPackageName};
            $fixImports
            /**
             * ${table.comment}
             * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
             */
            @$LB_GETTER
            @$LB_SETTER
            @$LB_TOSTRING(callSuper = true)
            @$LB_ACCS(chain = true)
            @$LB_SB
            @$LB_NAC
            @$LB_AAC
            @$MP_TABLE_NAME(value = "${table.name}", resultMap = "defaultResultMap")
            public class ${persistent.entityName} $extends{
                $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(persistent.entityFileName, JavaLanguage.INSTANCE, entityText)
        PsiTreeUtil.getChildOfType(psiFile, PsiClass::class.java)?.let {
            val parser = JavaPsiFacade.getInstance(project).parserFacade
            // 过滤可继承字段
            val columns: List<EFColumn> = when (efEntityBase) {
                PO_STABLE -> table.columns.filterNot { c -> CreateUserId.isSuper(c) || CreateTime.isSuper(c) }
                PO_BASE -> table.columns.filterNot { c -> CreateUserId.isSuper(c) || CreateTime.isSuper(c) || LatestUpdateUserId.isSuper(c) || LatestUpdateTime.isSuper(c) }
                PO_SIMPLE -> table.columns.filterNot { c -> CreateUserId.isSuper(c) || CreateTime.isSuper(c) || LatestUpdateUserId.isSuper(c) || LatestUpdateTime.isSuper(c) || IsDeleted.isSuper(c) }
                else -> table.columns
            }
            columns.forEach { column ->
                val columnAnnotationText = if (persistent.isId(column)) {
                    "@$MP_TABLE_ID(value = \"${column.name}\", type = $MP_TABLE_ID_TYPE_ASS)"
                } else "@$MP_TABLE_FIELD(\"${column.name}\")"
                parser.addFieldFromText(
                    """
                        /**
                         * ${column.comment}
                         */
                        $columnAnnotationText 
                        private ${column.type.toJavaType().canonicalName} ${if (persistent.isId(column)) "id" else column.name.lowerCamel()};
                    """.trimIndent(), it)
            }
            // 转换函数
            if (implTemp.enable) {
                with(implTemp) {
                    if (enablePage || enableListAll) {
                        parser.addFieldFromText("""
                            public static final $FUN<${persistent.entityQualified}, $itemName> item = $BEAN_UTIL.to(${persistent.entityQualified}.class, $itemName.class);
                        """.trimIndent(), it)
                    }
                    if (enableLoad || enableFill) {
                        parser.addFieldFromText("""
                            public static final $FUN<${persistent.entityQualified}, $optionName> option = $BEAN_UTIL.to(${persistent.entityQualified}.class, $optionName.class);
                        """.trimIndent(), it)
                    }
                    if (enableLoad || enableInsert || enableUpdate) {
                        parser.addFieldFromText("""
                            public static final $FUN<${persistent.entityQualified}, ${persistent.baseName}> to = $BEAN_UTIL.to(${persistent.entityQualified}.class, ${persistent.baseName}.class);
                        """.trimIndent(), it)
                    }
                    if (enableInsert || enableUpdate) {
                        parser.addFieldFromText("""
                            public static final $FUN<${persistent.baseName}, ${persistent.entityQualified}> from = $BEAN_UTIL.from(${persistent.entityQualified}.class, ${persistent.baseName}.class);
                        """.trimIndent(), it)
                    }
                }
            }
        }
        // 格式化
        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    override fun executeGenerateMapper(
        project: Project,
        dir: PsiDirectory,
        persistent: PersistentState,
        implTemp: ImplTempState
    ) {
        // 这里修正 PSI 整理导入的问题，手动补全
        val fixImports = if (implTemp.enable) {
            val imports = StringBuilder("")
            if (implTemp.enablePage || implTemp.enableListAll) {
                imports.append("import ${implTemp.queryQualified};")
            }
            if (implTemp.enableSelect) {
                imports.appendLine().append("import ${implTemp.selectQualified};")
            }
            imports.toString()
        } else ""
        val text = """
            package ${persistent.mapperPackageName};
            $fixImports
            /**
             * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
             */
            @$REPO
            public interface ${persistent.mapperName} extends $EF_MAPPER<${persistent.entityQualified}> {
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(persistent.mapperFileName, JavaLanguage.INSTANCE, text)
        // 生成接口方法，与 XML 对应
        if (implTemp.enable) {
            PsiTreeUtil.getChildOfType(psiFile, PsiClass::class.java)?.let {
                val parser = JavaPsiFacade.getInstance(project).parserFacade
                with(implTemp) {
                    if (enablePage) {
                        parser.addMethodFromText("""
                            int countByQuery(@$MB_PARAM("query") $queryName query);
                        """.trimIndent(), it)
                        parser.addMethodFromText("""
                            $MP_IPAGE<${persistent.entityQualified}> selectByQuery(@$MB_PARAM("ipage") $MP_IPAGE<${persistent.entityQualified}> ipage, 
                                                                                   @$MB_PARAM("query") $queryName query);
                        """.trimIndent(), it)
                    }
                    if (enableListAll) {
                        parser.addMethodFromText("""
                            $LIST<${persistent.entityQualified}> selectAllByQuery(@$MB_PARAM("query") $queryName query);
                        """.trimIndent(), it)
                    }
                    if (enableSelect) {
                        parser.addMethodFromText("""
                            $LIST<${persistent.entityQualified}> selectBySelectQuery(@$MB_PARAM("query") $selectName query);
                        """.trimIndent(), it)
                    }
                    if (enableLoad) {
                        parser.addMethodFromText("""
                            int countInIds(@$MB_PARAM("ids") $LIST<Long> ids);
                        """.trimIndent(), it)
                    }
                }
            }
        }
        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    override fun executeGenerateXml(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        persistent: PersistentState,
        implTemp: ImplTempState
    ) {
        // pk 字段
        val idTag = persistent.tableId?.let { "<id column=\"${it.getWrapName()}\" property=\"id\"/>" }.orEmpty()
        // 结果集映射
        val resultTag = buildString {
            table.columns.filter { !persistent.isId(it) }.forEach {
                this.append("<result column=\"${it.getWrapName()}\" property=\"${it.name.lowerCamel()}\"/>\n        ")
            }
        }.replaceFirstToEmpty("\n")
        // 全字段 SQL
        val columnTag = table.columns.joinToString(", ") { it.getWrapName() }
        // 模版方法
        val methodText = getXmlMethodByImplTemp(table, implTemp)
        val xmlText = """
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- $CREATED_BY ${PluginVersionUtil.getVersion()} -->
<mapper namespace="${persistent.mapperQualified}">
    <resultMap id="defaultResultMap" type="${persistent.entityQualified}" autoMapping="true">
        <!--@Table ${table.name}-->
        $idTag
        $resultTag
    </resultMap>
    <sql id="Base_Column_List">
        $columnTag
    </sql>
$methodText
</mapper>
        """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(persistent.xmlFileName, XMLLanguage.INSTANCE, xmlText)
//        val reformatted = CodeStyleManager.getInstance(project).reformat(psiFile)
        dir.add(psiFile)
    }

    private fun getXmlMethodByImplTemp(table: EFTable, implTempState: ImplTempState): String {
        val text = StringBuilder("")
        if (!implTempState.enable) {
            return text.toString()
        }
        with(implTempState) {
            if (enablePage) {
                text.appendLine().append("""
    <select id="countByQuery" resultType="int">
        select count(*) from ${table.name}
        <where>
            <!-- TODO -->
        </where>
    </select>

    <select id="selectByQuery" resultMap="defaultResultMap">
        select <include refid="Base_Column_List" /> from ${table.name}
        <where>
            <!-- TODO -->
        </where>
    </select>
                """.trimIndent())
            }
            if (enableListAll) {
                text.appendLine().appendLine().append("""
    <select id="selectAllByQuery" resultMap="defaultResultMap">
        select <include refid="Base_Column_List" /> from ${table.name}
        <where>
            <!-- TODO -->
        </where>
    </select>
                """.trimIndent())
            }
            if (enableSelect) {
                text.appendLine().appendLine().append("""
    <select id="selectBySelectQuery" resultMap="defaultResultMap">
        select <include refid="Base_Column_List" /> from ${table.name}
        <where>
            <!-- TODO -->
        </where>
    </select>
                """.trimIndent())
            }
            if (enableLoad) {
                text.appendLine().appendLine().append("""
    <select id="countInIds" resultType="int">
        select count(*) from ${table.name} where ${persistent.tableId!!.name} in <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
    </select>
                """.trimIndent())
            }
        }
        return text.toString()
    }

    override fun executeGenerateClient(
        project: Project,
        sourceDirectory: PsiDirectory,
        dir: PsiDirectory,
        table: EFTable,
        controlService: ControlServiceState,
        implTemp: ImplTempState
    ) {
        // 前缀、路径
        val moduleApiClass = findModuleDefinitionApiClass(project, controlService.clientModule, controlService.clientPackageName)
        val serviceNamePrefix = moduleApiClass?.let { "${it.qualifiedName}.SERVER_NAME + " }.or("/* TODO 未找到规则匹配的模块 API 定义 */ ")
        val apiPrefix = moduleApiClass?.let { "${it.qualifiedName}.API_PREFIX + " }.or("/* TODO 未找到规则匹配的模块 API 定义 */ ")
        val clientPath = getClientPath(moduleApiClass, table)
        // 继承模版接口
        val extension = getExtension(implTemp)
        // 生成出入参
        generateBean(project, sourceDirectory, table, implTemp)
        // 生成接口
        val controllerText = """
            package ${controlService.clientPackageName};

            /**
             * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
             */
            @$FEIGN_CLIENT(value = $serviceNamePrefix${controlService.clientName}.URL, primary = false)
            public interface ${controlService.clientName} $extension{
                String URL = $apiPrefix"$clientPath";
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(controlService.clientFileName, JavaLanguage.INSTANCE, controllerText)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    override fun executeGenerateServiceImpl(
        project: Project,
        dir: PsiDirectory,
        controlService: ControlServiceState,
        implTemp: ImplTempState
    ) {
        // 如果需要导入导出，先生成 ExcelService
        generateExcelService(project, dir, implTemp)

        val mapper = controlService.persistent.mapperName.firstCharLower()
        val text = """
            package ${controlService.serviceImplPackageName};

            /**
             * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
             */
            @$LB_SLF4J
            @$PRIMARY
            @$SERVICE
            public class ${controlService.serviceImplName} implements ${controlService.clientQualified} {
                @$AUTOWIRED
                private ${controlService.persistent.mapperQualified} $mapper;
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(controlService.serviceImplFileName, JavaLanguage.INSTANCE, text)
        // 实现模版接口的方法
        if (implTemp.enable) {
            with(implTemp) {
                PsiTreeUtil.getChildOfType(psiFile, PsiClass::class.java)?.let {
                    val parser = JavaPsiFacade.getInstance(project).parserFacade
                    if (enableImp || enableExp) {
                        parser.addFieldFromText(
                            """
                            @$AUTOWIRED
                            private $ATTACH_SERVICE attachService;
                        """.trimIndent(), it)
                        parser.addFieldFromText("""
                            @$AUTOWIRED
                            private $BKG_TASK_EXECUTOR bkgTaskExecutor;
                        """.trimIndent(), it)
                        parser.addFieldFromText("""
                            @$AUTOWIRED
                            private ${controlService.excelServiceQualified} ${controlService.excelServiceName.firstCharLower()};
                        """.trimIndent(), it)
                    }
                    if (enablePage) {
                        parser.addMethodFromText("""
                            @Override
                            public int count($queryQualified query) {
                                return $mapper.countByQuery(query);
                            }
                        """.trimIndent(), it)
                        parser.addMethodFromText("""
                            @Override
                            public $PAGI<$itemQualified, $queryQualified> list($PAGE_INFO<$queryQualified> pageInfo) {
                                $PAGI<$itemQualified, $queryQualified> result = $PAGE_UTIL
                                        .as($mapper.selectByQuery($PAGE_UTIL.as(pageInfo), pageInfo.getQuery()), pageInfo.getQuery())
                                        .map(${persistent.entityQualified}.item);
                                return result;
                            }
                        """.trimIndent(), it)
                    }
                    if (enableListAll) {
                        parser.addMethodFromText("""
                            @Override
                            public $LIST<$itemQualified> listAll($queryQualified query) {
                                $LIST<${persistent.entityQualified}> entities = $mapper.selectAllByQuery(query);
                                return entities.stream()
                                        .map(${persistent.entityQualified}.item)
                                        .collect($COLS.toList());
                            }
                        """.trimIndent(), it)
                    }
                    if (enableSelect) {
                        parser.addMethodFromText("""
                            @Override
                            public $LIST<$optionQualified> select($selectQualified query) {
                                $LIST<${persistent.entityQualified}> entities = $mapper.selectBySelectQuery(query);
                                return entities.stream()
                                        .map(${persistent.entityQualified}.option)
                                        .collect($COLS.toList());
                            }
                        """.trimIndent(), it)
                    }
                    if (enableFill) {
                        parser.addMethodFromText("""
                            @Override
                            public $LIST<$optionQualified> listByIds($LIST<Long> ids) {
                                $LIST<${persistent.entityQualified}> entities = $mapper.selectBatchIds(ids);
                                return entities.stream()
                                        .map(${persistent.entityQualified}.option)
                                        .collect($COLS.toList());
                            }
                        """.trimIndent(), it)
                    }
                    if (enableLoad) {
                        parser.addMethodFromText("""
                            @Override
                            public $baseBeanQualified load(long id) {
                                return loadFill(id);
                            }
                        """.trimIndent(), it)
                        parser.addMethodFromText("""
                            @Override
                            public boolean existsByIds($LIST<Long> ids) {
                                int count = $mapper.countInIds(ids);
                                return count > 0;
                            }
                        """.trimIndent(), it)
                    }
                    if (enableInsert) {
                        parser.addMethodFromText("""
                            @$TRANS
                            @Override
                            public $baseBeanQualified insert($baseBeanQualified value) {
                                ${persistent.entityQualified} entity = ${persistent.entityQualified}.from.apply(value);
                                $mapper.insert(entity);
                                return loadFill(entity.getId());
                            }
                        """.trimIndent(), it)
                    }
                    if (enableUpdate) {
                        parser.addMethodFromText("""
                            @$TRANS
                            @Override
                            public $baseBeanQualified update($baseBeanQualified value) {
                                ${persistent.entityQualified} entity = ${persistent.entityQualified}.from.apply(value);
                                $mapper.updateById(entity);
                                return loadFill(entity.getId());
                            }
                        """.trimIndent(), it)
                    }
                    if (enableLoad || enableInsert || enableUpdate) {
                        parser.addMethodFromText("""
                            /**
                             * 获取 + 填充
                             */
                            private $baseBeanQualified loadFill(long id) {
                                ${persistent.entityQualified} result = $mapper.selectById(id);
                                return ${persistent.entityQualified}.to.apply(result);
                            }
                        """.trimIndent(), it)
                    }
                    if (enableDelete) {
                        parser.addMethodFromText("""
                            @$TRANS
                            @Override
                            public int deleteByIds($LIST<Long> ids) {
                                return $mapper.deleteBatchIds(ids);
                            }
                        """.trimIndent(), it)
                    }
                    if (enableImp) {
                        parser.addMethodFromText("""
                            @Override
                            public $I_ATTACH template($IMP_PARAM impParam) {
                                try ($INS is = $EXCEL_UTIL.createAll(header(impParam))) {
                                    return attachService.put(is, "TODO.xlsx");
                                } catch ($IO_EXEC e) {
                                    throw $BIZ_EXEC.from(e);
                                }
                            }
                        """.trimIndent(), it)
                        parser.addMethodFromText("""
                            @Override
                            public $LIST<$EXCEL_SHEET> header($IMP_PARAM impParam) {
                                return $GU_LISTS.newArrayList(new $EXCEL_SHEET("Sheet1", $GU_LISTS.newArrayList(${controlService.excelServiceName.firstCharLower()}.getColumnInfoList())));
                            }
                        """.trimIndent(), it)
                        parser.addMethodFromText("""
                            @Override
                            public boolean imp($impInfoQualified impInfo) {
                                return false;
                            }
                        """.trimIndent(), it)
                    }
                    if (enableExp) {
                        parser.addMethodFromText("""
                            @Override
                            public boolean exp($expInfoQualified expInfo) {
                                return false;
                            }
                        """.trimIndent(), it)
                    }
                }
            }
        }

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun generateExcelService(project: Project, dir: PsiDirectory, implTemp: ImplTempState) {
        if (!implTemp.enableImp && implTemp.enableExp) {
            return
        }
        val service = implTemp.controlService
        val text = """
            package ${service.serviceImplPackageName};

            /**
             * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
             */
            @$SERVICE
            public class ${service.excelServiceName} {

                public $LIST<$EXCEL_COLUM<${implTemp.excelQualified}, ?>> getColumnInfoList() {
                    return null;
                }

                public $LIST<$EXCEL_PROPERTY<${implTemp.excelQualified}, ?, ?>> getPropertyInfoList() {
                    return null;
                }
            }
        """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(service.excelServiceFileName, JavaLanguage.INSTANCE, text)
        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    override fun executeGenerateController(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        controlService: ControlServiceState,
    ) {
        // 表注释截取作为接口注释
        val apiDesc = table.getCommentWithoutSuffix()
        val controllerText = """
            package ${controlService.controlPackageName};

            /**
             * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
             */
            @$ANNOTATION_PREAUTH
            @$SK_API_SUPPORT(order = $CLIENT_API_SORT)
            @$SK_API(value = "$apiDesc", tags = "$apiDesc")
            @$CONTROL(${controlService.clientQualified}.URL)
            public interface ${controlService.controlName} extends ${controlService.clientQualified} {
            }
        """.trimIndent()

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(controlService.controlFileName, JavaLanguage.INSTANCE, controllerText)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
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

    /**
     * 获取继承
     */
    private fun getExtension(implTemp: ImplTempState): String {
        if (!implTemp.enable) {
            return ""
        }
        val ext = StringBuilder("extends")
        // ClientQueryService
        if (implTemp.enablePage && !implTemp.enableListAll) {
            ext.append(" $QUERY_PAGE<${implTemp.itemQualified}, ${implTemp.queryQualified}>")
            implTemp.needItem = true
            implTemp.needQuery = true
        } else if (!implTemp.enablePage && implTemp.enableListAll) {
            ext.append(" $QUERY_ALL<${implTemp.itemQualified}, ${implTemp.queryQualified}>")
            implTemp.needItem = true
            implTemp.needQuery = true
        } else if (implTemp.enablePage && implTemp.enableListAll) {
            ext.append(" $QUERY_PAGE_ALL<${implTemp.itemQualified}, ${implTemp.queryQualified}>")
            implTemp.needItem = true
            implTemp.needQuery = true
        } else {
            ext.append(extendsPlaceholder)
        }
        // ClientFillService
        if (implTemp.enableSelect && !implTemp.enableFill) {
            ext.append(", $FILL_SELECT<${implTemp.optionQualified}, ${implTemp.selectQualified}>")
            implTemp.needOption = true
            implTemp.needSelectQuery = true
        } else if (!implTemp.enableSelect && implTemp.enableFill) {
            ext.append(", $FILL_NO_SELECT<${implTemp.optionQualified}>")
            implTemp.needOption = true
        } else if (implTemp.enableSelect && implTemp.enableFill) {
            ext.append(", $FILL_SIMPLE<${implTemp.optionQualified}, ${implTemp.selectQualified}>")
            implTemp.needOption = true
            implTemp.needSelectQuery = true
        } else {
            ext.append(extendsPlaceholder)
        }
        // ClientLoadService
        if (implTemp.enableLoad) {
            ext.append(", $LOAD_SIMPLE<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else {
            ext.append(extendsPlaceholder)
        }
        // ClientOperateService
        if (implTemp.enableInsert && !implTemp.enableUpdate && !implTemp.enableDelete) {
            ext.append(", $OPT_INS<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else if (!implTemp.enableInsert && implTemp.enableUpdate && !implTemp.enableDelete) {
            ext.append(", $OPT_UPT<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else if (!implTemp.enableInsert && !implTemp.enableUpdate && implTemp.enableDelete) {
            ext.append(", $OPT_SIMPLE_DEL<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else if (implTemp.enableInsert && implTemp.enableUpdate && !implTemp.enableDelete) {
            ext.append(", $OPT_NO_DEL<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else if (implTemp.enableInsert && !implTemp.enableUpdate && implTemp.enableDelete) {
            ext.append(", $OPT_NO_UPT<${implTemp.baseBeanQualified}, $QUERY>, $OPT_NO_QUERY_DEL<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else if (!implTemp.enableInsert && implTemp.enableUpdate && implTemp.enableDelete) {
            ext.append(", $OPT_NO_INS<${implTemp.baseBeanQualified}, $QUERY>, $OPT_NO_QUERY_DEL<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else if (implTemp.enableInsert && implTemp.enableUpdate && implTemp.enableDelete) {
            ext.append(", $OPT_SIMPLE<${implTemp.baseBeanQualified}, $QUERY>, $OPT_NO_QUERY_DEL<${implTemp.baseBeanQualified}>")
            implTemp.needBase = true
        } else {
            ext.append(extendsPlaceholder)
        }
        // ClientExcelService
        if (implTemp.enableImp && !implTemp.enableExp) {
            ext.append(", $EXCEL_IMP<$IMP_PARAM, ${implTemp.impInfoQualified}>")
            implTemp.needImpInfo = true
            implTemp.needImpParam = true
            implTemp.needExcel = true
        } else if (!implTemp.enableImp && implTemp.enableExp) {
            ext.append(", $EXCEL_EXP<${implTemp.queryQualified}, ${implTemp.expInfoQualified}>")
            implTemp.needQuery = true
            implTemp.needExpInfo = true
            implTemp.needExcel = true
        } else if (implTemp.enableImp && implTemp.enableExp) {
            ext.append(", $EXCEL<$IMP_PARAM, ${implTemp.queryQualified}, ${implTemp.impInfoQualified}, ${implTemp.expInfoQualified}>")
            implTemp.needQuery = true
            implTemp.needImpInfo = true
            implTemp.needImpParam = true
            implTemp.needExpInfo = true
            implTemp.needExcel = true
        } else {
            ext.append(", ")
        }
        return ext.toString()
            .replaceToEmpty("$extendsPlaceholder, ")
            .run { if (trim() == "extends") "" else this }
    }

    private fun generateBean(project: Project, sourceDir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        // bean 包目录
        var dir: PsiDirectory? = null
        if (implTemp.needBase || implTemp.needQuery || implTemp.needItem || implTemp.needOption
            || implTemp.needSelectQuery || implTemp.needExpInfo || implTemp.needImpInfo) {
            dir = PackageUtil.findOrCreateDirectoryForPackage(implTemp.controlService.clientModule,
                implTemp.beanPackageName, sourceDir, false, true)
        } else {
            return
        }
        if (dir == null) {
            throw RuntimeException("出入参包目录获取失败")
        }
        if (implTemp.needQuery) {
            executeGenerateQuery(project, dir, table, implTemp)
        }
        if (implTemp.needSelectQuery) {
            executeGenerateSelectQuery(project, dir, table, implTemp)
        }
        if (implTemp.needOption) {
            executeGenerateOption(project, dir, table, implTemp)
        }
        if (implTemp.needItem) {
            executeGenerateItem(project, dir, table, implTemp)
        }
        if (implTemp.needBase) {
            executeGenerateBase(project, dir, table, implTemp)
        }
        if (implTemp.needImpParam) {
            executeGenerateImpParam(project, dir, table, implTemp)
        }
        if (implTemp.needImpInfo) {
            executeGenerateImpInfo(project, dir, table, implTemp)
        }
        if (implTemp.needExpInfo) {
            executeGenerateExpInfo(project, dir, table, implTemp)
        }
        if (implTemp.needExcel) {
            executeGenerateExcel(project, dir, table, implTemp)
        }
    }

    private fun executeGenerateQuery(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val queryText = """
               package ${implTemp.beanPackageName};

               /**
                * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
                */
               @$LB_GETTER
               @$LB_SETTER
               @$LB_TOSTRING(callSuper = true)
               @$LB_ACCS(chain = true)
               @$LB_SB
               @$LB_NAC
               @$LB_AAC
               @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(查询条件)")
               public class ${implTemp.queryName} extends $QUERY {
                   $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
               }
            """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.queryFileName, JavaLanguage.INSTANCE, queryText)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateSelectQuery(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val queryText = """
               package ${implTemp.beanPackageName};

               /**
                * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
                */
               @$LB_GETTER
               @$LB_SETTER
               @$LB_TOSTRING(callSuper = true)
               @$LB_ACCS(chain = true)
               @$LB_SB
               @$LB_NAC
               @$LB_AAC
               @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(选项查询条件)")
               public class ${implTemp.selectName} extends $SELECT_QUERY {
                   $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
               }
            """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.selectFileName, JavaLanguage.INSTANCE, queryText)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateOption(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val text = """
           package ${implTemp.beanPackageName};

           /**
            * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
            */
           @$LB_GETTER
           @$LB_SETTER
           @$LB_TOSTRING(callSuper = true)
           @$LB_ACCS(chain = true)
           @$LB_SB
           @$LB_NAC
           @$LB_AAC
           @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(选项)")
           public class ${implTemp.optionName} extends $BASE_BEAN {
               $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
           }
        """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.optionFileName, JavaLanguage.INSTANCE, text)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateItem(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val superText = if (implTemp.needOption) implTemp.optionQualified else BASE_BEAN
        val itemText = """
           package ${implTemp.beanPackageName};

           /**
            * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
            */
           @$LB_GETTER
           @$LB_SETTER
           @$LB_TOSTRING(callSuper = true)
           @$LB_ACCS(chain = true)
           @$LB_SB
           @$LB_NAC
           @$LB_AAC
           @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(列表)")
           public class ${implTemp.itemName} extends $superText {
               $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
           }
        """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.itemFileName, JavaLanguage.INSTANCE, itemText)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateBase(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val superText = if (implTemp.needItem) {
            implTemp.itemQualified
        } else if (implTemp.needOption) {
            implTemp.optionQualified
        } else {
            BASE_BEAN
        }
        val baseText = """
                package ${implTemp.beanPackageName};

                /**
                 * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
                 */
                @$LB_GETTER
                @$LB_SETTER
                @$LB_TOSTRING(callSuper = true)
                @$LB_ACCS(chain = true)
                @$LB_SB
                @$LB_NAC
                @$LB_AAC
                @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}")
                public class ${implTemp.persistent.baseName} extends $superText {
                    $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
                }
            """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.baseBeanFileName, JavaLanguage.INSTANCE, baseText)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateImpParam(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val text = """
                package ${implTemp.beanPackageName};

                /**
                 * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
                 */
                @$LB_GETTER
                @$LB_SETTER
                @$LB_TOSTRING(callSuper = true)
                @$LB_ACCS(chain = true)
                @$LB_SB
                @$LB_NAC
                @$LB_AAC
                @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(导入参数)")
                public class ${implTemp.impParamName} extends $IMP_PARAM {
                    $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
                }
            """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.impParamFileName, JavaLanguage.INSTANCE, text)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateImpInfo(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val text = """
                package ${implTemp.beanPackageName};

                /**
                 * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
                 */
                @$LB_GETTER
                @$LB_SETTER
                @$LB_TOSTRING(callSuper = true)
                @$LB_ACCS(chain = true)
                @$LB_SB
                @$LB_NAC
                @$LB_AAC
                @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(导入)")
                public class ${implTemp.impInfoName} extends $IMP_INFO<${implTemp.impParamQualified}> {
                    $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
                }
            """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.impInfoFileName, JavaLanguage.INSTANCE, text)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateExpInfo(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val text = """
                package ${implTemp.beanPackageName};

                /**
                 * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
                 */
                @$LB_GETTER
                @$LB_SETTER
                @$LB_TOSTRING(callSuper = true)
                @$LB_ACCS(chain = true)
                @$LB_SB
                @$LB_NAC
                @$LB_AAC
                @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(导出)")
                public class ${implTemp.expInfoName} extends $EXP_INFO<${implTemp.queryQualified}> {
                    $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
                }
            """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.expInfoFileName, JavaLanguage.INSTANCE, text)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }

    private fun executeGenerateExcel(project: Project, dir: PsiDirectory, table: EFTable, implTemp: ImplTempState) {
        val text = """
                package ${implTemp.beanPackageName};

                /**
                 * <br> $CREATED_BY ${PluginVersionUtil.getVersion()}
                 */
                @$LB_GETTER
                @$LB_SETTER
                @$LB_TOSTRING(callSuper = true)
                @$LB_ACCS(chain = true)
                @$LB_SB
                @$LB_NAC
                @$LB_AAC
                @$SK_API_PROP(description = "${table.getCommentWithoutSuffix()}(表格)")
                public class ${implTemp.excelName} implements $IO_SERIAL {
                    $SERIAL_UID_FIELD = ${SerialVersionUtil.generateUID()}L;
                }
            """.trimIndent()
        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(implTemp.excelFileName, JavaLanguage.INSTANCE, text)

        val shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        val reformatted = CodeStyleManager.getInstance(project).reformat(shortened)
        dir.add(reformatted)
    }
}
