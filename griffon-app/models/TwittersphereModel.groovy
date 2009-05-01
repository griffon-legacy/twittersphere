import groovy.beans.Bindable
import gov.nasa.worldwind.Model
import gov.nasa.worldwind.WorldWind
import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.render.Annotation

class TwittersphereModel {
  Model worldWindModel = WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME)

  @Bindable String searchMode
  @Bindable String searchText

  @Bindable boolean searching
  @Bindable List tweetList
  @Bindable int tweetListPos

  @Bindable Map<String, Annotation> tweetAnnotaitons

}