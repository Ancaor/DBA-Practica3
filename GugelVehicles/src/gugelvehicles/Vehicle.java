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
import java.util.ArrayList;

/**
    * Superclase define los Vehiculos y su comportamiento
    * 
    * 
    * @author Antonio José Camarero Ortega
    */

public class Vehicle extends Agent{
    
    protected boolean finish = false;
    protected final boolean DEBUG=true;
    protected int state;
    
    protected AgentID serverAgent;
    protected AgentID controllerAgent;
    
    
    ////////CAPABILITIES///////////
    
    protected int fuelrate;
    protected int range;
    protected boolean fly;
    
    protected VehicleType vehicleType;
    
    protected String vehicleTypeName;

    //////////////////////////////
    
    protected String ANSI;
    
    //
    protected int battery;
    protected int x;
    protected int y;
    protected ArrayList<Integer> radar = new ArrayList<>();
    
    //
    
    protected String reply_with_controller;
    protected String reply_with_server;
    
    
    protected static final int UMBRAL_BATERIA = 70;
    
    
    protected static final int WAIT_CONTROLLER = 0;
    protected static final int WAIT_SERVER_CHEKIN = 1;
    protected static final int REQUEST_WORLD_INFO = 2;
    protected static final int WAIT_CONTROLLER_COMMAND = 3;
    protected static final int SEND_COMMAND_TO_SERVER = 4;
    protected static final int FINISH = 5;
    protected static final int  WAIT_TURN = 6;

    protected String conversationID;
    protected int enery;
    protected boolean goal;
    protected int position;
    protected String next_pos;
    
    protected JsonObject information_package;
    protected ArrayList<Integer> posiciones_Radar;

    public Vehicle(AgentID aid, AgentID serverID, AgentID controllerID) throws Exception {
        super(aid);
        
        this.serverAgent = serverID;
        this.controllerAgent = controllerID;
        
        
        state = WAIT_CONTROLLER;
    }
    
    /**
    * Estado en el que el vehiculo espera a que el controlador le indique acciones:
    * 
    *   - Si recibe un REQUEST checkin : El vehiculo manda el comando de chekin al server y pasa al estado WAIR_SERVER_CHEKIN.
    *   - Si recibe un REQUEST start : El vehiculo puede empezar a moverse por lo que pasa a esperar su turno(WAIT_TURN).
    *   - Si recibe un REQUEST reset : El vehiculo debe resetearse por lo que vuelve a esperar un mensaje del controlador.
    * 
    * 
    * @author Antonio José Camarero Ortega
    */
    
    
    private void wait_controller() {
        
        ArrayList<String> message = this.receiveMessage();
        
        String performativa = message.get(0);
        this.conversationID = message.get(1);
        this.reply_with_controller = message.get(2);
        String content = message.get(3);
        String in_reply_to = message.get(4);
        System.out.println(performativa);
        System.out.println(content);
        System.out.println(conversationID);
        System.out.println(reply_with_controller);
        
        if(performativa.equals("REQUEST") && content.contains("checkin")){
            System.out.println(ANSI_BLUE + "Controlador solicita su suscripción");
            
            JsonObject contenido = new JsonObject(); 
            contenido = Json.object().add("command","checkin");
            System.out.println(ANSI_BLUE + "ENVIANDO MENSAJE A SERVER");
            this.sendMessage(this.serverAgent, contenido.toString(), ACLMessage.REQUEST, conversationID, "","");
            
            
            state=WAIT_SERVER_CHEKIN;
        }else if(performativa.equals("REQUEST") && content.contains("START")){
            System.out.println(ANSI_BLUE+ "Coche ya puede moverse");
            state=WAIT_TURN;
        }else if(performativa.equals("REQUEST") && content.contains("RESET")){
            System.out.println(ANSI_BLUE+ "reinicio requerido");
            state= WAIT_CONTROLLER;
        }
        
        
        
    }
    
    /**
    * Espera la respuesta del chekin del servidor
    * 
    * Una vez respondido analiza las Capabilities y las compara con las capabilities esperadas segun su tipo de vehiculo:
    * 
    *   - Si cohinciden con su tipo de vehiculo : Manda al controller el mensaje de vehicle:ok y pasa al estado WAIT_CONTROLLER.
    *   - Si NO cohinciden con su tipo de vehiculo : Manda al controller el mensaje de vehicle:error y pasa al estado WAIT_CONTROLLER.
    * 
    * @author Antonio José Camarero Ortega
    */
    
