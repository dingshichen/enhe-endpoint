// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.window

import com.enhe.endpoint.FEIGN_CLIENT
import com.enhe.endpoint.REST_MAPPINGS
import com.enhe.endpoint.psi.findAttributeRealValue
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode

/**
 * 节点
 */
abstract class BaseNode(parentNode: SimpleNode? = null) : CachingSimpleNode(parentNode) {

    abstract fun updateNode(project: Project)
}

/**
 * 根节点
 */
class RootNode : BaseNode() {

    private val moduleNodes = mutableListOf<ModuleNode>()

    override fun updateNode(project: Project) {
        cleanUpCache()
        moduleNodes.clear()
        ModuleManager.getInstance(project).modules.forEach {
            val moduleNode = ModuleNode(this, it, project)
            if (moduleNode.childCount > 0) {
                moduleNodes.add(moduleNode)
            }
        }
        update()
    }

    override fun buildChildren() = moduleNodes.toTypedArray()

    override fun getName() = "EnheV3"
}

/**
 * 模块节点
 */
class ModuleNode(
    private val parentNode: SimpleNode,
    private val module: Module,
    private val project: Project
) : BaseNode(parentNode) {

    private val controllerNodes = mutableListOf<ControllerNode>()

    init {
        updateNode(project)
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        controllerNodes.clear()
        val psiAnnotations = JavaAnnotationIndex.getInstance()["Service", module.project, GlobalSearchScope.moduleScope(module)]
        psiAnnotations.forEach {
            when (val serviceClass = it.parent.parent) {
                is PsiClass -> {
                    val feignService = serviceClass.supers.find { feign -> feign.hasAnnotation(FEIGN_CLIENT) }
                    feignService?.getAnnotation(FEIGN_CLIENT)?.findAttributeRealValue("value")?.let { value ->
                        ControllerNode(this, serviceClass, project, value).apply {
                            if (this@apply.children().isNotEmpty()) {
                                controllerNodes += this
                            }
                        }
                    }
                }
            }
        }
        update()
    }

    override fun buildChildren() = controllerNodes.toTypedArray()

    override fun getName() = module.name

}

/**
 * 控制器节点
 */
class ControllerNode(
    private val parentNode: SimpleNode,
    private val service: PsiClass,
    private val project: Project,
    private val parentPath: String
) : BaseNode(parentNode) {

    private val endpointNodes = mutableListOf<EndpointNode>()

    init {
        updateNode(project)
    }

    fun children() = endpointNodes

    override fun updateNode(project: Project) {
        cleanUpCache()
        endpointNodes.clear()
        service.methods.forEach {
            it.findSuperMethods().forEach { superMethod ->
                superMethod.annotations.forEach { an ->
                    if (an.qualifiedName in REST_MAPPINGS) {
                        endpointNodes += EndpointNode(this, project, parentPath, an)
                    }
                }
            }
        }
        update()
    }

    override fun buildChildren() = endpointNodes.toTypedArray()

    override fun getName(): String {
        val serviceName = when(val parentClass = service.parent) {
            is PsiClass -> "${parentClass.name}.${service.name}"
            else -> service.name
        }
        return "${subParentPath()} - $serviceName"
    }

    /**
     * 排除项目前缀
     */
    private fun subParentPath(): String {
        val index = parentPath.indexOf("/")
        if (index == -1) {
            return parentPath
        }
        return parentPath.substring(index + 1)
    }

}

class EndpointNode(
    private val parentNode: SimpleNode,
    private val project: Project,
    private val parentPath: String,
    private val restAnnotation: PsiAnnotation
) : BaseNode(parentNode) {

    init {
        updateNode(project)
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        update()
    }

    override fun buildChildren() = emptyArray<SimpleNode>()

    // (an.attributes[0] as ClsNameValuePairImpl).children[0].children[0].resolveRealValue()
    override fun getName(): String {
        var childPath = restAnnotation.findAttributeRealValue("value")
        if (childPath.isNullOrBlank()) {
            val st = restAnnotation.text.indexOf("{\"") + 2
            val ed = restAnnotation.text.indexOf("\"}")
            childPath = restAnnotation.text.substring(st, ed)
        }
        return "$childPath - ${getMethodName()}"
    }

    private fun getMethodName(): String {
        return when (val method = restAnnotation.parent.parent) {
            is PsiMethod -> method.name
            else -> ""
        }
    }

}
