import groovy.beans.Bindable
import gov.nasa.worldwind.Model
import gov.nasa.worldwind.WorldWind
import ca.odell.glazedlists.EventList
import gov.nasa.worldwind.avlist.AVKey
import ca.odell.glazedlists.BasicEventList
import gov.nasa.worldwind.render.Annotation
import ca.odell.glazedlists.swing.EventComboBoxModel

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
  EventList<String> searchTermsList = new BasicEventList<String>()
  EventComboBoxModel<String> searchModel = new EventComboBoxModel<String>(searchTermsList)
}
