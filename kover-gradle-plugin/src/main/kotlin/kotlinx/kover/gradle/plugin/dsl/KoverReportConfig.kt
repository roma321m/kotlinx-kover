/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.KoverMigrations
import org.gradle.api.*
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Configuration of Kover reports.
 *
 * Example of usage:
 * ```
 *  koverReport {
 *      filters {
 *          // common filters for all reports of all variants
 *      }
 *      verify {
 *          // common verification rules for all variants
 *      }
 *
 *      // default reports - special reports that are filled in by default with class measurements from Kotlin/JVM or Kotlin/MPP projects
 *      defaults {
 *          // add content of reports for specified variant to default reports
 *          mergeWith("buildVariant")
 *
 *          filters {
 *              // override report filters for default reports
 *          }
 *
 *          html {
 *              // configure default HTML report
 *          }
 *
 *          xml {
 *              // configure default XML report
 *          }
 *
 *          verify {
 *              // configure default coverage verification
 *          }
 *      }
 *
 *      androidReports("buildVariant") {
 *          filters {
 *              // override report filters for reports of specified Android build variant
 *          }
 *
 *          html {
 *              // configure HTML report for specified Android build variant
 *          }
 *
 *          xml {
 *              // configure XML report for specified Android build variant
 *          }
 *
 *          verify {
 *              // configure coverage verification for specified Android build variant
 *          }
 *      }
 *
 *  }
 * ```
 */
public interface KoverReportConfig {
    /**
     * Specify common filters for all report variants, these filters will be inherited in HTML/XML/verification reports.
     * They can be redefined in the settings of a specific report.
     * ```
     *  filters {
     *      excludes {
     *          // ...
     *      }
     *
     *      includes {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun filters(config: Action<KoverReportFiltersConfig>)


    /**
     * Specify common verification rules for all report variants: JVM and Android build variants.
     * They can be overridden in the settings for a specific report variant or a specific report in a particular variant.
     * ```
     *  verify {
     *      rule {
     *          // verification rule
     *      }
     *
     *      rule("custom rule name") {
     *          // named verification rule
     *      }
     *  }
     * ```
     */
    public fun verify(config: Action<KoverVerificationRulesConfig>)

    /**
     * Configure reports for classes from Kotlin/JVM or Kotlin/MPP projects.
     * Also content from specified Android build variant can be added by calling `mergeWith`.
     *
     * example:
     * ```
     * koverReport {
     *      defaults {
     *          // add content of reports for specified variant to default reports
     *          mergeWith("buildVariant")
     *
     *          filters {
     *              // override report filters for default reports
     *          }
     *
     *          html {
     *              // configure default HTML report
     *          }
     *
     *          xml {
     *              // configure default XML report
     *          }
     *
     *          verify {
     *              // configure default coverage verification
     *          }
     *      }
     * }
     * ```
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun defaults(config: Action<KoverDefaultReportVariantConfig>) { }

    /**
     * Configure reports for classes of specified Android build variant.
     *
     * example:
     * ```
     * koverReport {
     *      androidReports("debug") {
     *          filters {
     *              // override report filters for reports of 'debug' Android build variant
     *          }
     *
     *          html {
     *              // configure HTML report for 'debug' Android build variant
     *          }
     *
     *          xml {
     *              // configure XML report for 'debug' Android build variant
     *          }
     *
     *          verify {
     *              // configure coverage verification for 'debug' Android build variant
     *          }
     *      }
     * }
     * ```
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun androidReports(variant: String, config: Action<KoverReportVariantConfig>) { }

    /**
     * TODO
     */
    public fun total(config: Action<KoverReportVariantConfig>)

    /**
     * TODO
     */
    public fun variant(variant: String, config: Action<KoverReportVariantConfig>)

}

/**
 *  Configuration for default variant reports
 */
public interface KoverDefaultReportVariantConfig: KoverReportVariantConfig {
    /**
     * Add the contents of the reports with specified variant to the default reports.
     *
     * Affects the default reports of this project and any project that specifies this project as a dependency.
     */
    public fun mergeWith(otherVariant: String)
}

