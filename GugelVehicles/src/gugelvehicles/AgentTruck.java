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
import static gugelvehicles.Agent.ANSI_PURPLE;
import java.util.ArrayList;

/**
 *
 * @author Anton
 */
public class AgentTruck extends Agent{

    
    private static final int UMBRAL_BATERIA = 70;
    
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
    private static final int WAIT_CONTROLLER_COMMAND = 3;
    private static final int SEND_COMMAND_TO_SERVER = 4;
    private static final int FINISH = 5;
    private static final int  WAIT_TURN = 6;
   // private static final int SEND_COMMAND = 6;
    private String conversationID;
    private int enery;
    private boolean goal;
    private int position;
    private String netx_pos;
    
    private JsonObject information_package;
    private ArrayList<Integer> posiciones_Radar;
    
    public AgentTruck(AgentID aid,AgentID serverID, AgentID controllerID) throws Exception {
        super(aid);
         this.serverAgent = serverID;
        this.controllerAgent = controllerID;


        System.out.println(ANSI_PURPLE + "Camion inicializado");
        state = WAIT_CONTROLLER;
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
            System.out.println(ANSI_PURPLE+ "camion ya puede moverse");
            state=REQUEST_WORLD_INFO;
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
    
    private void requestWorldInfo() {
        
        System.out.println(ANSI_PURPLE + "Solicita información del mundo");
            
        this.sendMessage(this.serverAgent, "", ACLMessage.QUERY_REF, conversationID, this.reply_with_server,"");
        System.out.println(ANSI_PURPLE + "espera información del mundo");
        ArrayList<String> respuesta = this.receiveMessage();
        System.out.println(ANSI_PURPLE + "información del mundo recibida");
        String performativa = respuesta.get(0);
        String conv_id = respuesta.get(1);
        String content = respuesta.get(3);
        
        System.out.println(ANSI_PURPLE+performativa);
        System.out.println(ANSI_PURPLE+content);
        System.out.println(ANSI_PURPLE+conv_id);
        System.out.println(ANSI_PURPLE+reply_with_server);
        
        JsonObject object = Json.parse(content).asObject();
        
        JsonObject result = object.get("result").asObject();
        
        this.battery = result.get("battery").asInt();
        System.out.println("bateria " + this.battery);
        this.x = result.get("x").asInt()+5;
        this.y = result.get("y").asInt()+5;
        this.position = this.x + (this.y * 510);
        System.out.println("x " + this.x + " y "+ this.y);
        
        JsonArray aux = result.get("sensor").asArray();
        
        
        
        for(int i=0; i < aux.size();i++){
            
            this.radar.add(aux.get(i).asInt());
        }
        this.posiciones_Radar = convertRadarToPositions();
        //for(int i=0; i < radar.size(); i++){
                
          //  System.out.print(radar.get(i));
          //  if((i+1)%this.range == 0)
            //    System.out.print("\n");
        //}
        
        this.enery = result.get("energy").asInt();
        this.goal = result.get("goal").asBoolean();
        
        ArrayList<Integer> abiertos = calcularAbiertos();
      //  System.out.println("abiertos");
       // for(int i=0; i < abiertos.size(); i++){
       //     System.out.println(abiertos.get(i));
       // }
        ArrayList<Integer> cerrados = calcularCerrados();
      //  System.out.println("cerrados");
       // for(int i=0; i < cerrados.size(); i++){
       //     System.out.println(cerrados.get(i));
      //  }
        
        int pos_objetivo = obtenerPosObjetivo();
        //System.out.println("pos_objetivo");
        //System.out.println(pos_objetivo);
        
        
        information_package = Json.object();
        //JsonArray abiertos_json = Json.array();
        
        
        information_package.add("radar", this.convertToJson(radar));
        information_package.add("abiertos", this.convertToJson(abiertos));
        information_package.add("cerrados", this.convertToJson(cerrados));
        information_package.add("pos", this.position );
        information_package.add("objetive_pos", pos_objetivo);
        System.out.println(information_package.toString());
        
        this.state = WAIT_TURN;
        
    }
    
    private void waitTurn() {
                System.out.println(ANSI_PURPLE + "esperando turno");

        JsonObject response = Json.object();
        response.add("state", "IDLE");
        
        this.sendMessage(controllerAgent, response.toString(), ACLMessage.INFORM, conversationID, "", "");
        ArrayList<String> message = this.receiveMessage();
        String performativa = message.get(0);
        
       
            
        this.sendMessage(controllerAgent, information_package.toString(), ACLMessage.INFORM,this.conversationID , "", "");
        state=WAIT_CONTROLLER_COMMAND;
        
        
    }
    
    private void waitControllerCommand() {
                System.out.println(ANSI_PURPLE + "esperando comando");

        ArrayList<String> controller_response = this.receiveMessage();
        
        String performativa = controller_response.get(0);
        String content = controller_response.get(3);
        
        System.out.println(performativa);
        System.out.println(content);
        
        if(performativa.equals("REQUEST")){
            JsonObject json_content = Json.parse(content).asObject();
            
            if(!json_content.getString("next_pos", "unknown").equals("unknown")){ // Si tiene next_pos
                this.netx_pos = json_content.getString("next_pos","unknown");
                this.state = SEND_COMMAND_TO_SERVER;
            }else{                                         // Si tiene "command" FINISH
                this.state= FINISH;
            }
        }

    }
    
    private void sendCommandToServer() {
                System.out.println(ANSI_PURPLE + "enviando comando a server");

        JsonObject message = Json.object();
        
        if(this.battery < UMBRAL_BATERIA){
            message.add("command", "refuel");
            this.sendMessage(serverAgent, message.toString(), ACLMessage.REQUEST, conversationID, this.reply_with_server, "");
        }else{
            
            message.add("command", this.netx_pos);
            this.sendMessage(serverAgent, message.toString(), ACLMessage.REQUEST, conversationID, this.reply_with_server, "");
        }
        
        ArrayList<String> response = this.receiveMessage();
        
        String performativa = response.get(0);
        String content = response.get(3);
        
        System.out.println(performativa);
        System.out.println(content);
        
        this.state = REQUEST_WORLD_INFO;
        
    }
    
    public ArrayList<Integer> calcularAbiertos(){
        ArrayList<Integer> abiertos = new ArrayList<>();
        
        for(int i=0; i<this.range; i++)
            abiertos.add(this.posiciones_Radar.get(i));
        
        for(int i=1; i < this.range-1; i++){
            abiertos.add(this.posiciones_Radar.get(this.range*i));
            abiertos.add(this.posiciones_Radar.get((this.range*(i+1))-1));
        }
        
        for(int i=0; i < this.range; i++){
            abiertos.add(this.posiciones_Radar.get((this.range*(this.range-1))+i));
        }
        
        
        return abiertos;
    }
    
    public ArrayList<Integer> calcularCerrados(){
        ArrayList<Integer> cerrados = new ArrayList<>();
        
        
        for(int i=1; i < this.range-1; i++){
            for(int j=1; j < this.range-1; j++){
                cerrados.add(this.posiciones_Radar.get((this.range*i)+j));
            }
            
        }
        
         
        return cerrados;
    }

    private int obtenerPosObjetivo() {
        int posObjetivo = -1;
        
        int x = this.x-((this.range-1)/2);
        int y = this.y-((this.range-1)/2);
        
        for(int i=0; i < this.radar.size(); i++){
            
            if(this.radar.get(i) == 3){
                posObjetivo = x + (y*510);
            }
            
            if(i%this.range == 0 && i!= 0){
                x = this.x-((this.range-1)/2);
                y++;
            }else{
                x++;
            }
        }
        
        return posObjetivo;
    }
    
    private JsonArray convertToJson(ArrayList<Integer> array){
        JsonArray json = Json.array();
        
        for(int i=0; i < array.size(); i++){
            json.add(array.get(i));
        }
        
        return json;
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
                case REQUEST_WORLD_INFO:
                    requestWorldInfo();
                    break;

                case WAIT_CONTROLLER_COMMAND:
                    waitControllerCommand();
                    break;

                case FINISH:
                    finish();
                break;

                case SEND_COMMAND_TO_SERVER:
                    sendCommandToServer();
                break;
                
                case WAIT_TURN:
                    waitTurn();
                break;
                    
            }
            
           
        }
       System.out.println(ANSI_PURPLE+"------- TRUCK FINISHED -------");
    }
    
    public ArrayList<Integer> convertRadarToPositions(){
        ArrayList<Integer> posiciones = new ArrayList<>();
        
        int tamanio = 0;
        
        if(radar.size() == 9){
            tamanio = 1;
        }
        else if(radar.size() == 25){
            tamanio = 2;
        }    
        else{
            tamanio = 5;
        }
        
        int index = 0;
            for(int i = this.y-tamanio; i <= this.y+tamanio; i++)
                for(int j = this.x-tamanio; j <= this.x+tamanio; j++){
                    posiciones.add(i*510+j);
                }
        
        return posiciones;
    }

 
    
}