    private void wait_server_chekin() {
        
        ArrayList<String> message = this.receiveMessage();
        
        String performativa = message.get(0);
        String conv_id = message.get(1);
        this.reply_with_server = message.get(2);
        String content = message.get(3);
        
        System.out.println(performativa);
        System.out.println(ANSI+content);
        System.out.println(conv_id);
        System.out.println(reply_with_server);
        
        if(performativa.equals("INFORM") && content.contains("OK")){
            System.out.println(ANSI + "Checkin realizado con exito");
            
            JsonObject contenido = new JsonObject();
            contenido = Json.parse(content).asObject();
            
            JsonObject capabilities = contenido.get("capabilities").asObject();
            
            boolean isVehicleType = checkTipoVechivulo(this.vehicleType,capabilities.get("fuelrate"));
            
            if(isVehicleType){
                this.fuelrate = capabilities.get("fuelrate").asInt();
                this.range = capabilities.get("range").asInt();
                this.fly = capabilities.get("fly").asBoolean();
                
                System.out.println(ANSI+ "SI es un " + this.vehicleTypeName);
                contenido = Json.object().add("vehicle","OK");
                
                this.sendMessage(this.controllerAgent, contenido.toString(), ACLMessage.INFORM, conversationID , this.reply_with_controller,"");
            }else{
                contenido = Json.object().add("vehicle","ERROR");
                
                System.out.println(ANSI+ "NO es un "+this.vehicleTypeName);
                
                this.sendMessage(this.controllerAgent, contenido.toString(), ACLMessage.INFORM, conversationID , this.reply_with_controller,"");
            }
            
            state=WAIT_CONTROLLER;
        }
        
    }
    
    /**
    * Finaliza la ejecución del vehiculo
    * 
    * Manda un mensaje al controlador para indicar que acaba su turno y finaliza.
    * 
    * @author Antonio José Camarero Ortega
    */
    private void finish() {
        JsonObject message = Json.object();
        message.add("state", "FIN_TURNO");        
        this.sendMessage(controllerAgent, message.toString(), ACLMessage.INFORM, conversationID, "", "");
        
        this.finish=true;
        
    }
    
    
    /**
    * Comprueba que las capabilities asignadas al vehiculo son las que debe tener segun su tipo.
    * 
    * Se basa en el fuelrate para ver si es correcto o no.
    * 
    * 
    * @author Antonio José Camarero Ortega
    * 
    * @return check indica si es o no el tipo correcto de vehiculo.
    */
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
    
    /**
    * Solicita infrmación de lo sensores 
    * 
    * Solicita información del mundo y la empaqueta para enviarla al controlador.
    * Además de la información recibida de sensores esta función obtiene del radar la lista de nodos
    * abiertos y cerrados, tambien captura la posición del objetivo si este es localizado por el radar.
    * 
    * @author Antonio José Camarero Ortega
    */
    private void requestWorldInfo() {
        
        System.out.println(ANSI + "Solicita información del mundo");
        radar.clear();
        
        System.out.println(ANSI + "ENVIANDO MENSAJE A SERVER");
        this.sendMessage(this.serverAgent, "", ACLMessage.QUERY_REF, conversationID, this.reply_with_server,"");
        
        ArrayList<String> respuesta = this.receiveMessage();
        
        String performativa = respuesta.get(0);
        String conv_id = respuesta.get(1);
        String content = respuesta.get(3);
        this.reply_with_server =  respuesta.get(2);
        
        System.out.println(ANSI +"**Contenido del mensaje recibido del servidor**");
        
        System.out.println(ANSI + "Performativa: " + performativa);
        System.out.println(ANSI + "Contenido: " + content);
        System.out.println(ANSI + "ConversationID: " +conv_id);
        System.out.println(ANSI + "ReplyWith: " +reply_with_server);
        
        System.out.println(ANSI +"************************************************");
        
        JsonObject object = Json.parse(content).asObject();
        
        JsonObject result = object.get("result").asObject();
        
        this.battery = result.get("battery").asInt();
        System.out.println("bateria " + this.battery);
        this.x = result.get("x").asInt()+5;
        this.y = result.get("y").asInt()+5;
        this.position = this.x + (this.y * 510);
        this.goal = result.get("goal").asBoolean();
        System.out.println("x " + this.x + " y "+ this.y);
        JsonArray aux = result.get("sensor").asArray();
        
        for(int i=0; i < aux.size();i++){
            this.radar.add(aux.get(i).asInt());
        }
        
        
        
        this.posiciones_Radar = convertRadarToPositions();
        
        this.enery = result.get("energy").asInt();
        this.goal = result.get("goal").asBoolean();
        
        
        ArrayList<Integer> cerrados = calcularCerrados();
        System.out.println("cerrados");
        for(int i=0; i < cerrados.size(); i++){
            System.out.println(cerrados.get(i));
        }
        
        ArrayList<Integer> abiertos = calcularAbiertos(cerrados);
        System.out.println("abiertos");
        for(int i=0; i < abiertos.size(); i++){
            System.out.println(abiertos.get(i));
        }
       
        int pos_objetivo = obtenerPosObjetivo();
        
        information_package = Json.object();
        
  
        information_package.add("radar", this.convertToJson(radar));
        information_package.add("abiertos", this.convertToJson(abiertos));
        information_package.add("cerrados", this.convertToJson(cerrados));
        information_package.add("pos", this.position );
        information_package.add("objetive_pos", pos_objetivo);
        information_package.add("goal", this.goal);
        System.out.println(information_package.toString());
        System.out.println("-----POSICION VEHICULO: x:" + position%510 + " y:" + position/510);
        this.state = WAIT_CONTROLLER_COMMAND;
        
        
    }
    
