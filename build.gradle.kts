plugins {
	application
}

repositories {
	jcenter()
}

dependencies {
	implementation("info.picocli:picocli:4.5.1")
	implementation("com.google.guava:guava:21.0")
	annotationProcessor("info.picocli:picocli-codegen:4.5.1")
	testImplementation("junit:junit:4.12")
}

group = "me.jasonrcarrete"
version = "1.0.2"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
	mainClassName = "me.jasonrcarrete.rcon.RconClient"
}

tasks.compileJava {
	options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}
