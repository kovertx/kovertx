import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    antlr
}

dependencies {
    implementation("io.vertx:vertx-sql-client")
    implementation(project(":kovertx-core"))

    testImplementation("io.vertx:vertx-jdbc-client")
    testImplementation("org.xerial:sqlite-jdbc:3.45.3.0")
    testImplementation("io.agroal:agroal-api:2.4")
    testImplementation("io.agroal:agroal-pool:2.4")

    antlr("org.antlr:antlr4:4.13.1")
}

tasks.generateGrammarSource {
    outputDirectory = file("${project.buildDir}/generated-src/antlr/main/io/kovertx/sql/migrator/grammar")
    arguments = arguments + listOf("-package", "io.kovertx.sql.migrator.grammar")
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateGrammarSource)
    dependsOn(tasks.generateTestGrammarSource)
}

tasks.sourcesJar {
    dependsOn(tasks.generateGrammarSource)
}