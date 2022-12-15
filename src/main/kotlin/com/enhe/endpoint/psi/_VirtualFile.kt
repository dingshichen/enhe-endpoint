// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-14

package com.enhe.endpoint.psi

import com.intellij.openapi.vfs.VirtualFile

/**
 * 深度查找 class 文件
 * @param packageName 包路径
 */
fun VirtualFile.depthFindClassFile(packageName: String): Array<VirtualFile> {
    val splitPackages = packageName.split(".", limit = 2)
    val children = this.findChild(splitPackages.first())
    if (splitPackages.size == 1) {
        return children?.children ?: emptyArray()
    }
    return children?.depthFindClassFile(splitPackages[1]) ?: emptyArray()
}


fun VirtualFile.depthFindClassFile2(relPath: String): Array<VirtualFile> {
    return this.findFileByRelativePath(relPath)?.children ?: emptyArray()
}