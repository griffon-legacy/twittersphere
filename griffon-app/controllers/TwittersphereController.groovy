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
        if ((event.eventAction == SelectEvent.LEFT_CLICK)
            && (event.topPickedObject?.hasPosition())
            && (view.wwd.view instanceof OrbitView))
        {
            OrbitView orbitView = view.wwd.view
            Globe globe = view.wwd.model.globe
            if (globe != null && orbitView != null) {
                Position targetPos = event.topPickedObject.position
                // Use a PanToIterator to iterate view to target position
                orbitView.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                    // The elevation component of 'targetPos' here is not the surface elevation,
                    // so we ignore it when specifying the view center position.
                    orbitView, globe, new Position(targetPos, 0),
                    Angle.ZERO, Angle.ZERO, targetPos.elevation))
            }
        }
    }

    def search = {
        def pos = Position.fromDegrees(38.9666, -104.7227, 0)
        view.addTweet(pos, "Tweet!", view.imageIcon('/griffon-icon-48x48.png').image)
        OrbitView orbitView = view.wwd.view
        orbitView.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
            orbitView, view.wwd.model.globe, pos,
            Angle.ZERO, Angle.ZERO, 1000000))
    }
}