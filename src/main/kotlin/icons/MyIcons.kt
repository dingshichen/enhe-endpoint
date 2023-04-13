package icons

import com.intellij.openapi.util.IconLoader

// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-04-13

object MyIcons {

    @JvmField
    val Logo = IconLoader.getIcon("/icons/PLUGIN.svg", this.javaClass)

    @JvmField
    val RequestMapping = IconLoader.getIcon("/icons/REQUEST.svg", this.javaClass)

    @JvmField
    val GetMapping = IconLoader.getIcon("/icons/GET.svg", this.javaClass)

    @JvmField
    val PostMapping = IconLoader.getIcon("/icons/POST.svg", this.javaClass)

    @JvmField
    val PutMapping = IconLoader.getIcon("/icons/PUT.svg", this.javaClass)

    @JvmField
    val DeleteMapping = IconLoader.getIcon("/icons/DELETE.svg", this.javaClass)
}