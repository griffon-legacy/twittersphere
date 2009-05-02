import groovy.beans.Bindable
import gov.nasa.worldwind.Model
import gov.nasa.worldwind.WorldWind
import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.render.Annotation

@Bindable class TwittersphereModel {
  Model worldWindModel = WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME)

  boolean animate
  String searchMode
  String searchText
  int searchLimit

  boolean searching
  List tweetList
  int tweetListPos

  @Bindable Map<String, Annotation> tweetAnnotaitons

}