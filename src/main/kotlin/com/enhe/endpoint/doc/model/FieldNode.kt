// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-28

package com.enhe.endpoint.doc.model

/**
 * 属性节点
 */
data class FieldNode(
    val typeText: String,
) {

    /**
     * 父节点
     */
    var parentNode: FieldNode? = null

    /**
     * 子节点
     */
    private val childrenNodes by lazy { mutableListOf<FieldNode>() }

    /**
     * 添加子节点
     */
    operator fun plusAssign(fieldNode: FieldNode) {
        childrenNodes += fieldNode
    }

    /**
     * 从下往上找，是否已存在
     */
    tailrec fun existFromDownToUp(parentNode: FieldNode? = null): Boolean {
        val parent = parentNode ?: this.parentNode ?: return false
        if (this == parent) {
            return true
        }
        return existFromDownToUp(parent.parentNode)
    }

    /**
     * 类型相同，则认为是相同
     */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is FieldNode) {
            return false
        }
        return this.typeText == other.typeText
    }

    override fun hashCode(): Int {
        return this.typeText.hashCode()
    }

}