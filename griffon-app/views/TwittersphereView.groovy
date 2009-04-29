import java.awt.Color
import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import gov.nasa.worldwind.layers.WorldMapLayer
import gov.nasa.worldwind.examples.ClickAndGoSelectListener
import gov.nasa.worldwind.examples.LayerPanel
import java.awt.BorderLayout
import gov.nasa.worldwind.event.SelectListener
import java.awt.image.BufferedImage
import gov.nasa.worldwind.geom.Position
import java.awt.Image
import gov.nasa.worldwind.render.GlobeAnnotation
import gov.nasa.worldwind.render.Annotation
import gov.nasa.worldwind.layers.RenderableLayer
import gov.nasa.worldwind.layers.AnnotationLayer

application(title:'twittersphere',  pack:true, locationByPlatform:true) {

    wwd = widget(new WorldWindowGLCanvas(), preferredSize: [500, 500],
        model: model.worldWindModel, constraints: BorderLayout.CENTER,
        selected: controller.mapSelection 
    )
    println 'hi'
    wwd.addSelectListener (controller.mapSelection as SelectListener)
    wwd.inputHandler.mouseClicked = { evt ->
        println evt
        icon = imageIcon('/griffon-icon-48x48.png')
        addTweet(Position.fromDegrees(38.9666, -104.7227, 0), "Tweet!", icon.image);
    }
    //wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));

    layerPanel = widget(new LayerPanel(wwd, null), constraints:BorderLayout.WEST)

}

annotationLayer = new AnnotationLayer()
wwd.model.layers.add(0, annotationLayer)

inset = 10; // pixels
def addTweet(Position position, String tweet, Image tweetImage) {

    def image = new BufferedImage(tweetImage.width, tweetImage.height, BufferedImage.TYPE_INT_ARGB)
    image.getGraphics().drawImage(tweetImage, 0, 0, null)
    int width = image.width
    int height = image.height
    // Check for alpha channel in image
    // Create annotation

    def text = tweet
    GlobeAnnotation ga = new GlobeAnnotation(text, position)
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

    annotationLayer.addAnnotation(ga)
}