plugins {
    java
}

group = "com.sc.socialcredit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/repository/public/") // Добавляем репозиторий Spigot
}

dependencies {
    // Зависимость на Bukkit
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT") // Убедитесь, что версия соответствует вашей версии сервера

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
