// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-28

package com.enhe.endpoint.provider

class EventMetadata(
    private val adapter: String,
) {

    fun isEqualsAdapter(adapter: String) = adapter == this.adapter
}

