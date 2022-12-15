// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.window

import com.enhe.endpoint.*
import com.enhe.endpoint.psi.findAttributeRealValue
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.impl.scopes.LibraryScope
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import javax.swing.Icon

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

    init {
        myClosedIcon = AllIcons.Actions.Colors
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        moduleNodes.clear()
        project.getService(ModuleManager::class.java).modules.forEach {
            val moduleNode = ModuleNode(this, it, project)
            if (moduleNode.childCount > 0) {
                moduleNodes.add(moduleNode)
            }
        }
        moduleNodes.sortBy { it.name }
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
        myClosedIcon = AllIcons.Actions.ModuleDirectory
        updateNode(project)
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        controllerNodes.clear()
        val psiAnnotations = mutableListOf<PsiAnnotation>()
        LibraryControlService.getInstance(project).addPsiAnnotations(psiAnnotations, module, project)
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
        myClosedIcon = AllIcons.General.ImplementingMethod
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
                        endpointNodes += EndpointNode(this, project, parentPath, an, it)
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
    private val restAnnotation: PsiAnnotation,
    val method: PsiMethod
) : BaseNode(parentNode) {

    init {
        myClosedIcon = getMethodIcon()
        updateNode(project)
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        update()
    }

    override fun buildChildren() = emptyArray<SimpleNode>()

    override fun getName(): String {
        var childPath = restAnnotation.findAttributeRealValue("value")
        if (childPath.isNullOrBlank()) {
            val st = restAnnotation.text.indexOf("{\"") + 2
            val ed = restAnnotation.text.indexOf("\"}")
            childPath = restAnnotation.text.substring(st, ed)
        }
        return childPath
    }

    /**
     * 根据请求类型选择不同的图标
     */
    private fun getMethodIcon(): Icon {
        return when (restAnnotation.qualifiedName) {
            GET_MAPPING -> IconLoader.getIcon("/icons/GET.svg", this.javaClass)
            POST_MAPPING -> IconLoader.getIcon("/icons/POST.svg", this.javaClass)
            PUT_MAPPING -> IconLoader.getIcon("/icons/PUT.svg", this.javaClass)
            DELETE_MAPPING -> IconLoader.getIcon("/icons/DELETE.svg", this.javaClass)
            else -> IconLoader.getIcon("/icons/REQUEST.svg", this.javaClass)
        }
    }

}
