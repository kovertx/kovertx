plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("io.vertx:vertx-stack-depchain:${findProperty("vertx.version")}"))
    constraints {
        api(project(":kovertx-core"))
        api(project(":kovertx-config"))
        api(project(":kovertx-konfig"))
        api(project(":kovertx-serialization"))
        api(project(":kovertx-web"))
        api(project(":kovertx-sql-client"))
        api(project(":kovertx-sql-migrator"))
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}