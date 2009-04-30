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

application(title:'twittersphere',  
  pack:true, locationByPlatform:true,
  iconImage: imageIcon('/griffon-icon-48x48.png').image,
  iconImages: [imageIcon('/griffon-icon-48x48.png').image,
               imageIcon('/griffon-icon-32x32.png').image,
               imageIcon('/griffon-icon-16x16.png').image]
) {
  hbox(border:emptyBorder(6), constraints:BorderLayout.NORTH) {
    comboBox(items:["Search", "Timeline", "Following", "Followers", "Public"],
        selectedItem: bind(target:model, 'searchMode'),
        lightWeightPopupEnabled:false
    )
    hstrut(6)
    textField("#JavaOne", text:bind(target:model, 'searchText'))
    hstrut(6)
    button("Search", actionPerformed: controller.search)
  }
  wwd = widget(new WorldWindowGLCanvas(), preferredSize: [700, 500],
    model: model.worldWindModel, constraints: BorderLayout.CENTER,
    selected: controller.mapSelection
  )
}

LayerList ll = wwd.model.layers
ll.remove(ll.getLayerByName('Place Names'))
ApplicationTemplate.insertBeforeCompass(wwd, new OpenStreetMapLayer())
ApplicationTemplate.insertBeforeCompass(wwd, annotationLayer = new AnnotationLayer())

inset = 10 // pixels
def addTweet(pos, tweet, tweetImage) {
  // we need transparency, copy the image to enforce that
  int width = tweetImage.width
  int height = tweetImage.height
  def image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  image.graphics.drawImage(tweetImage, 0, 0, null)

  // Create annotation
  GlobeAnnotation ga = new GlobeAnnotation(tweet, pos)
  bean(ga.attributes,
    insets: [inset, height + inset * 2, inset, inset],
    imageSource: image,
    imageOffset: [inset, inset],
    imageRepeat: Annotation.IMAGE_REPEAT_NONE,
    imageOpacity: 1,
    size: [width + inset * 2 + 200, 68],
    adjustWidthToText : Annotation.SIZE_FIT_TEXT,
    backgroundColor : Color.WHITE,
    textColor: Color.BLACK,
    borderColor: Color.BLACK)

  //add it to the layer
  annotationLayer.addAnnotation(ga)
  return ga
}