// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-28

package com.enhe.endpoint.database

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

interface EFCodeGenerateService {

    companion object {

        @JvmStatic
        fun getInstance(project: Project): EFCodeGenerateService = project.getService(EFCodeGenerateService::class.java)
    }

    fun executeGenerateEntity(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        persistent: PersistentState,
        implTemp: ImplTempState
    )

    fun executeGenerateMapper(
        project: Project,
        dir: PsiDirectory,
        persistent: PersistentState,
        implTemp: ImplTempState
    )

    fun executeGenerateXml(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        persistent: PersistentState,
        implTemp: ImplTempState
    )

    fun executeGenerateClient(
        project: Project,
        sourceDirectory: PsiDirectory,
        dir: PsiDirectory,
        table: EFTable,
        controlService: ControlServiceState,
        implTemp: ImplTempState,
    )

    fun executeGenerateServiceImpl(
        project: Project,
        dir: PsiDirectory,
        controlService: ControlServiceState,
        implTemp: ImplTempState,
    )

    fun executeGenerateController(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        controlService: ControlServiceState,
    )
}