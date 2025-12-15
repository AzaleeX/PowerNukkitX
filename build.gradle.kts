import java.nio.charset.StandardCharsets
// Gradle API imports
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.publish.maven.MavenPublication
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    `java-library`
    `maven-publish`
    java
    idea
    jacoco
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.freefair.lombok") version "8.4"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
}

// Project configuration
group = "org.powernukkitx"
version = "2.0.0-SNAPSHOT"
description = "PNX Server"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

// Dependencies configuration
dependencies {
    // Networking
    api(libs.bundles.netty)
    
    // Logging
    api(libs.bundles.logging)
    
    // Core libraries
    api(libs.annotations)
    api(libs.jsr305)
    api(libs.gson)
    api(libs.guava)
    api(libs.commonsio)
    api(libs.fastutil)
    api(libs.snakeyaml)
    api(libs.stateless4j)

    // Database and storage
    implementation(libs.bundles.leveldb)
    
    // Utilities
    implementation(libs.rng.simple)
    implementation(libs.rng.sampling)
    implementation(libs.asm)
    implementation(libs.jose4j)
    implementation(libs.joptsimple)
    implementation(libs.disruptor)
    implementation(libs.oshi)
    implementation(libs.fastreflection)
    implementation(libs.terra)
    implementation(libs.bundles.compress)
    implementation(libs.bundles.terminal)
    implementation(libs.okaeri)

    // Testing
    testImplementation(libs.bundles.test)
    testImplementation(libs.commonsio)
    testImplementation(libs.commonslang3)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

// Dependency resolution optimization
configurations.all {
    resolutionStrategy {
        // Cache dynamic versions for 10 minutes
        cacheDynamicVersionsFor(10, "minutes")
        // Cache changing modules for 10 minutes
        cacheChangingModulesFor(10, "minutes")
        // Prefer project modules over external dependencies
        preferProjectModules()
    }
}

// Annotation processing optimization
tasks.withType<JavaCompile>().configureEach {
    options.annotationProcessorPath = configurations.getByName("annotationProcessor")
}

java {
    withSourcesJar()
    withJavadocJar()
    // Enable toolchain for better cross-platform compatibility
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// IDE configuration - automatically download dependencies source code
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = false
        // Exclude build directories from indexing
        excludeDirs.addAll(listOf(
            file(".gradle"),
            file("build"),
            file("out")
        ))
    }
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
        }
    }
}

// Optimize resource processing
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.processTestResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// Custom build tasks for different build scenarios
tasks.register<DefaultTask>("buildFast") {
    group = "alpha build"
    description = "Fast build without documentation and tests - for rapid development"
    dependsOn(tasks.compileJava, tasks.processResources, tasks.classes, tasks.jar)
}

tasks.register<DefaultTask>("buildSkipChores") {
    group = "alpha build"
    description = "Build without documentation and tests"
    dependsOn(tasks.compileJava, tasks.processResources, tasks.classes, tasks.jar, "shadowJar")
}

tasks.register<DefaultTask>("buildForGithubAction") {
    group = "build"
    description = "Optimized build for CI/CD pipelines"
    dependsOn(tasks.compileJava, tasks.processResources, tasks.classes, tasks.jar, "shadowJar", "test")
}

tasks.build {
    dependsOn("shadowJar")
    group = "alpha build"
}

tasks.clean {
    group = "alpha build"
    description = "Deletes the build directory and generated files"
    delete("pnx.yml", "terra", "services")
}

// Java compilation configuration
tasks.compileJava {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-Xpkginfo:always",
        "-parameters", // Preserve parameter names for better debugging
        "-Xlint:-options", // Suppress warnings about bootclasspath
        "-Xlint:deprecation",
        "-Xlint:unchecked"
    ))
    // Enable incremental compilation for faster builds
    options.isIncremental = true
    // Fork compiler process for better performance
    options.isFork = true
    options.forkOptions.jvmArgs = listOf("-Xmx2g")
    // Set release flag for better compatibility
    options.release.set(21)
    
    java.sourceCompatibility = JavaVersion.VERSION_21
    java.targetCompatibility = JavaVersion.VERSION_21
}

