// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-27

package com.enhe.endpoint.consts

import com.enhe.endpoint.window.EndpointPanel
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.util.IconLoader

const val PLUGIN_ID = "com.enhe.enhe-endpoint"
const val PLUGIN_NAME = "Enhe Endpoint"
const val MYBATIS_GENERATOR = "EF Mybatis 生成器"
const val CREATED_BY = "Created By Enhe-Endpoint"

val WINDOW_PANE = DataKey.create<EndpointPanel>("EndpointPanel")

object PluginIcons {

    val requestMapping = IconLoader.getIcon("/icons/REQUEST.svg", this.javaClass)

    val getMapping = IconLoader.getIcon("/icons/GET.svg", this.javaClass)

    val postMapping = IconLoader.getIcon("/icons/POST.svg", this.javaClass)

    val putMapping = IconLoader.getIcon("/icons/PUT.svg", this.javaClass)

    val deleteMapping = IconLoader.getIcon("/icons/DELETE.svg", this.javaClass)

}