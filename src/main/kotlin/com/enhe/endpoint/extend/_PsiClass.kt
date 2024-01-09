// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-28

package com.enhe.endpoint.extend

import com.enhe.endpoint.consts.FEIGN_CLIENT
import com.enhe.endpoint.consts.JAVA_ENUM
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiReturnStatement

/**
 * 获取 adapter 方法的返回值
 */
fun PsiClass.findAdapterValue(): String? {
    return this.findMethodRealReturnValue("adapter")
}

/**
 * 获取 @FeignClient 标注的接口
 */
fun PsiClass.findFeignClass(): PsiClass? {
    return supers.find { it.isInterface && it.hasAnnotation(FEIGN_CLIENT) }
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

/**
 * 获取字符串类型的属性的值
 */
fun PsiClass.findStringFieldRealValue(fieldName: String): String? {
    return findFieldByName(fieldName, false)?.children?.find { it is PsiBinaryExpression }?.resolveRealValue()
}

/**
 * 是否是 Java lang 包下的基类枚举
 */
fun PsiClass.isJavaBaseEnum(): Boolean {
    return qualifiedName == JAVA_ENUM
}