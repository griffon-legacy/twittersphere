import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import gov.nasa.worldwind.layers.WorldMapLayer
import gov.nasa.worldwind.examples.ClickAndGoSelectListener
import gov.nasa.worldwind.examples.LayerPanel
import java.awt.BorderLayout

application(title:'twittersphere',  pack:true, locationByPlatform:true) {

    wwd = widget(new WorldWindowGLCanvas(), preferredSize: [500, 500],
        model: model.worldWindModel, constraints: BorderLayout.CENTER)
    wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));

    layerPanel = widget(new LayerPanel(wwd, null), constraints:BorderLayout.WEST)

}