    /**
    * Espera a que el controlador le de el turno
    * 
    * El vehiculo espera aqui hasta que el controlador le indica que es su turno.
    * 
    * @author Antonio José Camarero Ortega
    */
    private void waitTurn() {
        System.out.println(ANSI + "Esperando turno");
        ArrayList<String> message = this.receiveMessage();
        System.out.println(ANSI + "tiene el turno");
        
        state=REQUEST_WORLD_INFO;  
    }
    
    
    /**
    * Espera la orden de movimiento del controlador
    * 
    * El vehiculo manda la información del mundo empaquetada al controlador y espera
    * que este le responda con el siguiente moviemiento:
    * 
    *   -Si el controlador le responde con un next_pos : El vehiculo tiene un movimiento que realizar.
    *   -Si el controlador le responde con un finish : El vehiculo ya ha llegado al objetivo y puede parar su ejecucion.
    * 
    * @author Antonio José Camarero Ortega
    */
    private void waitControllerCommand() {
        
        this.sendMessage(controllerAgent, information_package.toString(), ACLMessage.INFORM,this.conversationID , "", "");

        ArrayList<String> controller_response = this.receiveMessage();
        
        String performativa = controller_response.get(0);
        String content = controller_response.get(3);
        
        System.out.println(performativa);
        System.out.println(content);
        
        System.out.println(ANSI + "RECIBE MENSAJE COMMAND"  + this.getAid());
                
        if(performativa.equals("REQUEST")){
            JsonObject json_content = Json.parse(content).asObject();
            
            if(!json_content.getString("next_pos", "unknown").equals("unknown")){ // Si tiene next_pos
                this.next_pos = json_content.getString("next_pos","unknown");
                this.state = SEND_COMMAND_TO_SERVER;
                System.out.println(ANSI + "Contenido next_pos: " + next_pos);
            }else{                                         // Si tiene "command" FINISH
                this.state= FINISH;
            }
        }

    }
    
    /**
    * Envia la siguiente acción/movimiento al servidor
    * 
    * Si la bateria está por debajo del umbral hace un refuel.
    * Despues de haber hecho refuel o no indica al servidor la dirección a la que se mueve.
    * Por ultimo indica al controlador el fin del turno y vuelve a esperar turno.
    * 
    * @author Antonio José Camarero Ortega
    */
    private void sendCommandToServer() {
        
        JsonObject message = Json.object();
        
        if(this.battery < UMBRAL_BATERIA){
            message.add("command", "refuel");
            System.out.println(ANSI + "ENVIANDO MENSAJE A SERVER");
            this.sendMessage(serverAgent, message.toString(), ACLMessage.REQUEST, conversationID, this.reply_with_server, "");
            ArrayList<String> response = this.receiveMessage();
            this.reply_with_server =  response.get(2);

        }
            message = Json.object();
            message.add("command", this.next_pos);
            
            System.out.println(ANSI + "conversationID: " + conversationID);
            System.out.println(ANSI + "Performativa: " + ACLMessage.REQUEST);
            System.out.println(ANSI + "Contenido: " + message.toString());
            System.out.println(ANSI + "Reply-with: " + this.reply_with_server);
            
            System.out.println(ANSI + "ENVIANDO MENSAJE A SERVER");
            this.sendMessage(serverAgent, message.toString(), ACLMessage.REQUEST , conversationID, this.reply_with_server, "");
            
        
        
        ArrayList<String> response = this.receiveMessage();
        System.out.println(ANSI + "Recibido");
        
        String performativa = response.get(0);
        String content = response.get(3);
        
        
        System.out.println(ANSI + "CONVERSATIONID SEND_COMMAND_TO_SERVER: " + conversationID);
        System.out.println(ANSI + "PERFORMATIVE SEND_COMMAND_TO_SERVER: " + performativa);
        System.out.println(ANSI + "CONTENT SEND_COMMAND_TO_SERVER: " + content);
        
        message = Json.object();
        
        message.add("state", "FIN_TURNO");
        this.reply_with_server =  response.get(2);
        
        this.sendMessage(controllerAgent, message.toString(), ACLMessage.INFORM, conversationID, "", "");
        
        this.state = WAIT_TURN;
        
    }
    
