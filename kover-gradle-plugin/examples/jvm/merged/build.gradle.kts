plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":subproject"))
    implementation(project(":excluded"))
    testImplementation(kotlin("test"))

    kover(project(":subproject"))
}

kover {
    reports {
        filters {
            excludes {
                classes.addAll("kotlinx.kover.examples.merged.utils.*", "kotlinx.kover.examples.merged.subproject.utils.*")
            }
            includes {
                classes.addAll("kotlinx.kover.examples.merged.*")
            }
        }

        verify {
            rule {
                bound {
                    min.set(50)
                    max.set(75)
                }
            }
        }
    }
}
