// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-28

package com.enhe.endpoint.psi

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiReturnStatement


fun PsiClass.findAdapterValue(): String? {
    return this.findMethodRealReturnValue("adapter")
}

/**
 * 获取方法的返回值常量
 */
fun PsiClass.findMethodRealReturnValue(methodName: String): String? {
    val adapterMethods = this.findMethodsByName(methodName, false)
    if (adapterMethods.isEmpty()) {
        return null
    }
    val returnStatement = adapterMethods.first()
        .body
        ?.statements
        ?.find { it is PsiReturnStatement } as PsiReturnStatement
    return returnStatement.returnValue?.resolveRealValue()
}