    /**
    * Calcula los nodos abiertos del vehiculo
    * 
    * Partiendo del radar pasado a posiciones, se comprueban que posciones estan en cerrados y se descartan, el resto
    * se añaden a abiertos.
    * 
    * @author Antonio José Camarero Ortega
    */
    public ArrayList<Integer> calcularAbiertos(ArrayList<Integer> cerrados){
        ArrayList<Integer> abiertos = new ArrayList<>();
        
        for(int i=0; i<this.range; i++)
            if(!cerrados.contains(this.posiciones_Radar.get(i)))
                abiertos.add(this.posiciones_Radar.get(i));
        
        for(int i=1; i < this.range-1; i++){
            if(!cerrados.contains(this.posiciones_Radar.get(this.range*i)))
                abiertos.add(this.posiciones_Radar.get(this.range*i));
            if(!cerrados.contains(this.posiciones_Radar.get((this.range*(i+1))-1)))
                abiertos.add(this.posiciones_Radar.get((this.range*(i+1))-1));
        }
        
        for(int i=0; i < this.range; i++){
            if(!cerrados.contains(this.posiciones_Radar.get((this.range*(this.range-1))+i)))
                abiertos.add(this.posiciones_Radar.get((this.range*(this.range-1))+i));
        }
        
        
        return abiertos;
    }
    
    /**
    * Pasa cada casilla del radar a una pocición del mapa
    *  
    * @author Antonio José Camarero Ortega
    */
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
    
    /**
    * Calcula nodos cerrados del vehiculo
    * 
    * Comprueba el radar y guarda en cerrados las casillas uqe no estan en el borde de la vision del vehiculo,
    * además analiza los bordes del vehiculo y mete en cerrados tambien las que son muro o limite del mapa.
    * 
    * @author Antonio José Camarero Ortega
    */
    public ArrayList<Integer> calcularCerrados(){
        ArrayList<Integer> cerrados = new ArrayList<>();
        
        
        for(int i=0; i < this.range; i++){                
            for(int j=0; j < this.range; j++){
                if(i == this.range-1 || j == this.range-1 || i == 0 || j == 0){
                    if(this.radar.get((this.range*i)+j) == 1 || this.radar.get((this.range*i)+j) == 2)
                        cerrados.add(this.posiciones_Radar.get((this.range*i)+j));
                }
                else{
                    cerrados.add(this.posiciones_Radar.get((this.range*i)+j));
                }
            }
        }
        
         
        return cerrados;
    }

    /**
    * Analiza el radar en busca del objetivo
    * 
    * @author Antonio José Camarero Ortega
    */
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
    
    /**
    * Función que convierte un ArrayList en un JsonArray
    * 
    * @author Antonio José Camarero Ortega
    */
    private JsonArray convertToJson(ArrayList<Integer> array){
        JsonArray json = Json.array();
        
        for(int i=0; i < array.size(); i++){
            json.add(array.get(i));
        }
        
        return json;
    }
    
    
    /**
    * Execute del vehiculo
    * 
    * @author Antonio José Camarero Ortega
    */
    @Override
    public void execute(){
        while(!finish)
        {
            if(DEBUG)
                System.out.println(ANSI+"ESTADO_CAR : " + state);
             
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
       System.out.println(ANSI+"------- CAR FINISHED -------");
    }
    
}
