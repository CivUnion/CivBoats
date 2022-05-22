plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "CivBoats"
	}
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")
}
