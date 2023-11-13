/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("OVERRIDE_DEPRECATION")

package kotlinx.kover.gradle.plugin.dsl.impl

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject


internal abstract class KoverReportConfigImpl @Inject constructor(
    private val objects: ObjectFactory,
    private val layout: ProjectLayout
) : KoverReportConfig {
    internal val rootFilters: KoverReportFiltersConfigImpl = objects.newInstance()
    internal val rootVerify: KoverVerificationRulesConfigImpl = objects.newInstance()

    internal val total: KoverReportVariantConfigImpl = createVariant(TOTAL_VARIANT_NAME)

    internal val byName: MutableMap<String, KoverReportVariantConfigImpl> = mutableMapOf()

    override fun filters(config: Action<KoverReportFiltersConfig>) {
        rootFilters.also { config(it) }
    }

    override fun verify(config: Action<KoverVerificationRulesConfig>) {
        rootVerify.also { config(it) }
    }

    override fun total(config: Action<KoverReportVariantConfig>) {
        config(total)
    }

    override fun variant(variant: String, config: Action<KoverReportVariantConfig>) {
        val report = byName.getOrPut(variant) {
            createVariant(variant)
        }
        config(report)
    }

    internal fun createVariant(variantName: String): KoverReportVariantConfigImpl {
        val block = objects.newInstance<KoverReportVariantConfigImpl>(objects, layout.buildDirectory, variantName)

        block.filters.extendsFrom(rootFilters)
        block.verify.extendFrom(rootVerify)

        return block
    }
}

internal abstract class KoverReportVariantConfigImpl @Inject constructor(objects: ObjectFactory, buildDir: DirectoryProperty, variantName: String) :
    KoverReportVariantConfig {
    internal val filters: KoverReportFiltersConfigImpl = objects.newInstance()
    internal val verify: KoverVerifyTaskConfigImpl = objects.newInstance()

    internal val html: KoverHtmlTaskConfigImpl = objects.newInstance()
    internal val xml: KoverXmlTaskConfigImpl = objects.newInstance()
    internal val binary: KoverBinaryTaskConfigImpl = objects.newInstance()
    internal val log: KoverLogTaskConfigImpl = objects.newInstance()

    init {
        xml.xmlFile.convention(buildDir.file(xmlReportPath(variantName)))
        html.htmlDir.convention(buildDir.dir(htmlReportPath(variantName)))
        binary.onCheck.convention(false)
        binary.file.convention(buildDir.file(binaryReportPath(variantName)))

        log.format.convention("<entity> line coverage: <value>%")
        log.groupBy.convention(GroupingEntityType.APPLICATION)
        log.coverageUnits.convention(MetricType.LINE)
        log.aggregationForGroup.convention(AggregationType.COVERED_PERCENTAGE)
    }


    override fun filters(config: Action<KoverReportFiltersConfig>) {
        filters.clean()
        config(filters)
    }

    override fun filtersAppend(config: Action<KoverReportFiltersConfig>) {
        config(filters)
    }

    override fun html(config: Action<KoverHtmlTaskConfig>) {
        config(html)
    }

    override fun xml(config: Action<KoverXmlTaskConfig>) {
        config(xml)
    }

    override fun binary(config: Action<KoverBinaryTaskConfig>) {
        config(binary)
    }

    override fun verify(config: Action<KoverVerifyTaskConfig>) {
        verify.clean()
        config(verify)
    }

    override fun verifyAppend(config: Action<KoverVerifyTaskConfig>) {
        config(verify)
    }

    override fun log(config: Action<KoverLogTaskConfig>) {
        config(log)
    }
}

internal abstract class KoverHtmlTaskConfigImpl : KoverHtmlTaskConfig {
    override fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("TODO")
    }
}

internal abstract class KoverXmlTaskConfigImpl : KoverXmlTaskConfig {
    override fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("TODO")
    }
}

internal abstract class KoverBinaryTaskConfigImpl : KoverBinaryTaskConfig {
    override fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("TODO")
    }
}

internal abstract class KoverLogTaskConfigImpl: KoverLogTaskConfig {
    override fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("TODO")
    }
}

internal abstract class KoverVerifyTaskConfigImpl @Inject constructor(objects: ObjectFactory) :
    KoverVerificationRulesConfigImpl(objects), KoverVerifyTaskConfig

internal abstract class KoverVerificationRulesConfigImpl @Inject constructor(
    private val objects: ObjectFactory
) : KoverVerificationRulesConfig {
    internal abstract val rules: ListProperty<KoverVerifyRuleImpl>

    override fun rule(config: Action<KoverVerifyRule>) {
        val newRule = objects.newInstance<KoverVerifyRuleImpl>(objects, "")
        config(newRule)

        rules.add(newRule)
    }

    override fun rule(name: String, config: Action<KoverVerifyRule>) {
        val newRule = objects.newInstance<KoverVerifyRuleImpl>(objects, name)
        config(newRule)
        rules.add(newRule)
    }

    internal fun extendFrom(other: KoverVerificationRulesConfigImpl) {
        rules.addAll(other.rules)
    }

    internal fun clean() {
        rules.empty()
    }
}

