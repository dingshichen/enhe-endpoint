// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint.util

import com.enhe.endpoint.PLUGIN_ID
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

object PluginVersionUtil {

    fun getVersion() = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))?.version.orEmpty()
}