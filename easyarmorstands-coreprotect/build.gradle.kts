plugins {
    id("easyarmorstands.base")
}

dependencies {
    compileOnly(project(":easyarmorstands-plugin"))
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly(libs.coreprotect)
}
