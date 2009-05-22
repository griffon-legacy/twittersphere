import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import gov.nasa.worldwind.examples.ApplicationTemplate
import gov.nasa.worldwind.layers.*
import gov.nasa.worldwind.layers.Mercator.examples.OSMMapnikLayer
import gov.nasa.worldwind.layers.Earth.*
import gov.nasa.worldwind.render.Annotation
import gov.nasa.worldwind.render.GlobeAnnotation

import java.awt.BorderLayout
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.Image
import java.awt.event.ActionListener
import javax.swing.Timer

application(title:'twittersphere',  
  pack:true, locationByPlatform:true,
  iconImage: imageIcon('/griffon-icon-48x48.png').image,
  iconImages: [imageIcon('/griffon-icon-48x48.png').image,
               imageIcon('/griffon-icon-32x32.png').image,
               imageIcon('/griffon-icon-16x16.png').image]
) {

  wwd = widget(new WorldWindowGLCanvas(), preferredSize: [700, 500],
    model: model.worldWindModel, 
    constraints: BorderLayout.CENTER
  )

  hbox(border:emptyBorder(6), constraints:BorderLayout.SOUTH) {
    
    checkBox("Animated", selected:bind(target:model, 'animate', value:true))
    hstrut(6)

    comboBox(items:["Search", /*"Following", "Followers", */ "Public"],
      selectedItem: bind(target:model, 'searchMode'),
      lightWeightPopupEnabled:false,
      enabled:bind {!model.searching}
    )
    hstrut(6)

    label("Results:", enabled: bind {model.searchMode != 'Public'})
    resCount = slider(minimum: 20, maximum:200, preferredSize:[75,16],
        value:bind(id:'bnd', target:model, 'searchLimit', value:20),
        enabled: bind {model.searchMode != 'Public'})
    hstrut(2)
    label(text: bind {resCount.value})
    hstrut(6)

    comboBox(id:'searchBox', model:model.searchModel,
      selectedItem: bind(target:model, 'searchText'),
      lightWeightPopupEnabled:false, editable:true,
      enabled:bind {!model.searching & model.searchMode != 'Public'}
    )
    hstrut(6)

    button("Search",
      actionPerformed: controller.search,
      enabled:bind {!model.searching}
    )
    hstrut(6)

    progressBar(maximum: bind { model.tweetList?.size() ?: 1 }, 
      value: bind {model.tweetListPos}
    )
  }

}

// remove place names, add open street mal labels
LayerList ll = wwd.model.layers
ll.remove(ll.getLayerByName('Place Names'))
ApplicationTemplate.insertBeforeCompass(wwd, new OpenStreetMapLayer())
ApplicationTemplate.insertBeforeCompass(wwd, annotationLayer = new AnnotationLayer())

inset = 10 // pixels
def addTweet(pos, user, tweet, tweetImage) {
  // we need transparency, copy the image to enforce that
  def image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB)
  image.graphics.drawImage(tweetImage, 0, 0, 48, 48, null)

  // Create annotation
  GlobeAnnotation ga = new GlobeAnnotation("<a href='x'>$user</a><br>$tweet", pos)
  bean(ga.attributes,
    insets: [inset, 48 + inset * 2, inset, inset],
    imageSource: image,
    imageOffset: [inset, inset],
    imageRepeat: Annotation.IMAGE_REPEAT_NONE,
    imageOpacity: 1,
    size: [inset * 2 + 298, 0],
    adjustWidthToText : Annotation.SIZE_FIT_TEXT,
    backgroundColor : Color.WHITE,
    textColor: Color.BLACK,
    borderColor: Color.BLACK)

  //add it to the layer
  annotationLayer.addAnnotation(ga)
  
  //only show the last X number of annotations
  //the multi-step process is needed because
  //getAnnotations returns an unmodifiable collection
  if (annotationLayer.getAnnotations().size() > 10) {
  	def list = annotationLayer.getAnnotations()
	def array = list.toArray()
	annotationLayer.removeAnnotation(array[0])
  
  }

  return ga
}

tweetListAnimator = new Timer(0, controller.nextTweet as ActionListener)
tweetListAnimator.setDelay(15000)
println bnd
bnd.rebind()