public interface KoverReportVariantConfig {
    /**
     * Specify common filters for the current report variant, these filters will be inherited in HTML/XML/verification reports.
     * They can be redefined in the settings of a specific report.
     * ```
     *  filters {
     *      excludes {
     *          // ...
     *      }
     *
     *      includes {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun filters(config: Action<KoverReportFiltersConfig>)

    /**
     * TODO
     * Specify common filters for the current report variant, these filters will be inherited in HTML/XML/verification reports.
     * They can be redefined in the settings of a specific report.
     * ```
     *  filters {
     *      excludes {
     *          // ...
     *      }
     *
     *      includes {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun filtersAppend(config: Action<KoverReportFiltersConfig>)



    /**
     * Configure HTML report for current report variant.
     * ```
     *  html {
     *      filters {
     *          // ...
     *      }
     *
     *      title = "My report title"
     *      onCheck = false
     *      setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))
     *  }
     * ```
     */
    public fun html(config: Action<KoverHtmlTaskConfig>)

    /**
     * Configure XML report for current report variant.
     * ```
     *  xml {
     *      filters {
     *          // ...
     *      }
     *
     *      onCheck = false
     *      setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))
     *  }
     * ```
     */
    public fun xml(config: Action<KoverXmlTaskConfig>)

    /**
     * Configure Kover binary report for current report variant.
     * ```
     *  binary {
     *      filters {
     *          // ...
     *      }
     *
     *      onCheck.set(false)
     *      file.set(layout.buildDirectory.file("my-project-report/report.ic"))
     *  }
     * ```
     *
     * Kover binary report is compatible with IntelliJ Coverage report (ic)
     */
    public fun binary(config: Action<KoverBinaryTaskConfig>)

    /**
     * Configure coverage verification for current report variant.
     * ```
     *  verify {
     *      onCheck = true
     *
     *      rule {
     *          // ...
     *      }
     *
     *      rule("Custom Name") {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun verify(config: Action<KoverVerifyTaskConfig>)

    /**
     * Configure coverage verification for current report variant.
     * ```
     *  verify {
     *      onCheck = true
     *
     *      rule {
     *          // ...
     *      }
     *
     *      rule("Custom Name") {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun verifyAppend(config: Action<KoverVerifyTaskConfig>)

    /**
     * Configure coverage printing to the log for current report variant.
     * ```
     *  log {
     *      onCheck = true
     *
     *      filters {
     *          // ...
     *      }
     *      header = null
     *      format = "<entity> line coverage: <value>%"
     *      groupBy = GroupingEntityType.APPLICATION
     *      coverageUnits = MetricType.LINE
     *      aggregationForGroup = AggregationType.COVERED_PERCENTAGE
     *  }
     * ```
     */
    public fun log(config: Action<KoverLogTaskConfig>)
}

/**
 * Configuration of coverage printing to the log for current report variant.
 * ```
 *  log {
 *      onCheck = true
 *
 *      filters {
 *          // ...
 *      }
 *      header = null
 *      format = "<entity> line coverage: <value>%"
 *      groupBy = GroupingEntityType.APPLICATION
 *      coverageUnits = MetricType.LINE
 *      aggregationForGroup = AggregationType.COVERED_PERCENTAGE
 *  }
 * ```
 */
public interface KoverLogTaskConfig {
    /**
     * Override common filters only for logging report.
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>)

    /**
     * Print coverage when running the `check` task.
     *
     * `false` by default.
     */
    public val onCheck: Property<Boolean>

    /**
     * Add a header line to the output before the lines with coverage.
     *
     * If value is `null` then the header is absent.
     *
     * `null` by default.
     */
    public val header: Property<String>

    /**
     * Format of the strings to print coverage for the specified in [groupBy] group.
     *
     * The following placeholders can be used:
     *  - `<value>` - coverage value
     *  - `<entity>` - name of the entity by which the grouping took place. `application` if [groupBy] is [GroupingEntityType.APPLICATION].
     *
     * `"<entity> line coverage: <value>%"` if value is `null`.
     *
     * `null` by default.
     */
    public val format: Property<String>

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     *
     *
     * [GroupingEntityType.APPLICATION] if value is `null`.
     *
     * `null` by default.
     */
    public val groupBy: Property<GroupingEntityType>

