application {
	title="Twittersphere"
	startupGroups=["twittersphere"]
	autoShutdown=true
}
mvcGroups {
	twittersphere {
		model="twittersphere.TwittersphereModel"
		controller="twittersphere.TwittersphereController"
		view="twittersphere.TwittersphereView"
	}
}
