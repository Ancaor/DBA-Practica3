/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gugelvehicles;

import es.upv.dsic.gti_ia.core.AgentID;
import static gugelvehicles.Agent.ANSI_PURPLE;

/**
 * Clase que hereda de Vehiculo
 * 
 * Establece el tipo concreto del vehiculo y su nombre.
 * 
 * @author Antonio José Camarero Ortega
 */
public class AgentTruck extends Vehicle{

    public AgentTruck(AgentID aid, AgentID serverID, AgentID controllerID) throws Exception {
        super(aid, serverID, controllerID);
        this.ANSI = ANSI_PURPLE;
        System.out.println(ANSI + "Truck inicializado");

        this.vehicleType = VehicleType.TRUCK;
        this.vehicleTypeName = "Truck";
    }


 
    
}