internal abstract class KoverVerifyRuleImpl @Inject constructor(private val objects: ObjectFactory, val name: String) : KoverVerifyRule {

    init {
        disabled.set(false)
        groupBy.set(GroupingEntityType.APPLICATION)
    }

    override fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("TODO")
    }

    override fun minBound(minValue: Int, coverageUnits: MetricType, aggregationForGroup: AggregationType) {
        val newBound = createBound()
        newBound.min.set(minValue)
        newBound.coverageUnits.set(coverageUnits)
        newBound.aggregationForGroup.set(aggregationForGroup)
        bounds += newBound
    }

    override fun minBound(min: Int) {
        val newBound = createBound()
        newBound.min.set(min)
        bounds += newBound
    }

    override fun minBound(min: Provider<Int>) {
        val newBound = createBound()
        newBound.min.set(min)
        bounds += newBound
    }

    override fun maxBound(maxValue: Int, coverageUnits: MetricType, aggregation: AggregationType) {
        val newBound = createBound()
        newBound.max.set(maxValue)
        newBound.coverageUnits.set(coverageUnits)
        newBound.aggregationForGroup.set(aggregation)
        bounds += newBound
    }

    override fun maxBound(max: Int) {
        val newBound = createBound()
        newBound.max.set(max)
        bounds += newBound
    }

    override fun maxBound(max: Provider<Int>) {
        val newBound = createBound()
        newBound.max.set(max)
        bounds += newBound
    }

    override fun bound(min: Int, max: Int, coverageUnits: MetricType, aggregation: AggregationType) {
        val newBound = createBound()
        newBound.min.set(min)
        newBound.max.set(max)
        newBound.coverageUnits.set(coverageUnits)
        newBound.aggregationForGroup.set(aggregation)
        bounds += newBound
    }

    override fun bound(config: Action<KoverVerifyBound>) {
        val newBound = createBound()
        config(newBound)
        bounds += newBound
    }

    internal val bounds: MutableList<KoverVerifyBound> = mutableListOf()

    private fun createBound(): KoverVerifyBound {
        val newBound = objects.newInstance<KoverVerifyBound>()
        newBound.coverageUnits.set(MetricType.LINE)
        newBound.aggregationForGroup.set(AggregationType.COVERED_PERCENTAGE)
        return newBound
    }
}


internal open class KoverReportFiltersConfigImpl @Inject constructor(
    objects: ObjectFactory
) : KoverReportFiltersConfig {
    internal val excludesImpl: KoverReportFilterImpl = objects.newInstance()
    internal val includesImpl: KoverReportFilterImpl = objects.newInstance()

    override fun excludes(config: Action<KoverReportFilter>) {
        config(excludesImpl)
    }

    override fun includes(config: Action<KoverReportFilter>) {
        config(includesImpl)
    }

    internal fun clean() {
        excludesImpl.clean()
        includesImpl.clean()
    }

    internal fun extendsFrom(other: KoverReportFiltersConfigImpl) {
        excludesImpl.extendsFrom(other.excludesImpl)
        includesImpl.extendsFrom(other.includesImpl)
    }
}


internal abstract class KoverReportFilterImpl: KoverReportFilter {
    internal abstract val classes: SetProperty<String>
    internal abstract val annotations: SetProperty<String>

    override fun classes(vararg names: String) {
        classes.addAll(*names)
    }

    override fun classes(names: Iterable<String>) {
        classes.addAll(names)
    }

    override fun classes(vararg names: Provider<String>) {
        names.forEach { nameProvider ->
            classes.add(nameProvider)
        }
    }

    override fun classes(names: Provider<Iterable<String>>) {
        classes.addAll(names)
    }

    override fun packages(vararg names: String) {
        names.forEach { packageName ->
            classes.add(packageName.packageAsClass())
        }
    }

    override fun packages(names: Iterable<String>) {
        names.forEach { packageName ->
            classes.add(packageName.packageAsClass())
        }
    }

    override fun packages(vararg names: Provider<String>) {
        names.forEach { packageNameProvider ->
            classes.add(packageNameProvider.map { it.packageAsClass() })
        }
    }

    override fun packages(names: Provider<Iterable<String>>) {
        classes.addAll(names.map { packages ->
            packages.map { it.packageAsClass() }
        })
    }

    override fun annotatedBy(vararg annotationName: String) {
        annotations.addAll(*annotationName)
    }

    override fun annotatedBy(vararg annotationName: Provider<String>) {
        annotationName.forEach { nameProvider ->
            annotations.add(nameProvider)
        }
    }

    internal fun extendsFrom(other: KoverReportFilterImpl) {
        classes.addAll(other.classes)
        annotations.addAll(other.annotations)
    }

    internal fun clean() {
        classes.empty()
        annotations.empty()
    }

    private fun String.packageAsClass(): String = "$this.*"
}
