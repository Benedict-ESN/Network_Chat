plugins {
    id("java")
}

group = "ru.netologi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation ("org.mockito:mockito-core:3.6.28")

}
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}
tasks.test {
    useJUnitPlatform()
}