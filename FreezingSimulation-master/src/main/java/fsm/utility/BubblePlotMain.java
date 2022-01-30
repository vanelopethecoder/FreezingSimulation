package fsm.utility;

import NodeModels.ParticleProperties;

import java.util.HashMap;
import java.util.Map;

public class BubblePlotMain {

    public static  void main(String [] args) {

        BubblePlotUtil bubble = new BubblePlotUtil("hello", null);


        Map<String, ParticleProperties> particlePropertiesMap = new HashMap<>();

        ParticleProperties particleProperties = new ParticleProperties();
        particleProperties.setX(10);
        particleProperties.setY(10);
        particleProperties.setVelocity(2);

        particlePropertiesMap.put("1" , particleProperties);

        bubble.updatePannel("", particlePropertiesMap);
        bubble.pack();
        bubble.setVisible(true);


    }

}
