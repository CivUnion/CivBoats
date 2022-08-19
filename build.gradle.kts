plugins {
    `java-library`
}

// Remove the root build directory
gradle.buildFinished {
	project.buildDir.deleteRecursively()
}

allprojects {
	group = rootProject.group
	version = rootProject.version
	description = rootProject.description
}

subprojects {
	apply(plugin = "java-library")

	java {
		toolchain.languageVersion.set(JavaLanguageVersion.of(17))
	}

	tasks {
		compileJava {
			options.encoding = Charsets.UTF_8.name()
			options.release.set(17)
		}
		processResources {
			filteringCharset = Charsets.UTF_8.name()
			filesMatching("**/plugin.yml") {
				expand( project.properties )
			}
		}
	}
}