// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-05-26

package com.enhe.endpoint.window

import com.intellij.psi.PsiMethod
import javax.swing.Icon

data class EndpointModel(val icon: Icon, val path: String, val matchingDegree: Int, val method: PsiMethod) {

    constructor(icon: Icon, path: String, method: PsiMethod) : this(icon, path, 0, method)
}
