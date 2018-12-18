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
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import static gugelvehicles.Agent.ANSI_BLUE;
import static gugelvehicles.Agent.ANSI_GREEN;
import java.util.ArrayList;

/**
 *
 * @author Anton
 */
public class AgentDron extends Vehicle{

    public AgentDron(AgentID aid, AgentID serverID, AgentID controllerID) throws Exception {
        super(aid, serverID, controllerID);
        this.ANSI = ANSI_GREEN;
        System.out.println(ANSI + "Dron inicializado");

        this.vehicleType = VehicleType.DRON;
        this.vehicleTypeName = "Dron";
    }

}

