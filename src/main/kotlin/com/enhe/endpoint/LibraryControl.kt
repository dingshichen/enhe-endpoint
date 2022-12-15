// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-15

package com.enhe.endpoint

import com.intellij.openapi.module.impl.scopes.LibraryScope
import com.intellij.openapi.project.Project
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.search.GlobalSearchScope

// 以后配置化
val INCLUDE_LIBRARY_CONTROL_MODULES = listOf(
    LibraryControl("asset.service", "Maven: com.enhe.commons:asset.service:"),
    LibraryControl("flow.service", "Maven: com.enhe.commons:flow.service:"),
    LibraryControl("job.service", "Maven: com.enhe.commons:job.service:")
)

/**
 * 含有控制器的库
 */
class LibraryControl(
    private val moduleName: String,
    private val libraryNameStart: String,
) {

    fun equalsModule(moduleName: String): Boolean = this.moduleName == moduleName

    fun equalsLibrary(libraryName: String): Boolean = libraryName.startsWith(this.libraryNameStart)

}

interface LibraryControlService {

    companion object {

        @JvmStatic
        fun getInstance(project: Project): LibraryControlService = project.getService(LibraryControlService::class.java)
    }

    /**
     * 将模块里、模块的库里 的控制器找到，存在 psiAnnotations 集合中
     */
    fun addPsiAnnotations(psiAnnotations: MutableList<PsiAnnotation>, module: Module, project: Project)
}

class LibraryControlServiceImpl : LibraryControlService {

    override fun addPsiAnnotations(psiAnnotations: MutableList<PsiAnnotation>, module: Module, project: Project) {
        val libraryControl = findLibraryControl(module)
        if (libraryControl == null) {
            psiAnnotations.addAll(
                JavaAnnotationIndex.getInstance()["Service", module.project, GlobalSearchScope.moduleScope(
                    module
                )]
            )
        } else {
            ModuleRootManager.getInstance(module).orderEntries().forEachLibrary { lib ->
                if (libraryControl.equalsLibrary(lib.name.orEmpty())) {
                    psiAnnotations.addAll(
                        JavaAnnotationIndex.getInstance()["Service", module.project, LibraryScope(project, lib)]
                    )
                }
                return@forEachLibrary true
            }
        }
    }

    private fun findLibraryControl(module: Module): LibraryControl? {
        return INCLUDE_LIBRARY_CONTROL_MODULES.find { it.equalsModule(module.name) }
    }
}