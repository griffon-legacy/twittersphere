import gov.nasa.worldwind.event.SelectEvent
import gov.nasa.worldwind.geom.Position
import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.globes.Globe
import gov.nasa.worldwind.view.OrbitView
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator

class TwittersphereController {

    TwittersphereModel model
    TwittersphereView view

    void mvcGroupInit(Map args) {
        // this method is called after model and view are injected
    }

    def mapSelection = { SelectEvent event ->
        println "Try!"
        if ((event.eventAction == SelectEvent.LEFT_CLICK)
            && (event.topPickedObject?.hasPosition)
            && (model.wwd.view instanceof OrbitView))
        {
            OrbitView orbitView = model.wwd.view
            Globe globe = model.wwd.model.globe;
            if (globe != null && orbitView != null) {
                println "Success!"
                Position targetPos = event.topPickedObject.position
                // Use a PanToIterator to iterate view to target position
                orbitView.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                    // The elevation component of 'targetPos' here is not the surface elevation,
                    // so we ignore it when specifying the view center position.
                    view, globe, new Position(targetPos, 0),
                    Angle.ZERO, Angle.ZERO, targetPos.getElevation() + this.elevationOffset));
            }
        }
    }
}