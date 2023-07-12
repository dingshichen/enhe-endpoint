package com.enhe.endpoint.util

import com.enhe.endpoint.extend.findAttributeRealValue
import com.intellij.psi.PsiAnnotation

object PathUtil {

    fun subParentPath(parentPath: String): String {
        val index = parentPath.indexOf("/")
        if (index == -1) {
            return parentPath.replace("//", "/")
        }
        return parentPath.substring(index + 1).replace("//", "/")
    }

    fun getChildPath(restAnnotation: PsiAnnotation): String {
        var childPath = restAnnotation.findAttributeRealValue("value")
        if (childPath.isNullOrBlank()) {
            val st = restAnnotation.text.indexOf("{\"") + 2
            val ed = restAnnotation.text.indexOf("\"}")
            childPath = if (st < 0 || ed < 0 || st >= ed) {
                ""
            } else {
                restAnnotation.text.substring(st, ed)
            }
        }
        return childPath
    }
}