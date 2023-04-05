// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-04-04

package com.enhe.endpoint.notifier

import com.enhe.endpoint.consts.PLUGIN_ID
import com.enhe.endpoint.consts.PLUGIN_NAME
import com.enhe.endpoint.consts.PluginIcons
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.EditorColorsUtil
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.text.VersionComparatorUtil

class StartupNotifier : StartupActivity {

    private val pluginVersion = "$PLUGIN_NAME last version"

    override fun runActivity(project: Project) {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return
        }
        val properties = PropertiesComponent.getInstance()
        val lastVersion = properties.getValue(pluginVersion)
        PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))?.let {
            if (lastVersion == null || VersionComparatorUtil.compare(lastVersion, it.version) < 0) {
                EditorColorsUtil.getGlobalOrDefaultColorScheme().run {
                    EnheNotifier.notifyStartup(project, PluginIcons.logo)
                }
            }
            properties.setValue(pluginVersion, it.version)
        }
    }

}