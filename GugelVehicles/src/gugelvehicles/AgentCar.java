/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gugelvehicles;


import es.upv.dsic.gti_ia.core.AgentID;

/**
 * Clase que hereda de Vehiculo
 * 
 * Establece el tipo concreto del vehiculo y su nombre.
 * 
 * @author Antonio José Camarero Ortega
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
