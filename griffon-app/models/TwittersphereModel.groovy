import groovy.beans.Bindable
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;

import javax.swing.*;
import java.awt.*;

class TwittersphereModel {
   Model worldWindModel = WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
}