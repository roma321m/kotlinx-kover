package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import org.gradle.testkit.runner.*
import kotlin.test.*

internal class ReportsCachingTests : BaseGradleScriptTest() {
    @Test
    fun testCaching() {
        builder("Test caching of merged reports")
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("simple")
            .withLocalCache()
            .build()
            .run("build", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                outcome(":test") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverMergedXmlReport") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverMergedHtmlReport") { assertEquals(TaskOutcome.SUCCESS, this) }
            }
            .run("clean", "--build-cache") {
                checkDefaultBinaryReport(false)
                checkDefaultReports(false)
            }
            .run("build", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                outcome(":test") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverMergedXmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverMergedHtmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
            }
    }

    @Test
    fun testProjectReportCaching() {
        builder("Test caching projects reports")
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("simple")
            .withLocalCache()
            .build()
            .run("koverReport", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultProjectReports()
                outcome(":test") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverXmlReport") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverHtmlReport") { assertEquals(TaskOutcome.SUCCESS, this) }
            }
            .run("clean", "--build-cache") {
                checkDefaultBinaryReport(false)
                checkDefaultProjectReports(false)
            }
            .run("koverReport", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultProjectReports()
                outcome(":test") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverXmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverHtmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
            }
    }

}