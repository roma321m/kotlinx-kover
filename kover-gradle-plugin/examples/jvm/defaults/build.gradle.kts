plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kover {
    // disable()

    excludeJavaCode()

    excludeInstrumentation {
        classes("com.example.subpackage.*")
    }

    excludeTests {
        tasks("myTest")
    }
}

kover {
    reports {
        filters {
            includes {
                classes.add("com.example.*")
            }
        }

        verify {
            rule {
                isEnabled = true
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

                filters {
                    excludes {
                        classes.add("com.example.verify.subpackage.*")
                    }
                    includes {
                        classes.add("com.example.verify.*")
                    }
                }

                bound {
                    min.set(1)
                    max.set(99)
                    coverageUnits.set(kotlinx.kover.gradle.plugin.dsl.MetricType.LINE)
                    aggregationForGroup.set(kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE)
                }
            }
        }

        defaults {
            filters {
                excludes {
                    classes.add("com.example.subpackage.*")
                }
                includes {
                    classes.add("com.example.*")
                }
            }

            xml {
                onCheck.set(false)
                xmlFile.set(layout.buildDirectory.file("my-project-report/result.xml"))

                filters {
                    excludes {
                        classes.add("com.example2.subpackage.*")
                    }
                    includes {
                        classes.add("com.example2.*")
                    }
                }
            }

            html {
                onCheck.set(false)
                htmlDir.set(layout.buildDirectory.dir("my-project-report/html-result"))

                filters {
                    excludes {
                        classes.add("com.example2.subpackage.*")
                    }
                    includes {
                        classes.add("com.example2.*")
                    }
                }
            }

            verify {
                onCheck.set(true)
                rule {
                    groupBy.set(kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION)

                    filters {
                        excludes {
                            classes.add("com.example.verify.subpackage.*")
                        }
                        includes {
                            classes.add("com.example.verify.*")
                        }
                    }

                    bound {
                        min.add(2)
                        max.add(98)
                        coverageUnits.set(kotlinx.kover.gradle.plugin.dsl.MetricType.LINE)
                        aggregationForGroup.set(kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE)
                    }
                }
            }
        }
    }
}