    /**
     * Specifies which metric is used for coverage evaluation.
     *
     * [MetricType.LINE] if value is `null`.
     *
     * `null` by default.
     */
    public val coverageUnits: Property<MetricType>

    /**
     * Specifies aggregation function that will be calculated over all the elements of the same group. This function returns the printed value.
     *
     * [AggregationType.COVERED_PERCENTAGE] if value is `null`.
     *
     * `null` by default.
     */
    public val aggregationForGroup: Property<AggregationType>
}

/**
 * TODO
 */
public interface KoverCustomVariant: KoverReportVariantConfig {
    /**
     * TODO
     */
    fun add(vararg variants: String)

    /**
     * TODO
     */
    fun addAndroid(buildType: String, vararg flavours: String)
}


/**
 * Filters to excludes
 */
public interface KoverReportFiltersConfig {
    /**
     * Configures class filter in order to exclude declarations marked by specific annotations.
     *
     * Example:
     *  ```
     *  annotations {
     *      excludes += "com.example.Generated"
     *  }
     *  ```
     */

    /**
     * Configures class filter in order to exclude classes and functions.
     *
     * Example:
     *  ```
     *  excludes {
     *      classes("com.example.FooBar?", "com.example.*Bar")
     *      packages("com.example.subpackage")
     *      annotatedBy("*Generated*")
     *  }
     *  ```
     * Excludes have priority over includes.
     */
    public fun excludes(config: Action<KoverReportFilter>)

    /**
     * Configures class filter in order to include classes.
     *
     * Example:
     *  ```
     *  includes {
     *      classes("com.example.FooBar?", "com.example.*Bar")
     *      packages("com.example.subpackage")
     *  }
     *  ```
     * Excludes have priority over includes.
     */
    public fun includes(config: Action<KoverReportFilter>)

