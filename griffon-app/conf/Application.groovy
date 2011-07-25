application {
	title="Twittersphere"
	startupGroups=["twittersphere"]
	autoShutdown=true
}
mvcGroups {
	twittersphere {
		model="TwittersphereModel"
		view="TwittersphereView"
		controller="TwittersphereController"
	}
}
