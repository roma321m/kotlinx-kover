/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.appliers.reports.ReportsVariantApplier
import kotlinx.kover.gradle.plugin.appliers.variants.AbstractReportVariant
import kotlinx.kover.gradle.plugin.appliers.variants.AndroidReportVariant
import kotlinx.kover.gradle.plugin.appliers.variants.CustomReportVariant
import kotlinx.kover.gradle.plugin.appliers.variants.JvmReportVariant
import kotlinx.kover.gradle.plugin.appliers.variants.TotalReportVariant
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.KoverNames.DEPENDENCY_CONFIGURATION_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.JVM_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.PROJECT_EXTENSION_NAME
import kotlinx.kover.gradle.plugin.dsl.impl.KoverExtensionImpl
import kotlinx.kover.gradle.plugin.dsl.impl.KoverReportVariantConfigImpl
import kotlinx.kover.gradle.plugin.dsl.impl.KoverVariantFactoryConfigImpl
import kotlinx.kover.gradle.plugin.locators.VariantSourceLocator
import kotlinx.kover.gradle.plugin.sources.AndroidVariantSource
import kotlinx.kover.gradle.plugin.sources.JvmVariantSource
import kotlinx.kover.gradle.plugin.tasks.services.KoverAgentJarTask
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import kotlinx.kover.gradle.plugin.tools.CoverageToolFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Main Gradle Plugin applier of the project.
 */
internal class ProjectApplier(private val project: Project) {
    private lateinit var projectExtension: KoverExtensionImpl