    @Deprecated(
        message = "Class filters was moved into 'excludes { classes(\"fq.name\") }' or 'includes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun classes(block: () -> Unit) { }

    @Deprecated(
        message = "Class inclusion filters was moved into 'includes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val includes: MutableList<String>
        get() = mutableListOf()

    @Deprecated(
        message = "Class exclusion filters was moved into 'excludes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val excludes: MutableList<String>
        get() = mutableListOf()
}

/**
 * Exclusion or inclusion class filter for Kover reports.
 *
 * Exclusions example for Kotlin:
 * ```
 *     excludes {
 *          classes("*.foo.Bar", "*.M?Class")
 *          classes(listOf("*.foo.Bar", "*.M?Class"))
 *          packages("foo.b?r", "com.*.example")
 *          val somePackages =
 *          packages(listOf("foo.b?r", "com.*.example"))
 *          annotatedBy("*Generated*", "com.example.KoverExclude")
 *      }
 * ```
 */
public interface KoverReportFilter {
    /**
     * Add specified classes to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  classes("*.foo.Bar", "*.M?Class")
     * ```
     */
    public fun classes(vararg names: String)

    /**
     * Add specified classes to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example for Groovy:
     * ```
     *  def someClasses = ["*.foo.Bar", "*.M?Class"]
     *  ...
     *  classes(someClasses)
     * ```
     *
     * Example for Kotlin:
     * ```
     *  val someClasses = listOf("*.foo.Bar", "*.M?Class")
     *  ...
     *  classes(someClasses)
     * ```
     */
    public fun classes(names: Iterable<String>)

    /**
     * Add specified classes to current filters.
     * TODO Lazy
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  classes("*.foo.Bar", "*.M?Class")
     * ```
     */
    public fun classes(vararg names: Provider<String>)

    /**
     * Add specified classes to current filters.
     * TODO Lazy
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example for Groovy:
     * ```
     *  def someClasses = ["*.foo.Bar", "*.M?Class"]
     *  ...
     *  classes(someClasses)
     * ```
     *
     * Example for Kotlin:
     * ```
     *  val someClasses = listOf("*.foo.Bar", "*.M?Class")
     *  ...
     *  classes(someClasses)
     * ```
     */
    public fun classes(names: Provider<Iterable<String>>)

    /**
     * Add all classes in specified package and it subpackages to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  packages("foo.b?r", "com.*.example")
     * ```
     */
    public fun packages(vararg names: String)

    /**
     * Add all classes in specified package and it subpackages to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example for Groovy:
     * ```
     *  def somePackages = ["foo.b?r", "com.*.example"]
     *
     *  packages(somePackages)
     * ```
     *
     * Example for Kotlin:
     * ```
     *  val somePackages = listOf("foo.b?r", "com.*.example")
     *  ...
     *  packages(somePackages)
     * ```
     */
    public fun packages(names: Iterable<String>)

    /**
     * Add all classes in specified package and it subpackages to current filters.
     *      * TODO Lazy
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  packages("foo.b?r", "com.*.example")
     * ```
     */
    public fun packages(vararg names: Provider<String>)

    /**
     * Add all classes in specified package and it subpackages to current filters.
     *      * TODO Lazy
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example for Groovy:
     * ```
     *  def somePackages = ["foo.b?r", "com.*.example"]
     *
     *  packages(somePackages)
     * ```
     *
     * Example for Kotlin:
     * ```
     *  val somePackages = listOf("foo.b?r", "com.*.example")
     *  ...
     *  packages(somePackages)
     * ```
     */
    public fun packages(names: Provider<Iterable<String>>)

    /**
     * Add to filters all classes and functions marked by specified annotations.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  annotatedBy("*Generated*", "com.example.KoverExclude")
     * ```
     */
    public fun annotatedBy(vararg annotationName: String)

    /**
     * Add to filters all classes and functions marked by specified annotations.
     *      * TODO Lazy
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  annotatedBy("*Generated*", "com.example.KoverExclude")
     * ```
     */
    public fun annotatedBy(vararg annotationName: Provider<String>)

    /**
     * TODO
     *
     *
     *
     *
     *
     *
     */
    public fun androidGeneratedClasses() {
        classes(
            "*Fragment",
            "*Fragment\$*",
            "*Activity",
            "*Activity\$*",
            "*.databinding.*",
            "*.BuildConfig"
        )
    }
}

/**
 * Configure Kover HTML Report.
 *
 * Example:
 * ```
 * ...
 * html {
 *     // Filter the classes that will be included in the HTML report.
 *     // This filter does not affect the list of classes that will be instrumented and it is applied only to the report of the current project.
 *     filters {
 *         // ...
 *     }
 *
 *     title = "Custom title"
 *
 *     // Generate an HTML report when running the `check` task
 *     onCheck = false
 *
 *     // Specify HTML report directory
 *     setReportDir(layout.buildDirectory.file("my-html-report"))
 * }
 *  ...
 * ```
 */
public interface KoverHtmlTaskConfig {
    /**
     * Override common filters only for HTML report.
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>)

    /**
     * Specify header in HTML reports.
     *
     * If not specified, project path is used instead.
     */
    public val title: Property<String>

    /**
     * Specify charset in HTML reports.
     *
     * If not specified, used return value of `Charset.defaultCharset()` for Kover report generator and UTF-8 is used for JaCoCo.
     */
    public val charset: Property<String>

    /**
     * Generate an HTML report when running the `check` task.
     */
    public val onCheck: Property<Boolean>

    // TODO !!!
    /**
     * Specify HTML report directory.
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun setReportDir(dir: Any) {
        TODO()
    }

    /**
     * TODO
     */
    public val htmlDir: DirectoryProperty
}

/**
 * Configure Kover XML Report.
 *
 * Example:
 * ```
 * ...
 * xml {
 *     // Filter the classes that will be included in the XML report.
 *     // This filter does not affect the list of classes that will be instrumented and it is applied only to the report of the current project.
 *     filters {
 *         // ...
 *     }
 *
 *     // Generate an XML report when running the `check` task
 *     onCheck = false
 *
 *     // Specify file to generate XML report
 *     setReportFile(layout.buildDirectory.file("my-xml-report.xml"))
 * }
 *  ...
 * ```
 */
public interface KoverXmlTaskConfig {
    /**
     * Override common filters only for XML report.
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>)

    /**
     * Generate an XML report when running the `check` task.
     */
    public val onCheck: Property<Boolean>

    /**
     * Specify file to generate XML report.
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun setReportFile(xmlFile: Any) {
        TODO()
    }

    /**
     * TODO
     */
    public val xmlFile: RegularFileProperty
}

/**
 * Configure Kover binary Report.
 *
 * Example:
 * ```
 * ...
 * binary {
 *     // Filter the classes that will be included in the binary report.
 *     // This filter does not affect the list of classes that will be instrumented and it is applied only to the report of the current project.
 *     filters {
 *         // ...
 *     }
 *
 *     // Generate binary report when running the `check` task
 *     onCheck.set(false)
 *
 *     // Specify file to generate binary report
 *     file.set(layout.buildDirectory.file("my-project-report/report.bin"))
 * }
 *  ...
 * ```
 */
public interface KoverBinaryTaskConfig {
    /**
     * Override common filters only for binary report.
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>)

    /**
     * Generate binary report when running the `check` task.
     */
    public val onCheck: Property<Boolean>

    /**
     * Specify file to generate binary report
     */
    public val file: RegularFileProperty
}

/**
 * Configuration of the coverage's result verification with the specified rules.
 *
 * Example:
 * ```
 *  verify {
 *      onCheck = true
 *      rule {
 *          // verification rule
 *      }
 *  }
 * ```
 */
public interface KoverVerifyTaskConfig: KoverVerificationRulesConfig {
    /**
     * Verify coverage when running the `check` task.
     * `null` by default, for Kotlin JVM and Kotlin MPP triggered on `check` task, but not for Android.
     */
    public val onCheck: Property<Boolean>
}

/**
 * Configuration to specify verification rules.
 *
 * Example:
 * ```
 *  verify {
 *      rule {
 *          // verification rule
 *      }
 *
 *      rule("custom rule name") {
 *          // named verification rule
 *      }
 *  }
 * ```
 */
public interface KoverVerificationRulesConfig {
    /**
     * Add new coverage verification rule to check after test task execution.
     */
    public fun rule(config: Action<KoverVerifyRule>)

    /**
     * Add new named coverage verification rule to check after test task execution.
     *
     * The name will be displayed in case of a verification error if Kover Tool was used.
     */
    public fun rule(name: String, config: Action<KoverVerifyRule>)
}

/**
 * Describes a single Kover verification task rule (that is part of Gradle's verify),
 * with the following configurable parameters:
 *
 * - Which classes and packages are included or excluded into the current rule
 * - What coverage bounds are enforced by current rules
 * - What kind of bounds (branches, lines, bytecode instructions) are checked by bound rules.
 */
public interface KoverVerifyRule {
    /**
     * TODO
     */
    public val groupBy: Property<GroupingEntityType>

    /**
     * TODO
     */
    public val disabled: Property<Boolean>

    /**
     * Specifies that the rule is checked during verification.
     */
    @Deprecated(
        message = "",
        replaceWith = ReplaceWith("groupBy"),
        level = DeprecationLevel.ERROR
    )
    public var isEnabled: Boolean
        get() {
            TODO()
        }
        set(value) {
            TODO()
        }

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     */
    @Deprecated(
        message = "",
        replaceWith = ReplaceWith("groupBy"),
        level = DeprecationLevel.ERROR
    )
    public var entity: GroupingEntityType
        get() {
            TODO()
        }
        set(value) {
            TODO()
        }

    /**
     * Specifies the set of Kover report filters that control
     * included or excluded classes and packages for verification.
     *
     * An example of filter configuration:
     * ```
     * filters {
     *    excludes {
     *        // Do not include deprecated package into verification coverage data
     *        className("org.jetbrains.deprecated.*")
     *     }
     * }
     * ```
     *
     * @see KoverReportFilter
     */
    @Deprecated(
        message = "",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>)

    /**
     * Specifies the set of verification rules that control the
     * coverage conditions required for the verification task to pass.
     *
     * An example of bound configuration:
     * ```
     * // At least 75% of lines should be covered in order for build to pass
     * bound {
     *     aggregationForGroup = AggregationType.COVERED_PERCENTAGE // Default aggregation
     *     metric = MetricType.LINE
     *     minValue = 75
     * }
     * ```
     *
     * @see KoverVerifyBound
     */
    public fun bound(config: Action<KoverVerifyBound>)

    /**
     * A shortcut for
     * ```
     * bound {
     *     min.set(min)
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(min: Int)

    /**
     * A shortcut for
     * ```
     * bound {
     *     min.set(min)
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(min: Provider<Int>)

    /**
     * A shortcut for
     * ```
     * bound {
     *     max = max
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(max: Int)

    /**
     * A shortcut for
     * ```
     * bound {
     *     max = max
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(max: Provider<Int>)

    // Default parameters values supported only in Kotlin.

    /**
     * A shortcut for
     * ```
     * bound {
     *     minValue = minValue
     *     coverageUnits = coverageUnits
     *     aggregationForGroup = aggregationForGroup
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(
        minValue: Int,
        coverageUnits: MetricType = MetricType.LINE,
        aggregationForGroup: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    /**
     * A shortcut for
     * ```
     * bound {
     *     maxValue = maxValue
     *     coverageUnits = coverageUnits
     *     aggregation = aggregation
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(
        maxValue: Int,
        coverageUnits: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    /**
     * A shortcut for
     * ```
     * bound {
     *     max = max
     *     min = min
     *     coverageUnits = coverageUnits
     *     aggregation = aggregation
     * }
     * ```
     *
     * @see bound
     */
    public fun bound(
        min: Int,
        max: Int,
        coverageUnits: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

}

/**
 * Describes a single bound for the verification rule to enforce;
 * Bound specifies what type of coverage is enforced (branches, lines, instructions),
 * how coverage is aggregated (raw number or percents) and what numerical values of coverage
 * are acceptable.
 */
public interface KoverVerifyBound {
    /**
     * TODO
     */
    public val min: Property<Int>

    /**
     * TODO
     */
    public val max: Property<Int>

    /**
     * TODO
     */
    public val coverageUnits: Property<MetricType>

    /**
     * TODO
     */
    public val aggregationForGroup: Property<AggregationType>

    /**
     * Specifies minimal value to compare with counter value.
     */
    @Deprecated(
        message = "",
        replaceWith = ReplaceWith("min"),
        level = DeprecationLevel.ERROR
    )
    public var minValue: Int?
        get() {
            TODO()
        }
        set(value) {
            TODO()
        }

    /**
     * Specifies maximal value to compare with counter value.
     */
    @Deprecated(
        message = "",
        replaceWith = ReplaceWith("max"),
        level = DeprecationLevel.ERROR
    )
    public var maxValue: Int?
        get() {
            TODO()
        }
        set(value) {
            TODO()
        }

    /**
     * Specifies which metric is used for code coverage verification.
     */
    @Deprecated(
        message = "",
        replaceWith = ReplaceWith("coverageUnits"),
        level = DeprecationLevel.ERROR
    )
    public var metric: MetricType
        get() {
            TODO()
        }
        set(value) {
            TODO()
        }

    /**
     * Specifies type of lines counter value to compare with minimal and maximal values if them defined.
     * Default is [AggregationType.COVERED_PERCENTAGE]
     */
    @Deprecated(
        message = "",
        replaceWith = ReplaceWith("aggregationForGroup"),
        level = DeprecationLevel.ERROR
    )
    public var aggregation: AggregationType
        get() {
            TODO()
        }
        set(value) {
            TODO()
        }

}

/**
 * Type of the metric to evaluate code coverage.
 */
public enum class MetricType {
    /**
     * Number of lines.
     */
    LINE,

    /**
     * Number of JVM bytecode instructions.
     */
    INSTRUCTION,

    /**
     * Number of branches covered.
     */
    BRANCH
}

/**
 * Type of counter value to compare with minimal and maximal values if them defined.
 */
public enum class AggregationType(val isPercentage: Boolean) {
    COVERED_COUNT(false),
    MISSED_COUNT(false),
    COVERED_PERCENTAGE(true),
    MISSED_PERCENTAGE(true)
}

/**
 * Entity type for grouping code to coverage evaluation.
 */
public enum class GroupingEntityType {
    /**
     * Counts the coverage values for all code.
     */
    APPLICATION,

    /**
     * Counts the coverage values for each class separately.
     */
    CLASS,

    /**
     * Counts the coverage values for each package that has classes separately.
     */
    PACKAGE;

    @Deprecated(
        message = "Entry was renamed to 'APPLICATION'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("APPLICATION"),
        level = DeprecationLevel.ERROR
    )
    object ALL
}
