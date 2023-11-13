package kotlinx.kover.gradle.plugin.dsl.shortcuts

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.newInstance

public interface KoverApplyConfig {
    /**
     * TODO
     */
    public val useJacoco: Property<Boolean>

    /**
     * TODO
     */
    public val jacocoVersion: Property<String>

    //
    public val excludeJava: Property<Boolean>

    /**
     * TODO
     */
    public fun classes(block: Action<KoverVariantClasses>)

    public fun instrumentation(block: Action<KoverVariantInstrumentation>)


    public fun createVariant(variantName: String, block: Action<KoverVariantFactoryConfig>)
}

@Suppress("UNUSED_PARAMETER")
public fun Project.koverApply(vararg excludedProjects: String, block: Action<KoverApplyConfig>) {
    val config: KoverApplyConfigImpl = objects.newInstance()

    val excluded = excludedProjects.toSet()

    val rootProject = this
    allprojects {
        if (excluded.isNotEmpty() && (this.name in excluded || this.path in excluded)) {
            return@allprojects
        }

        apply {
            plugin("org.jetbrains.kotlinx.kover")
        }

        rootProject.dependencies.add(KoverNames.DEPENDENCY_CONFIGURATION_NAME, this)

        block.execute(config)

        extensions.configure(KoverExtension::class) {
            useJacoco.set(config.useJacoco)
            jacocoVersion.set(config.jacocoVersion)

            variants {
                instrumentation {
//                    disableAll.set(config.)
                }
            }
        }
    }
}

public fun Project.koverApply(vararg excludedProjects: String) {
    koverApply(*excludedProjects) {
        // no-op
    }
}

public fun Project.koverApply(block: Action<KoverApplyConfig>) {
    koverApply(excludedProjects = emptyArray(), block)
}

public fun Project.koverApply() {
    koverApply(excludedProjects = emptyArray()) {
        // no-op
    }
}

private abstract class KoverApplyConfigImpl: KoverApplyConfig {
    override fun classes(block: Action<KoverVariantClasses>) {
        TODO("Not yet implemented")
    }

    override fun instrumentation(block: Action<KoverVariantInstrumentation>) {
        TODO("Not yet implemented")
    }

    override fun createVariant(variantName: String, block: Action<KoverVariantFactoryConfig>) {
        TODO("Not yet implemented")
    }

}
