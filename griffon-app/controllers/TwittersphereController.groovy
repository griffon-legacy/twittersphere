import gov.nasa.worldwind.event.SelectEvent
import gov.nasa.worldwind.geom.Position
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.globes.Globe
import gov.nasa.worldwind.view.OrbitView
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator
import groovy.util.slurpersupport.GPathResult
import java.beans.PropertyChangeListener
import java.net.URL
import java.net.URLEncoder
import net.yajjl.JsonParser
import net.yajjl.JsonParser.*

class TwittersphereController {

    TwittersphereModel model
    TwittersphereView view


    void mvcGroupInit(Map args) {
    	model.searchTermsList.add("#JavaOne")
		model.searchTermsList.addAll(getTrends())
        model.addPropertyChangeListener('tweetList',
            { view.tweetListAnimator.restart()} as PropertyChangeListener)
    }

    def search = {
        def mode = model.searchMode
        def text = model.searchText
        model.searching = true
        doOutside {
            def newTweets = []
            try {
                switch (mode) {
                    case 'Public':
                        newTweets = getPublicResults()
                        break
                    case 'Search' :
						if (text == null || text.trim().equals("")) {
							edt {
								view.searchBox.setSelectedItem("Enter a search term")
							}
							return 
						}
                        newTweets = getSearchResults(text)
                        break

                    default:
                    5.times {
                        newTweets << [
                            pos:Position.fromDegrees(new Random().nextInt(180)-90, new Random().nextInt(360)-180, 0),
                            icon:view.imageIcon('/griffon-icon-48x48.png').image,
                            tweet: "Tweet! - $mode - $text",
                            user: 'Nobody'
                        ]
                    }

                }
            } finally {
                edt {
                    model.tweetListPos = 0
                    model.tweetList = newTweets
                    model.searching = false
                }
            }
        }
    }

    def getPublicResults() {
        List results = []
        def doc = slurpAPIStream("http://twitter.com/statuses/public_timeline.xml")
        doc.status.each {
            results << [
                icon: it.user.profile_image_url as String,
                tweet: it.text as String,
                user: it.user.screen_name as String
            ]
        }
        return results
    }

    def getSearchResults(search) {
        List results = []
        def doc = slurpAPIStream("http://search.twitter.com/search.atom?q=${URLEncoder.encode(search)}&rpp=${model.searchLimit}")
        doc.entry.each {
            results << [
                icon: it.link[1]["@href"] as String,
                tweet: it.title as String,
                user: (it.author.uri as String)[19..-1]
            ]
        }
        return results
    }

    def addLocations(List<Map> tweets) {
        tweets.each this.&addLocation
    }
    
    def addLocation(def tweet) {
        try {
            def tvr = slurpAPIStream("http://twittervision.com/user/current_status/${tweet.user}.xml")
            tweet.pos = Position.fromDegrees(
                Float.parseFloat(tvr.location.latitude as String),
                Float.parseFloat(tvr.location.longitude as String),
                0);
        } catch (Exception ignore) {}
    }

    def nextTweet = {
        if (model.tweetList.size() > model.tweetListPos) {
            def tweet = model.tweetList[model.tweetListPos++]
            doOutside {
                if (!tweet.pos) {
                    addLocation(tweet)
                }
                if (tweet.icon instanceof String) {
                    tweet.icon = view.imageIcon(url:new URL(tweet.icon)).image
                    
                }
                if (tweet.pos) {
                    edt {
                        view.addTweet(tweet.pos, tweet.user, tweet.tweet, tweet.icon)
                        if (model.animate) {
                            OrbitView orbitView = view.wwd.view
                            orbitView.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                                orbitView, view.wwd.model.globe, tweet.pos,
                                Angle.ZERO, Angle.ZERO, 1000000))
                        } else {
                            doLater nextTweet
                        }
                    }
                } else {
                    edt nextTweet
                }
            }
        } else {
            view.tweetListAnimator.stop();
        }

    }

    def prevTweet = {
        
    }
	
	def getTrends = {
		try {
			def parser = new JsonParser()
			def jsonText = new URL("http://search.twitter.com/trends.json").openStream().text
			def obj = parser.parseObject(jsonText)
			def trendNames = obj.trends.collect{it.name}
			return trendNames[0..4]
		} catch(Exception e) {
			System.err.println text
            throw e
		}
	}

    XmlSlurper slurper = new XmlSlurper()
    GPathResult slurpAPIStream(String url) {
        def text = ""
        try {
            text = new URL(url).openStream().text
            synchronized (slurper) {
                return slurper.parse(new StringReader(text))
            }
        } catch (Exception e) {
            System.err.println text
            throw e
        }
    }

}
