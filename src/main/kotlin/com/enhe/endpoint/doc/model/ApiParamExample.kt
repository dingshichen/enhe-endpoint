// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.doc.model

/**
 * 参数示例值的包装
 */
class ApiParamExample(
    val value: Any
) {

    override fun toString(): String {
        return value.toString()
    }
}

