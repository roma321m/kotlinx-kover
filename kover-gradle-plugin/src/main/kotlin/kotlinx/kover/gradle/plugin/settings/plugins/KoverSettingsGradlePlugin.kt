/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.settings.plugins

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.asDependency
import kotlinx.kover.gradle.plugin.commons.asProducer
import kotlinx.kover.gradle.plugin.dsl.KoverSettingsExtension
import kotlinx.kover.gradle.plugin.settings.KoverParametersProcessor
import kotlinx.kover.gradle.plugin.settings.extensions.KoverAggregatedExtensionImpl
import kotlinx.kover.gradle.plugin.settings.extensions.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.settings.extensions.KoverSettingsExtensionImpl
import kotlinx.kover.gradle.plugin.settings.plugins.KoverProjectGradlePlugin.KoverContentAttr
import kotlinx.kover.gradle.plugin.settings.tasks.KoverAgentSearchTask
import kotlinx.kover.gradle.plugin.settings.tasks.KoverHtmlReportTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf

internal class KoverSettingsGradlePlugin: Plugin<Settings> {

    override fun apply(target: Settings) {
        val objects = target.serviceOf<ObjectFactory>()

        val settingsExtension = target.extensions.create<KoverSettingsExtensionImpl>(KOVER_PROJECT_EXTENSION_NAME, objects)

        target.gradle.settingsEvaluated {
            KoverParametersProcessor.process(settingsExtension, providers)
        }

        target.gradle.beforeProject {
            extensions.create<KoverProjectExtensionImpl>(KOVER_PROJECT_EXTENSION_NAME)

            if (!settingsExtension.coverageIsEnabled.get()) {
                return@beforeProject
            }

            val agentDependency = configurations.create("koverJar") {
                asDependency()
            }
            dependencies.add(agentDependency.name, rootProject)

            if (path == Project.PATH_SEPARATOR) {
                configureRootProject(target, settingsExtension)
            } else {
                configureChildProject()
            }
            pluginManager.apply(KoverProjectGradlePlugin::class.java)
        }
    }

    private fun Project.configureRootProject(settings: Settings, settingsExtension: KoverSettingsExtensionImpl) {
        val rootExtension = extensions.create<KoverAggregatedExtensionImpl>("hiddenRootKov")
        rootExtension.projects.addAll(settingsExtension.main.includedProjects.get())
        rootExtension.excludedProjects.addAll(settingsExtension.main.excludedProjects.get())
        rootExtension.excludedClasses.addAll(settingsExtension.main.excludedClasses.get())

        val dependencyConfig = configurations.create(KOVER_DEPENDENCY_NAME) {
            asDependency()
        }
        val rootDependencies = dependencies
        settings.rootProject.walkSubprojects { descriptor ->
            rootDependencies.add(KOVER_DEPENDENCY_NAME, project(descriptor.path))
        }

        val artifacts = configurations.create("koverArtifactsCollector") {
            asConsumer()
            attributes {
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.LOCAL_ARTIFACT)
            }
            extendsFrom(dependencyConfig)
        }

        tasks.register<KoverHtmlReportTask>("koverHtmlReport") {
            dependsOn(artifacts)
            this.artifacts.from(artifacts)
            includedProjects.set(rootExtension.projects)
            excludedProjects.set(rootExtension.excludedProjects)
            excludedClasses.set(rootExtension.excludedClasses)
            htmlDir.set(layout.buildDirectory.dir("reports/kover/html"))

            this.onlyIf {
                logger.quiet("HTML report dir file://${htmlDir.get().asFile.resolve("index.html")}")
                true
            }
        }

        configureAgentSearch()
    }

    private fun Project.configureChildProject() {

    }

    private fun Project.configureAgentSearch() {
        val agentVersion = "0.8.0-Beta2"
        val agentConfiguration = configurations.create("AgentConfiguration")
        dependencies.add(agentConfiguration.name, "org.jetbrains.kotlinx:kover-jvm-agent:$agentVersion")

        val agentJar = layout.buildDirectory.file("kover/kover-jvm-agent-$agentVersion.jar")

        val findAgentTask = tasks.register<KoverAgentSearchTask>("koverAgentSearch") {
            this@register.agentJar.set(agentJar)
            dependsOn(agentConfiguration)
            agentClasspath.from(agentConfiguration)
        }

        configurations.register("AgentJar") {
            asProducer()
            attributes {
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.AGENT_JAR)
            }

            outgoing.artifact(agentJar) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(findAgentTask)
            }
        }
    }

    private fun ProjectDescriptor.walkSubprojects(block: (ProjectDescriptor) -> Unit) {
        block(this)
        children.forEach { child ->
            child.walkSubprojects(block)
        }
    }
}

