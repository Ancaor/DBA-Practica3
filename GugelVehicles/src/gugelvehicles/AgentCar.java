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
import java.util.ArrayList;

/**
 *
 * @author Anton
 */
public class AgentCar extends Agent {

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
    private ArrayList<Integer> radar = new ArrayList<>();
    
    //
    
    private String reply_with_controller;
    private String reply_with_server;
    
    
    private static final int WAIT_CONTROLLER = 0;
    private static final int WAIT_SERVER_CHEKIN = 1;
    private static final int REQUEST_WORLD_INFO = 2;
   // private static final int WAIT_CHECKIN = 3;
    private static final int FINISH = 5;
   // private static final int SEND_COMMAND = 6;
    private String conversationID;
    private int enery;
    private boolean goal;
    
    
    public AgentCar(AgentID aid, AgentID serverID, AgentID controllerID) throws Exception {
        super(aid);
        
        this.serverAgent = serverID;
        this.controllerAgent = controllerID;
        
        
        System.out.println(ANSI_BLUE + "Coche inicializado");
        state = WAIT_CONTROLLER;
    }
    
    @Override
    public void execute(){
        while(!finish)
        {
            if(DEBUG)
                System.out.println(ANSI_BLUE+"ESTADO_CAR : " + state);
             
            switch(state)
            {
                case WAIT_CONTROLLER:
                    //state = LOGIN_AGENTS;
                    wait_controller();
                    break;
               case WAIT_SERVER_CHEKIN:
                    wait_server_chekin();
                    break;
                case REQUEST_WORLD_INFO:
                    requestWorldInfo();
                    break;

         /*       case WAIT_CHECKIN:
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
       System.out.println(ANSI_BLUE+"------- CAR FINISHED -------");
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
            System.out.println(ANSI_BLUE + "Controlador solicita su suscripción");
            
            JsonObject contenido = new JsonObject(); 
            contenido = Json.object().add("command","checkin");
            
            this.sendMessage(this.serverAgent, contenido.toString(), ACLMessage.REQUEST, conversationID, "","");
            
            
            state=WAIT_SERVER_CHEKIN;
        }else if(performativa.equals("REQUEST") && content.contains("START")){
            System.out.println(ANSI_BLUE+ "Coche ya puede moverse");
            state=REQUEST_WORLD_INFO;
        }else if(performativa.equals("REQUEST") && content.contains("RESET")){
            System.out.println(ANSI_BLUE+ "reinicio requerido");
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
        System.out.println(ANSI_BLUE+content);
        System.out.println(conv_id);
        System.out.println(reply_with_server);
        
        if(performativa.equals("INFORM") && content.contains("OK")){
            System.out.println(ANSI_BLUE + "Checkin realizado con exito");
            
            JsonObject contenido = new JsonObject();
            contenido = Json.parse(content).asObject();
            
            JsonObject capabilities = contenido.get("capabilities").asObject();
            
            boolean isVehicleType = checkTipoVechivulo(VehicleType.CAR,capabilities.get("fuelrate"));
            
            if(isVehicleType){
                this.fuelrate = capabilities.get("fuelrate").asInt();
                this.range = capabilities.get("range").asInt();
                this.fly = capabilities.get("fly").asBoolean();
                
                System.out.println(ANSI_BLUE+ "SI es un coche");
                contenido = Json.object().add("vehicle","OK");
                
                this.sendMessage(this.controllerAgent, contenido.toString(), ACLMessage.INFORM, conversationID , this.reply_with_controller,"");
            }else{
                contenido = Json.object().add("vehicle","ERROR");
                
                System.out.println(ANSI_BLUE+ "NO es un coche");
                
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

    private void requestWorldInfo() {
        
        System.out.println(ANSI_BLUE + "Solicita información del mundo");
            
        this.sendMessage(this.serverAgent, "", ACLMessage.QUERY_REF, conversationID, this.reply_with_server,"");
        
        ArrayList<String> respuesta = this.receiveMessage();
        
        String performativa = respuesta.get(0);
        String conv_id = respuesta.get(1);
        String content = respuesta.get(3);
        
        System.out.println(performativa);
        System.out.println(ANSI_BLUE+content);
        System.out.println(conv_id);
        System.out.println(reply_with_server);
        
        JsonObject object = Json.parse(content).asObject();
        
        JsonObject result = object.get("result").asObject();
        
        this.battery = result.get("battery").asInt();
        System.out.println("bateria " + this.battery);
        this.x = result.get("x").asInt()+2;
        this.y = result.get("y").asInt()+2;
        System.out.println("x " + this.x + " y "+ this.y);
        JsonArray aux = result.get("sensor").asArray();
        
        for(int i=0; i < aux.size();i++){
            this.radar.add(aux.get(i).asInt());
        }
        
        for(int i=0; i < radar.size(); i++){
            System.out.println(radar.get(i));
        }
        
        this.enery = result.get("energy").asInt();
        this.goal = result.get("goal").asBoolean();
        
        
        
        //PROVISIONAL
        //////////////////
        this.sendMessage(controllerAgent, content, fuelrate, conversationID, content, content);
        
        state=FINISH;
        ///////////////7
    }

}
