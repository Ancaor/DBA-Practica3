/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gugelvehicles;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import static gugelvehicles.Agent.ANSI_PURPLE;
import java.util.ArrayList;

/**
 *
 * @author Anton
 */
public class AgentTruck extends Agent{

    
    
    
    private boolean finish = false;
    private final boolean DEBUG=true;
    private int state;
    
    private AgentID serverAgent;
    private AgentID controllerAgent;
    
    
    ////////CAPABILITIES///////////
    
    private int fuelrate;
    private int range;
    private boolean fly;
    
    //////////////////////////////
    
    
    
    //
    private int battery;
    private int x;
    private int y;
    private ArrayList<Integer> radar;
    
    //
    
    private String reply_with_controller;
    private String reply_with_server;
    
    
    private static final int WAIT_CONTROLLER = 0;
    private static final int WAIT_SERVER_CHEKIN = 1;
   // private static final int REQUEST_CHECKIN = 2;
   // private static final int WAIT_CHECKIN = 3;
    private static final int FINISH = 5;
   // private static final int SEND_COMMAND = 6;
    private String conversationID;
    
    public AgentTruck(AgentID aid,AgentID serverID, AgentID controllerID) throws Exception {
        super(aid);
         this.serverAgent = serverID;
        this.controllerAgent = controllerID;


        System.out.println(ANSI_PURPLE + "Camion inicializado");
        state = WAIT_CONTROLLER;
    }
    
    @Override
    public void execute(){
        while(!finish)
        {
            if(DEBUG)
                System.out.println(ANSI_PURPLE+"ESTADO_TRUCK : " + state);
             
            switch(state)
            {
                case WAIT_CONTROLLER:
                    //state = LOGIN_AGENTS;
                    wait_controller();
                    break;
               case WAIT_SERVER_CHEKIN:
                    wait_server_chekin();
                    break;
         /*       case REQUEST_CHECKIN:
                    requestCheckin();
                    break;

                case WAIT_CHECKIN:
                    waitCheckin();
                    break;

           */     case FINISH:
                    finish();
                break;
/*
                case SEND_COMMAND:
                    sendCommand();
                break;
                    */
            }
            
           
        }
       System.out.println(ANSI_PURPLE+"------- TRUCK FINISHED -------");
    }

    private void wait_controller() {
        
        ArrayList<String> message = this.receiveMessage();
        
        String performativa = message.get(0);
        this.conversationID = message.get(1);
        this.reply_with_controller = message.get(2);
        String content = message.get(3);
        String in_reply_to = message.get(4);
       // System.out.println(reply_with_controller);
        System.out.println(performativa);
        System.out.println(content);
        System.out.println(conversationID);
        System.out.println(reply_with_controller);
        
        if(performativa.equals("REQUEST") && content.contains("checkin")){
            System.out.println(ANSI_PURPLE + "Controlador solicita su suscripción");
            
            JsonObject contenido = new JsonObject(); 
            contenido = Json.object().add("command","checkin");
            
            this.sendMessage(this.serverAgent, contenido.toString(), ACLMessage.REQUEST, conversationID, "","");
            
            
            state=WAIT_SERVER_CHEKIN;
        }else if(performativa.equals("REQUEST") && content.contains("START")){
            System.out.println(ANSI_PURPLE+ "Coche ya puede moverse");
            state=FINISH;
        }else if(performativa.equals("REQUEST") && content.contains("RESET")){
            System.out.println(ANSI_PURPLE+ "reinicio requerido");
            state= WAIT_CONTROLLER;
        }
        
        
        
    }

    private void wait_server_chekin() {
        
        ArrayList<String> message = this.receiveMessage();
        
        String performativa = message.get(0);
        String conv_id = message.get(1);
        this.reply_with_server = message.get(2);
        String content = message.get(3);
        
        System.out.println(performativa);
        System.out.println(ANSI_PURPLE+content);
        System.out.println(conv_id);
        System.out.println(reply_with_server);
        
        if(performativa.equals("INFORM") && content.contains("OK")){
            System.out.println(ANSI_PURPLE + "Checkin realizado con exito");
            
            JsonObject contenido = new JsonObject();
            contenido = Json.parse(content).asObject();
            
            JsonObject capabilities = contenido.get("capabilities").asObject();
            
            boolean isVehicleType = checkTipoVechivulo(VehicleType.TRUCK,capabilities.get("fuelrate"));
            
            if(isVehicleType){
                this.fuelrate = capabilities.get("fuelrate").asInt();
                this.range = capabilities.get("range").asInt();
                this.fly = capabilities.get("fly").asBoolean();
                
                System.out.println(ANSI_PURPLE+ "SI es un camion");
                contenido = Json.object().add("vehicle","OK");
                
                this.sendMessage(this.controllerAgent, contenido.toString(), ACLMessage.INFORM, conversationID , this.reply_with_controller,"");
            }else{
                contenido = Json.object().add("vehicle","ERROR");
                
                System.out.println(ANSI_PURPLE+ "NO es un camion");
                
                this.sendMessage(this.controllerAgent, contenido.toString(), ACLMessage.INFORM, conversationID , this.reply_with_controller,"");
            }
            
            
            //contenido = Json.object().add("command","checkin");
            
            //this.sendMessage(this.serverAgent, contenido.toString(), ACLMessage.REQUEST, conv_id, reply_with);
            
            
            state=WAIT_CONTROLLER;
        }
        
    }

    private void finish() {
        this.finish=true;
    }

    private boolean checkTipoVechivulo(VehicleType vehicleType, JsonValue fuelrate) {
        
        int fuelrate_ = fuelrate.asInt();

        boolean check = false;
        
        switch(vehicleType){
            case CAR:
                    if(fuelrate_ == 1)
                        check = true;
                break;
            case TRUCK:
                    if(fuelrate_ == 4)
                        check = true;
                break;
            case DRON:
                    if(fuelrate_ == 2)
                        check = true;
                break;
        }
        
        return check;
    }

 
    
}
