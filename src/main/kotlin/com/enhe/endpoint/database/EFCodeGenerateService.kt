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
        persistent: PersistentState
    )

    fun executeGenerateMapper(
        project: Project,
        dir: PsiDirectory,
        persistent: PersistentState
    )

    fun executeGenerateXml(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        persistent: PersistentState
    )

    fun executeGenerateClient(
        project: Project,
        sourceDirectory: PsiDirectory,
        dir: PsiDirectory,
        table: EFTable,
        controlService: ControlServiceState,
        implTempState: ImplTempState,
    )

    fun executeGenerateServiceImpl(
        project: Project,
        dir: PsiDirectory,
        controlService: ControlServiceState,
        implTempState: ImplTempState,
    )

    fun executeGenerateController(
        project: Project,
        dir: PsiDirectory,
        table: EFTable,
        controlService: ControlServiceState,
    )
}