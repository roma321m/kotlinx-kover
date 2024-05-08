/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.settings.plugins

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.CoverageToolVendor
import kotlinx.kover.gradle.plugin.commons.KOTLIN_MULTIPLATFORM_PLUGIN_ID
import kotlinx.kover.gradle.plugin.commons.asConsumer
import kotlinx.kover.gradle.plugin.commons.binReportName
import kotlinx.kover.gradle.plugin.commons.binReportsRootPath
import kotlinx.kover.gradle.plugin.settings.artifacts.CompilationInfo
import kotlinx.kover.gradle.plugin.settings.instrumentation.InstrumentationFilter
import kotlinx.kover.gradle.plugin.settings.instrumentation.JvmOnFlyInstrumenter
import kotlinx.kover.gradle.plugin.settings.tasks.ArtifactGenerationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal class KoverProjectGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.configureInstrumentation()
        target.configureArtifactGeneration()
    }


    interface KoverContentAttr {
        companion object {
            val ATTRIBUTE = Attribute.of(
                "kotlinx.kover.content.type",
                String::class.java
            )

            val AGENT_JAR = "AgentJar"
            val LOCAL_ARTIFACT = "localArtifact"
        }
    }

    private fun Project.configureInstrumentation() {
        val koverJarDependency = configurations.getByName("koverJar")
        val jarConfig = configurations.create("agentJarSource") {
            asConsumer()
            attributes {
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.AGENT_JAR)
            }
            extendsFrom(koverJarDependency)
        }
        JvmOnFlyInstrumenter.instrument(tasks.withType<Test>(), jarConfig, InstrumentationFilter(setOf(), setOf()))
    }

    private fun Project.configureArtifactGeneration() {
        val taskGraph = gradle.taskGraph

        val artifactFile = layout.buildDirectory.file("kover/kover.artifact")
        val generateArtifactTask = tasks.register<ArtifactGenerationTask>("koverGenerateArtifact") {
            outputFile.set(artifactFile)
        }

        // add tests
        val testTasks = tasks.withType<Test>().matching { task ->
            // TODO task not excluded
            taskGraph.hasTask(task.path)
        }

        val binReportFiles = project.layout.buildDirectory.dir(binReportsRootPath())
            .map { dir -> testTasks.map { dir.file(binReportName(it.name, CoverageToolVendor.KOVER)) } }

        val exts = extensions
        val pluginManager = pluginManager
        val projectPath = path

        val compilations = project.layout.buildDirectory.map {
            val compilations = when {
                pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) -> {
                    val kotlin = exts.getByType<KotlinJvmProjectExtension>()
                    kotlin.target.compilations.toList()
                }

                pluginManager.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID) -> {
                    val kotlin = exts.getByType<KotlinMultiplatformExtension>()
                    kotlin.targets
                        .filter { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }
                        .flatMap { it.compilations.toList() }
                }

                else -> {
                    emptyList()
                }
            }

            // TODO filter compilations by name
            compilations.filter { compilation ->
                val taskPath = projectPath + (if (projectPath == Project.PATH_SEPARATOR) "" else Project.PATH_SEPARATOR) + compilation.compileTaskProvider.name
                taskGraph.hasTask(taskPath)
            }
        }

        val compilationMap = compilations.map { allCompilations ->
            allCompilations.associate { compilation ->
                val sourceDirs = compilation.allKotlinSourceSets.flatMap { sourceSet -> sourceSet.kotlin.srcDirs }
                val outputDirs = compilation.output.classesDirs.files

                compilation.name to CompilationInfo(sourceDirs, outputDirs)
            }
        }

        val testTasksDeps = tasks.withType<Test>()
        val kotlinDeps = tasks.withType<KotlinCompilationTask<*>>()
        val javaDeps = tasks.withType<JavaCompile>()

        generateArtifactTask.configure {
            mustRunAfter(testTasksDeps)

            mustRunAfter(kotlinDeps)
            mustRunAfter(javaDeps)

            this.compilations.putAll(compilationMap)
            reportFiles.from(binReportFiles)
        }

        configurations.register("KoverArtifactProducer") {
            asProducer()
            attributes {
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.LOCAL_ARTIFACT)
            }

            outgoing.artifact(artifactFile) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(generateArtifactTask)
            }
        }
    }

}