// Optimize test compilation separately
tasks.compileTestJava {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.isFork = true
    options.forkOptions.jvmArgs = listOf("-Xmx1g")
}

// Test configuration
tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "-Xmx1g", // Limit test JVM memory
        "-XX:+UseG1GC", // Use G1GC for tests
        "-XX:MaxGCPauseMillis=200" // Lower GC pause time
    )
    
    // Performance optimizations for tests
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    forkEvery = 100 // Fork new JVM after 100 tests to prevent memory issues
    
    // Test execution settings
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = false
    }
    
    finalizedBy("jacocoTestReport") // report is always generated after tests run
}

// Code coverage configuration
tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        csv.required = false
        xml.required = true
        html.required = false
    }
    dependsOn("test") // tests are required to run before generating the report
}

tasks.withType<AbstractCopyTask>() {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named<AbstractArchiveTask>("sourcesJar") {
    destinationDirectory.set(layout.buildDirectory)
}

// Improve build reproducibility for better caching
tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.named<org.gradle.jvm.tasks.Jar>("jar") {
    destinationDirectory.set(layout.buildDirectory)
    archiveFileName.set("${project.description}.jar")
}

// Shadow JAR configuration for creating fat JAR with all dependencies
tasks.named<ShadowJar>("shadowJar") {
    dependsOn("copyDependencies")
    archiveClassifier.set("shaded")
    
    manifest {
        attributes(
            "Main-Class" to "cn.nukkit.JarStart",
            "Implementation-Version" to project.version,
            "Implementation-Title" to project.name,
            "Multi-Release" to "true"
        )
    }

    // Required to fix shadowJar log4j2 plugin caching issue
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)

    // Minimize JAR size by excluding unnecessary files
    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA", 
        "META-INF/*.RSA",
        "META-INF/DEPENDENCIES",
        "META-INF/LICENSE*",
        "META-INF/NOTICE*",
        "META-INF/maven/**",
        "about.html"
    )
    
    // Merge service files for better compatibility
    mergeServiceFiles()
    
    destinationDirectory.set(layout.buildDirectory)
    
    // Enable ZIP64 format for large archives (>4GB)
    isZip64 = true
}

tasks.register<Copy>("copyDependencies") {
    dependsOn(tasks.jar)
    group = "other"
    description = "Copy all dependencies to libs folder"
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs"))
    
    // Enable up-to-date checking for better incremental builds
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    
    // Performance: Only copy if dependencies changed
    inputs.files(configurations.runtimeClasspath)
    outputs.dir(layout.buildDirectory.dir("libs"))
}

// Javadoc configuration
tasks.javadoc {
    options.encoding = StandardCharsets.UTF_8.name()
    includes.add("**/**.java")
    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption(
        "source",
        java.sourceCompatibility.toString()
    )
    // Suppress some meaningless warnings
    javadocOptions.addStringOption("Xdoclint:none", "-quiet")
    
    // Performance: Only generate javadoc for public API
    javadocOptions.addBooleanOption("public", true)
    
    // Enable parallel processing
    isFailOnError = false
}

// Maven publishing configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "server"
            pom {
                url.set("https://github.com/PowerNukkitX/PowerNukkitX")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/PowerNukkitX/PowerNukkitX.git")
                    developerConnection.set("scm:git:ssh://github.com/PowerNukkitX/PowerNukkitX.git")
                    url.set("https://github.com/PowerNukkitX/PowerNukkitX")
                }
            }
        }
    }

    repositories {
        maven {
            name = "pnx"
            url = uri("https://repo.powernukkitx.org/releases")
            credentials {
                username = providers.gradleProperty("pnxUsername")
                    .orElse(providers.environmentVariable("PNX_REPO_USERNAME"))
                    .orNull
                password = providers.gradleProperty("pnxPassword")
                    .orElse(providers.environmentVariable("PNX_REPO_PASSWORD"))
                    .orNull
            }
        }
    }
}

// Encoding configuration for all tasks
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

// Task optimization - disable unnecessary tasks for faster builds
tasks.configureEach {
    // Skip tasks that aren't needed for standard builds
    if (name.contains("delombok") && !gradle.startParameter.taskNames.contains("javadoc")) {
        enabled = false
    }
}
