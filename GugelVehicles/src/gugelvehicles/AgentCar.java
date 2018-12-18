/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gugelvehicles;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import edu.emory.mathcs.backport.java.util.Arrays;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;

/**
 *
 * @author Anton
 */
public class AgentCar extends Vehicle {

    public AgentCar(AgentID aid, AgentID serverID, AgentID controllerID) throws Exception {
        super(aid, serverID, controllerID);
        this.ANSI = ANSI_BLUE;
        System.out.println(ANSI + "Coche inicializado");

        this.vehicleType = VehicleType.CAR;
        this.vehicleTypeName = "Coche";
        
        
    }
    

}