    /**
     * The code executed right at the moment of applying of the plugin.
     */
    fun onApply() {
        val koverBucketConfiguration = project.configurations.create(DEPENDENCY_CONFIGURATION_NAME) {
            asBucket()
        }

        projectExtension = project.extensions.create(PROJECT_EXTENSION_NAME)

        val toolProvider = CoverageToolFactory.get(projectExtension)

        // DEPS
        val agentClasspath = project.configurations.create(JVM_AGENT_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        project.dependencies.add(JVM_AGENT_CONFIGURATION_NAME, toolProvider.map { tool -> tool.jvmAgentDependency })

        val reporterClasspath = project.configurations.create(JVM_REPORTER_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        project.dependencies.add(
            JVM_REPORTER_CONFIGURATION_NAME,
            toolProvider.map { tool -> tool.jvmReporterDependency })
        project.dependencies.add(
            JVM_REPORTER_CONFIGURATION_NAME,
            toolProvider.map { tool -> tool.jvmReporterExtraDependency })

        val totalReports = ReportsVariantApplier(
            project,
            TOTAL_VARIANT_NAME,
            ReportsVariantType.TOTAL,
            toolProvider,
            projectExtension.reports.total,
            reporterClasspath
        )

        VariantSourceLocator(project) { sources ->
            val instrData = collectInstrumentationData(toolProvider, agentClasspath)

            val jvmVariant =
                sources.jvm?.createVariant(
                    toolProvider,
                    koverBucketConfiguration,
                    variantConfig(JVM_VARIANT_NAME),
                    instrData
                )

            if (jvmVariant != null) {
                ReportsVariantApplier(
                    project,
                    JVM_VARIANT_NAME,
                    ReportsVariantType.JVM,
                    toolProvider,
                    reportsConfig(JVM_VARIANT_NAME),
                    reporterClasspath
                ).assign(jvmVariant)
            }

            val androidVariants = sources.android.map { source ->
                source.createVariant(
                    toolProvider,
                    koverBucketConfiguration,
                    variantConfig(source.details.buildVariant),
                    instrData
                )
            }

            val variantsFromSources = mutableMapOf<String, AbstractReportVariant>()
            jvmVariant?.let { variantsFromSources[JVM_VARIANT_NAME] = it }
            androidVariants.forEach { variantsFromSources[it.variantName] = it }

            val totalVariant = TotalReportVariant(project, toolProvider, variantConfig(TOTAL_VARIANT_NAME))
            variantsFromSources.values.forEach { totalVariant.mergeWith(it) }
            totalReports.assign(totalVariant)

            projectExtension.variants.byName.forEach { (name, config) ->
                if (name !in variantsFromSources) {
                    val customVariant =
                        CustomReportVariant(project, name, toolProvider, koverBucketConfiguration, config)

                    config.variantNamesMap.forEach { (mergedName, optional) ->
                        val mergedVariant = variantsFromSources[mergedName]
                        if (mergedVariant != null) {
                            customVariant.mergeWith(mergedVariant)
                        } else {
                            if (!optional) {
                                TODO("Not found $mergedName")
                            }
                        }
                    }

                    ReportsVariantApplier(
                        project,
                        name,
                        ReportsVariantType.CUSTOM,
                        toolProvider,
                        reportsConfig(name),
                        reporterClasspath
                    ).assign(customVariant)

                } else {
                    if (config.variantNamesMap.isNotEmpty() || config.androidVariants.isNotEmpty()) {
                        TODO("source variant can't be merged")
                    }
                }
            }

            androidVariants.forEach { androidVariant ->
                ReportsVariantApplier(
                    project,
                    androidVariant.variantName,
                    ReportsVariantType.ANDROID,
                    toolProvider,
                    reportsConfig(androidVariant.variantName),
                    reporterClasspath
                ).assign(androidVariant)
            }
        }

    }

    private fun variantConfig(variantName: String): KoverVariantFactoryConfigImpl {
        return projectExtension.variants.byName.getOrElse(variantName) {
            projectExtension.variants.create()
        }
    }

    private fun reportsConfig(variantName: String): KoverReportVariantConfigImpl {
        return projectExtension.reports.byName.getOrElse(variantName) {
            projectExtension.reports.createVariant(variantName)
        }
    }

    private fun JvmVariantSource.createVariant(
        toolProvider: Provider<CoverageTool>,
        bucket: Configuration,
        config: KoverVariantFactoryConfigImpl,
        instrData: InstrumentationData
    ): JvmReportVariant {
        tests.instrument(instrData)
        return JvmReportVariant(project, toolProvider, bucket, this, config)
    }

    private fun AndroidVariantSource.createVariant(
        toolProvider: Provider<CoverageTool>,
        bucket: Configuration,
        config: KoverVariantFactoryConfigImpl,
        instrData: InstrumentationData
    ): AndroidReportVariant {
        tests.instrument(instrData)
        return AndroidReportVariant(project, details.buildVariant, toolProvider, bucket, this, config)
    }

    /**
     * Collect all configured data, required for online instrumentation.
     */
    private fun collectInstrumentationData(
        toolProvider: Provider<CoverageTool>,
        agentClasspath: Configuration
    ): InstrumentationData {
        /*
        * Uses lazy jar search for the agent, because an eager search will cause a resolution at the configuration stage,
        * which may affect performance.
        * See https://github.com/Kotlin/kotlinx-kover/issues/235
        */
        val findAgentJarTask = project.tasks.register<KoverAgentJarTask>(FIND_JAR_TASK)
        findAgentJarTask.configure {
            // depends on agent classpath to resolve it in execute-time
            dependsOn(agentClasspath)

            this.tool.convention(toolProvider)
            this.agentJar.set(project.layout.buildDirectory.map { dir -> dir.file(agentFilePath(toolProvider.get().variant)) })
            this.agentClasspath.from(agentClasspath)
        }

        // TODO !!!
        return InstrumentationData(
            findAgentJarTask,
            toolProvider,
            emptySet()//projectExtension.instrumentation.classes
        )
    }
}

/**
 * All configured data used in online instrumentation.
 */
internal class InstrumentationData(
    val findAgentJarTask: TaskProvider<KoverAgentJarTask>,
    val toolProvider: Provider<CoverageTool>,
    val excludedClasses: Set<String>
)


/*
        val listener = object : CompilationsListener {
            override fun onJvmCompilation(kit: JvmVariantSource) {
                kit.tests.instrument(instrData)
                defaultApplier.applyCompilationKit(kit)
            }

            override fun onAndroidCompilations(kits: List<AndroidVariantSource>) {
                kits.forEach { kit ->
                    kit.tests.instrument(instrData)
                    val applier =
                        AndroidVariantApplier(project, kit.buildVariant, koverBucketConfiguration, reporterClasspath, toolProvider)

                    val configs =
                        reportExtension.namedReports[kit.buildVariant] ?: project.androidReports(kit.buildVariant)

                    applier.applyConfig(configs, reportExtension.rootFilters, reportExtension.rootVerify)
                    applier.applyCompilationKit(kit)

                    androidAppliers[kit.buildVariant] = applier
                }
            }

            override fun onFinalize() {
                reportExtension.namedReports.keys.forEach { variantName ->
                    if (variantName !in androidAppliers) {
                        throw KoverIllegalConfigException("Build variant '$variantName' not found in project '${project.path}' - impossible to configure Android reports for it.\nAvailable variations: ${androidAppliers.keys}")
                    }
                }

//                reportExtension.total.merged.forEach { variantName ->
//                    val applier = androidAppliers[variantName] ?: throw KoverIllegalConfigException("Build variant '$variantName' not found in project '${project.path}' - impossible to merge default reports with its measurements.\n" +
//                            "Available variations: ${androidAppliers.keys}")
//                    defaultApplier.mergeWith(applier)
//                }

                defaultApplier.applyConfig(reportExtension.total, reportExtension.rootFilters, reportExtension.rootVerify)
            }
        }
 */

