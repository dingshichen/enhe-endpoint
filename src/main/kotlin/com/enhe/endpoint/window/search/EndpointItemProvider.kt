// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-05-26

package com.enhe.endpoint.window.search

import com.enhe.endpoint.window.EndpointModel
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel
import com.intellij.ide.util.gotoByName.ChooseByNameWeightedItemProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.Processor
import java.util.function.Predicate

class EndpointItemProvider : ChooseByNameWeightedItemProvider {

    companion object {

        @JvmStatic
        fun getInstance(project: Project): EndpointItemProvider = project.getService(EndpointItemProvider::class.java)
    }

    private val endpoints = mutableListOf<EndpointModel>()

    fun clear() {
        endpoints.clear()
    }

    operator fun plusAssign(endpointModel: EndpointModel) {
        endpoints += endpointModel
    }

    override fun filterNames(base: ChooseByNameViewModel, names: Array<out String>, pattern: String): MutableList<String> {
        return mutableListOf()
    }

    override fun filterElements(
        base: ChooseByNameViewModel,
        pattern: String,
        everywhere: Boolean,
        cancelled: ProgressIndicator,
        consumer: Processor<Any?>
    ): Boolean {
        return filterElementsWithWeights(base, pattern, everywhere, cancelled) { descriptor: FoundItemDescriptor<*> ->
            consumer.process(descriptor.item)
        }
    }

    override fun filterElementsWithWeights(
        base: ChooseByNameViewModel,
        pattern: String,
        everywhere: Boolean,
        indicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<*>>
    ): Boolean {
        return filterElements(pattern) {
            val descriptor = FoundItemDescriptor(it, it.matchingDegree)
            consumer.process(descriptor)
        }
    }

    fun filterElements(pattern: String, consumer: Predicate<EndpointModel>): Boolean {
        val matched = endpoints.filter { e -> e.path.contains(pattern) }
        matched.sortedBy { it.matchingDegree }
        for (endpointModel in matched) {
            if (!consumer.test(endpointModel)) {
                return false
            }
        }
        return true
    }
}