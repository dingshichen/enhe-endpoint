// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.consts.FEIGN_CLIENT
import com.enhe.endpoint.extend.findValueAttributeRealValue
import com.enhe.endpoint.extend.getModules
import com.enhe.endpoint.extend.ofHttpMethod
import com.enhe.endpoint.util.PathStringUtil
import com.enhe.endpoint.window.LibraryControlService
import com.enhe.endpoint.window.search.EndpointItemProvider
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.ui.AppUIUtil
import com.intellij.util.net.HTTPMethod

class EFModule(
    val name: String,
    val controllers: MutableList<EFController> = mutableListOf()
)

class EFController(
    val simpleName: String,
    val qualifiedName: String,
    val path: String,
    val endpoints: MutableList<EFEndpoint> = mutableListOf()
)

class EFEndpoint(
    val methodName: String,
    val path: String,
    val fullPath: String,
    val httpMethod: HTTPMethod,
    val psiMethod: PsiMethod,
)

object EndpointContext {

    val efModules: MutableList<EFModule> = mutableListOf()

    fun refresh(project: Project) {
        EndpointItemProvider.getInstance(project).clear()
        efModules.clear()
        AppUIUtil.invokeOnEdt {
            project.getModules().filter { it.name.endsWith(".service") }.forEach { m ->
                val efModule = EFModule(m.name)
                // 初始化控制器
                refreshController(project, m, efModule)
                if (efModule.controllers.isNotEmpty()) {
                    efModules += efModule
                }
            }
            efModules.sortBy { it.name }
        }
    }

    private fun refreshController(project: Project, module: Module, efModule: EFModule) {
        val psiAnnotations = mutableListOf<PsiAnnotation>()
        // 从 @Service 注解开始作为锚点
        LibraryControlService.getInstance(project).addPsiAnnotations(psiAnnotations, module, project)
        psiAnnotations.forEach {
            when (val serviceClass = it.parent.parent) {
                is PsiClass -> {
                    val feignService = serviceClass.supers.filter { sc -> sc.isInterface }
                        .find { feign -> feign.hasAnnotation(FEIGN_CLIENT) }
                    feignService?.getAnnotation(FEIGN_CLIENT)?.findValueAttributeRealValue()?.let { value ->
                        // 去掉模块前缀
                        val path = if (value.startsWith(module.name)) {
                            value.substring(module.name.length)
                        } else value
                        val efController = EFController(getFullServiceName(serviceClass), serviceClass.qualifiedName!!, PathStringUtil.formatPath(path))
                        // 初始化接口方法
                        refreshMethod(serviceClass, efController)
                        if (efController.endpoints.isNotEmpty()) {
                            efModule.controllers += efController
                        }
                    }
                }
            }
        }
        efModule.controllers.sortBy { it.path }
    }

    private fun refreshMethod(serviceClass: PsiClass, efController: EFController) {
        // find method
        serviceClass.methods.forEach {
            it.findSuperMethods().forEach { superMethod ->
                superMethod.annotations.forEach { an ->
                    ofHttpMethod(an.qualifiedName)?.let { httpMethod ->
                        val path = PathStringUtil.formatPath(an.findValueAttributeRealValue())
                        val efEndpoint = EFEndpoint(it.name, path, "${efController.path}/$path", httpMethod, it)
                        efController.endpoints += efEndpoint
                    }
                }
            }
        }
        // find default method in service interface
        val overrideMethodNames = efController.endpoints.map { it.methodName }.toSet()
        serviceClass.supers.filter { it.isInterface && it.hasAnnotation(FEIGN_CLIENT) }.forEach {
            it.methods.filter { method -> method.name !in overrideMethodNames }.forEach { method ->
                method.annotations.forEach { an ->
                    ofHttpMethod(an.qualifiedName)?.let { httpMethod ->
                        val path = PathStringUtil.formatPath(an.findValueAttributeRealValue())
                        val efEndpoint = EFEndpoint(method.name, path, "$efController/$path", httpMethod, method)
                        efController.endpoints += efEndpoint
                    }
                }
            }
        }
        efController.endpoints.sortBy { it.path }
    }

    /**
     * 如果是内部类，则获取 StdObjServiceImpl.Full 这种形式
     */
    private fun getFullServiceName(service: PsiClass): String {
        return when (val parentClass = service.parent) {
            is PsiClass -> "${parentClass.name}.${service.name}"
            else -> service.name!!
        }
    }
}