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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import ThetaStar.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Anton
 */
public class AgentController extends Agent{
    
    private AgentID serverAgent;
    String car1Agent_name = "car11111111111112111111111111111111111111111";
    String car2Agent_name = "car11222111111112111111111111111111111111111";
    String truckAgent_name = "truc1k1111111112111111111111111111111111111";
    String dronAgent_name = "dron1111111111112111111111111111111111111111";
    AgentID car1Agent = new AgentID(this.car1Agent_name);
    AgentID car2Agent = new AgentID(this.car2Agent_name);
    AgentID truckAgent = new AgentID(this.truckAgent_name);
    AgentID dronAgent = new AgentID(this.dronAgent_name);
    
    private boolean finish = false;
    
    private String conversationID;
    
    private boolean DEBUG = true;
    
    private static final int SUSCRIBE = 0;
    private static final int AWAKE_AGENTS = 1;
    private static final int REQUEST_CHECKIN = 2;
    private static final int WAIT_CHECKIN = 3;
    private static final int WAIT_IDLE = 4;
    private static final int REQUEST_INFO = 7;
    private static final int UPDATE_INFO = 8;
    private static final int SELECT_POSITION = 9;
    private static final int FINISH = 5;
    private static final int FINISH_ERROR=11;
    private static final int SEND_COMMAND = 6;
    private static final int NEXT_ITERATION= 13;
    
    private int state;
    private AgentDron agentDron;
    private AgentTruck agentTruck;
    private AgentCar agentCar2;
    private AgentCar agentCar1;
    private AgentID controllerAgent;
    
    ////////////////////////////////////////////
    private int turnoActual = 0;
    private ArrayList<AgentID> arrayVehiculos = new ArrayList<>();
    private ArrayList<Integer> abiertos = new ArrayList<>();
    private ArrayList<Integer> cerrados = new ArrayList<>();
    private HashMap<Integer, ArrayList<AgentID>> abiertosFinal = new HashMap<>();
    private HashMap<Integer, ArrayList<AgentID>> cerradosFinal = new HashMap<>();
    private ArrayList<Integer> mapa = new ArrayList<>();
    private ArrayList<Integer> radar = new ArrayList<>();
    private int posicionVehiculoX;
    private int posicionVehiculoY;
    private int objetivePos;
    
    MapPoint nextObj;
    private ArrayList<MapPoint> vehiclesPositions = new ArrayList<>(4); 
    private ArrayList<MapPoint> nextPositions = new ArrayList<>(4);    

    
    /////////////////////////////////////////////
   // private Map<Integer, Integer> mapAbiertos = new Map<Integer, Integer>();
    ////////////////////////////////////////////
    
    private static int tamanio_mapa = 510;
    private  static int m = tamanio_mapa;
    private  static int n = tamanio_mapa;
    
    public AgentController(AgentID aid, AgentID server_id) throws Exception {
        super(aid);
        this.arrayVehiculos.add(this.car1Agent);
        this.arrayVehiculos.add(this.car2Agent);
        this.arrayVehiculos.add(this.truckAgent);
        this.arrayVehiculos.add(this.dronAgent);
        
        this.vehiclesPositions.add(new MapPoint(0,0));
        this.vehiclesPositions.add(new MapPoint(0,0));
        this.vehiclesPositions.add(new MapPoint(0,0));
        this.vehiclesPositions.add(new MapPoint(0,0));
        
        this.nextPositions.add(new MapPoint(0,0));
        this.nextPositions.add(new MapPoint(0,0));
        this.nextPositions.add(new MapPoint(0,0));
        this.nextPositions.add(new MapPoint(0,0));
        
        
        this.controllerAgent = aid;
        
        this.serverAgent = server_id;
        this.state = AWAKE_AGENTS;
        
        initMap(mapa);
        
    }
  
    
    public void initMap(ArrayList<Integer> mapa){
        
        if(DEBUG)
            System.out.println(ANSI_YELLOW+"INIT MAP");
        
        for(int i = 0; i < m*5; i+=1)
            mapa.add(2);
 
        for(int i = 0; i < m-10; i+=1){
           mapa.add(2);
           mapa.add(2);
           mapa.add(2);
           mapa.add(2);
           mapa.add(2);
           for (int j = 0; j < m-10; j+=1)
               mapa.add(-1);
           mapa.add(2);
           mapa.add(2);
           mapa.add(2);
           mapa.add(2);
           mapa.add(2);
        }
        
        for(int i = 0; i < m*5; i+=1)
            mapa.add(2);
        
    }
    
   
    public MapPoint iniciarMapPoint(int a){
        int x = a%tamanio_mapa;
        int y = a/tamanio_mapa;
        MapPoint resultado = new MapPoint(x,y);
        return resultado;
    }
    
    public String transformarMapPoint(MapPoint m){
        
        System.out.println(ANSI_RED + "m: " + m.x + ","+m.y + " pos: "+ this.posicionVehiculoX + ","+ this.posicionVehiculoY);
        
        if((m.y < this.posicionVehiculoY) && (m.x < this.posicionVehiculoX))
            return "moveNW";
        else if(m.y < this.posicionVehiculoY && m.x == this.posicionVehiculoX)
            return "moveN"; 
        else if(m.y < this.posicionVehiculoY && m.x > this.posicionVehiculoX)
            return "moveNE";
        else if(m.y == this.posicionVehiculoY && m.x < this.posicionVehiculoX)
            return "moveW";
        else if(m.y == this.posicionVehiculoY && m.x > this.posicionVehiculoX)
            return "moveE";
        else if(m.y > this.posicionVehiculoY && m.x < this.posicionVehiculoX)        
            return "moveSW";   
        else if(m.y > this.posicionVehiculoY && m.x == this.posicionVehiculoX)        
            return "moveS";    
        else      
            return "moveSE";     

       /* 
        if((m.y > this.posicionVehiculoY) && (m.x > this.posicionVehiculoX))
            return "moveNW";
        else if(m.y > this.posicionVehiculoY && m.x == this.posicionVehiculoX)
            return "moveN"; 
        else if(m.y > this.posicionVehiculoY && m.x < this.posicionVehiculoX)
            return "moveNE";
        else if(m.y == this.posicionVehiculoY && m.x > this.posicionVehiculoX)
            return "moveW";
        else if(m.y == this.posicionVehiculoY && m.x < this.posicionVehiculoX)
            return "moveE";
        else if(m.y < this.posicionVehiculoY && m.x > this.posicionVehiculoX)        
            return "moveSW";   
        else if(m.y < this.posicionVehiculoY && m.x == this.posicionVehiculoX)        
            return "moveS";    
        else      
            return "moveSE";
        */
    }
   
    public double distance(MapPoint p1, MapPoint p2){
        int xValue = (p1.x-p2.x)*(p1.x-p2.x);
        int yValue = (p1.y-p2.y)*(p1.y-p2.y);
        return Math.sqrt(xValue+yValue);
    }
    
    private boolean IsOnObjetive(){
        if(radar.size() == 9){
            if(radar.get(4) == 3)
                return true;
            else
                return false;
        }
        else if(radar.size() == 25){
             if(radar.get(12) == 3)
                return true;
            else
                return false;
        }    
        else{
             if(radar.get(60) == 3)
                return true;
            else
                return false;
        }
    }
    
    private void selectPosition(){
        System.out.println(ANSI_RED + "seleccionando posición");
        if(!this.IsOnObjetive()){
            System.out.println(ANSI_RED + "No esta en el objetivo");
            Objetivo proxObj = new Objetivo();

            ArrayList<Integer> abi = new ArrayList<>();
            ArrayList<Integer> cer = new ArrayList<>();

            for(Map.Entry<Integer, ArrayList<AgentID>> entry : abiertosFinal.entrySet()){
               // System.out.println(entry.getValue().get(0));
                if(entry.getValue().contains(arrayVehiculos.get(turnoActual))){
                    System.out.println("MI ID: " + arrayVehiculos.get(turnoActual));
                    System.out.println("IDs abiertos: ");
                    for(int i = 0; i < entry.getValue().size(); i++)
                        System.out.print(entry.getValue().get(i));
                    
                    abi.add(entry.getKey());
                }
            }
            
           

            ArrayList<MapPoint> posOcupadas = new ArrayList<>();
            for(int i = 0; i < vehiclesPositions.size(); i++){
                if(vehiclesPositions.get(i) != new MapPoint(posicionVehiculoX, posicionVehiculoY)){
                    posOcupadas.add(vehiclesPositions.get(i));
                }

                posOcupadas.add(nextPositions.get(i));
            }
            
            nextObj = new MapPoint(0,0);
            
            
            ///////////////
            System.out.println(posicionVehiculoX);
            System.out.println(posicionVehiculoY);
            System.out.println(finish);
            System.out.println(objetivePos);
            System.out.println(abi);
            System.out.println(finish);
            
            System.out.print(ANSI_RED + "abiertos en mapa: ");
            for(int i=0; i < abi.size(); i++){
                System.out.print(ANSI_RED + mapa.get(abi.get(i)));
            }
            
            System.out.println(ANSI_RED + "MAPA DEL CONTROLADOR");
            //Objetivo.printMap(mapa, abi);
            //Objetivo.printMap()
            nextObj = proxObj.nextPosition(posicionVehiculoX,posicionVehiculoY, finish, objetivePos, abi,mapa, vehiclesPositions );

            System.out.println(ANSI_RED + "No esta en el objetivo");
            //////////////////////////////////////////////
            
            nextPositions.set(turnoActual, nextObj);
             
            //Mandar mensaje al vehículo
            
            JsonObject response = Json.object();

            System.out.println(nextObj.x + ","+ nextObj.y);
            System.out.println(ANSI_RED + "No esta en el objetivo");
            response.add("next_pos", transformarMapPoint(nextObj));
            System.out.println(ANSI_RED + arrayVehiculos.get(turnoActual).getLocalName() + "te envío: ");
            
            System.out.print(ANSI_RED + response.toString());

            this.sendMessage(arrayVehiculos.get(turnoActual), response.toString(), ACLMessage.REQUEST, conversationID,
                    "", "");
            
            
        }else{
            System.out.println(ANSI_RED + "ESTA EN OBJETIVO");
            JsonObject response = Json.object();

            response.add("command", "FINISH");
            
            this.sendMessage(arrayVehiculos.get(turnoActual), response.toString(), ACLMessage.REQUEST, conversationID,
                    "", "");
        }

        if(turnoActual < arrayVehiculos.size()-1){
            turnoActual++;
        }
        else{
            turnoActual = 0;
        }
        
        System.out.println("\n\n\n\n" + ANSI_RED + "**********************************************************************\n\n\n\n");
        System.out.println(ANSI_RED + "********TURNO DE " + this.arrayVehiculos.get(turnoActual).getLocalName() +"\"********\n\n\n\n");
        System.out.println(ANSI_RED + "**********************************************************************\n\n\n\n");
        this.receiveMessage(); // fin de turno
        
        
        state = WAIT_IDLE;
    }
    
    private void updateInfo(){
        System.out.println(ANSI_RED+"esta en update info");
        for(int i = 0; i < cerrados.size(); i++){
            //System.out.println(cerrados.get(i));
            ArrayList<AgentID> coincidencias = new ArrayList<>();
            if(!cerradosFinal.containsKey(cerrados.get(i))){                
                cerradosFinal.put(cerrados.get(i), coincidencias);
            }
            else{
                coincidencias = cerradosFinal.get(cerrados.get(i));
            }
            
            if(!coincidencias.contains(arrayVehiculos.get(turnoActual))){
                    coincidencias.add(arrayVehiculos.get(turnoActual));
                    cerradosFinal.put(cerrados.get(i), coincidencias);
            }
        }
        
        System.out.println(ANSI_RED + "PASA EL PRIMER FOR DE CERRADOS");
        //System.out.println(ANSI_RED+"abiertos");
        for(int i = 0; i < abiertos.size(); i++){
            //System.out.println(abiertos.get(i));
            if(!cerradosFinal.containsKey(abiertos.get(i))){
                ArrayList<AgentID> coincidencias = new ArrayList<>();
                if(!abiertosFinal.containsKey(abiertos.get(i))){                
                    abiertosFinal.put(abiertos.get(i), coincidencias);
                }
                else{
                    coincidencias = abiertosFinal.get(abiertos.get(i));
                }

                if(!coincidencias.contains(arrayVehiculos.get(turnoActual))){
                        coincidencias.add(arrayVehiculos.get(turnoActual));
                        abiertosFinal.put(abiertos.get(i), coincidencias);
                }
            }
        }
        
        System.out.println(ANSI_RED + "PASA EL SEGUNDO FOR DE ABIERTOS");
        cerrados.clear();
        abiertos.clear();
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
        System.out.println(ANSI_RED + "VA A ENTRAR EN FOR DE ESCRIBIR RADAR");
        System.out.println(ANSI_RED + "POSICION ANTES DE ENTRAR A ESCRIBIR: x:" +posicionVehiculoX + " y: " +posicionVehiculoY);
        
        System.out.print(ANSI_RED+ "radar: {");
       for(int i = 0; i < radar.size(); i++)
           System.out.print(ANSI_RED+ radar.get(i)+',');
        System.out.print(ANSI_RED+ "}");
        int index = 0;
        
        
            for(int i = posicionVehiculoY-tamanio; i <= posicionVehiculoY+tamanio; i++)
                for(int j = posicionVehiculoX-tamanio; j <= posicionVehiculoX+tamanio; j++){
                    if(mapa.get(j*m+i) ==0 && radar.get(index) ==1){
                       // if(DEBUG)
                           // System.out.println(ANSI_YELLOW+"SOBRESCRIBIENDO UN CERO CON UN UNO");
                    }
                    if(DEBUG)
                        //System.out.println(ANSI_YELLOW+radar.get(index));
                    
                    mapa.set(i*m+j, radar.get(index));
                    if(mapa.get(i*m+j) == 0){
                       // if(DEBUG)
                            //System.out.println(ANSI_YELLOW+"PUESTOS 0");
                    }
                    index+=1;
                }
        System.out.println(ANSI_RED + "ACABA UPDATE INFO Y VA A PASAR A OTRO ESTADO");
        state = SELECT_POSITION;
    }
    
    private void requestInfo(){
        abiertos.clear();
        cerrados.clear();
        radar.clear();
      //  this.sendMessage(this.arrayVehiculos.get(turnoActual), "", ACLMessage.QUERY_REF, conversationID, "", "");
        ArrayList<String> msg = this.receiveMessage();
        System.out.println(ANSI_RED + "recibe la info");
        String contenido = msg.get(3);
        
        JsonObject object = Json.parse(contenido).asObject();
        System.out.println(ANSI_RED+contenido);
        JsonArray abiertosJson = object.get("abiertos").asArray();
       // ArrayList<Integer> abiertosInt = new ArrayList<>();
        
        for(int i = 0; i < abiertosJson.size(); i++){
            //System.out.println(abiertosJson.get(i).asInt());
            abiertos.add(abiertosJson.get(i).asInt());
        }
        
        
        
        JsonArray cerradosJson = object.get("cerrados").asArray();
       // ArrayList<Integer> abiertosInt = new ArrayList<>();
        
        for(int i = 0; i < cerradosJson.size(); i++){
            cerrados.add(cerradosJson.get(i).asInt());
        }
        
        for(int i=0; i < cerrados.size(); i++){
            System.out.print(ANSI_RED + cerrados.get(i));
        }
        
        int pos_ = object.get("pos").asInt();
        MapPoint aux = iniciarMapPoint(pos_);
        posicionVehiculoX = aux.x;
        posicionVehiculoY = aux.y;
        
        System.out.println(ANSI_RED + "\npos x a guardar: " + posicionVehiculoX + " pos y a guardar: " + posicionVehiculoY);
        MapPoint pos = new MapPoint(posicionVehiculoX,posicionVehiculoY);
        System.out.println(ANSI_RED+turnoActual);
        vehiclesPositions.set(turnoActual, pos);  // MAL
        
        if(object.get("objetive_pos").asInt() != -1){
            this.objetivePos = object.get("objetive_pos").asInt();
        }
        
        JsonArray radarJson = object.get("radar").asArray();
       // ArrayList<Integer> abiertosInt = new ArrayList<>();
        
        for(int i = 0; i < radarJson.size(); i++){
            radar.add(radarJson.get(i).asInt());
        }
        
        System.out.println(ANSI_RED+"RADAR RECIBIDO: " + radar.toString());
        
        this.state = UPDATE_INFO;
    }
    
    private void waitIdle(){
      //  System.out.println(ANSI_RED + "Esperando idls");
      //  ArrayList<String> msg1 = this.receiveMessage();
      /*  System.out.println(ANSI_RED + "tiene 1 idls");
        ArrayList<String> msg2 = this.receiveMessage();
        System.out.println(ANSI_RED + "tiene 2 idls");
        ArrayList<String> msg3 = this.receiveMessage();
        System.out.println(ANSI_RED + "tiene 3 idls");
        ArrayList<String> msg4 = this.receiveMessage();
        System.out.println(ANSI_RED + "tiene 4 idls");
        */
      this.sendMessage(this.arrayVehiculos.get(this.turnoActual), "", ACLMessage.INFORM, conversationID, "", "");
      
        state = REQUEST_INFO;
        if(DEBUG){
            //if(msg1.get(3).contains("IDLE") && msg2.get(3).contains("IDLE") 
            //        && msg3.get(3).contains("IDLE") && msg4.get(3).contains("IDLE")){       //Si contiene IDLE
            //    System.out.println(ANSI_RED+"Todos a IDLE");
           // }

            //else{
             //   System.out.println(ANSI_RED+"Error en WAIT_IDLE");
            //}
        }
        
    }

    private void finish() {
        this.finish=true;
        
        // RECEIVE DE LOS COCHES ANTES DE FINALIZAR PARA QUE NO CIERRE LA SESION
        this.receiveMessage();
        this.receiveMessage();
        this.receiveMessage();
        this.receiveMessage();
        ////////////////////////////////////////////////////////////////////////
        
        this.sendMessage(this.serverAgent, "", ACLMessage.CANCEL, "","", "");
        
        ArrayList<String> agree = this.receiveMessage();
        System.out.println(agree.get(0));
        ArrayList<String> mensaje_traza = this.receiveMessage();
        
        BufferedImage im = null;
        try{
                System.out.println(ANSI_RED+"Recibiendo traza ...");

                JsonObject injson = Json.parse(mensaje_traza.get(3)).asObject();

                JsonArray array = injson.get("trace").asArray();
                byte data[] = new byte[array.size()];
                for(int i = 0; i<data.length; i++)
                    data[i] = (byte) array.get(i).asInt();

                FileOutputStream fos  = new FileOutputStream("mitraza.png");
                fos.write(data);
                fos.close();

                 im = ImageIO.read(new File("mitraza.png"));
                
                System.out.println(ANSI_RED+"TAMANIO MAPA: " + im.getWidth());
                System.out.println(ANSI_RED+"Traza guardada");
            }catch(IOException ex){
                System.out.println(ANSI_RED+"Error procesando traza");
            }
    }
    
    public void suscribe(){
        
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("world","map1");
        
        this.sendMessage(serverAgent, contenido.toString(), ACLMessage.SUBSCRIBE, "","" ,"");
        System.out.println(ANSI_RED + "Mensaje_suscripcion enviado");
        ArrayList<String> response = this.receiveMessage();
        System.out.println(ANSI_RED + "Respuesta suscripcion recicibida");
        
        String performativa = response.get(0);
        String conv_id = response.get(1);
        String reply_with = response.get(2);
        String content = response.get(3);
        
        System.out.println(performativa);
        System.out.println(content);
        System.out.println(conv_id);
        System.out.println(reply_with);
        
        if(performativa.equals("INFORM")&&content.contains("OK")){
            System.out.println(ANSI_RED + "Suscribicionado correctamente");
            
            this.conversationID = response.get(1);
            if(DEBUG)
            System.out.println(ANSI_RED+"ConversationID : " + this.conversationID);
            
            state=REQUEST_CHECKIN;
        }else{
            System.out.println(ANSI_RED+"Fallo al suscribirse");
            this.guardarTraza(content);
            
            System.out.println(response.get(3));
            state=FINISH_ERROR;
        }
        
    }

    private void awakeAgents() {
        try {
            this.agentCar1 = new AgentCar(car1Agent,this.serverAgent,this.controllerAgent);
            this.agentCar2 = new AgentCar(car2Agent,this.serverAgent,this.controllerAgent);
            this.agentTruck = new AgentTruck(truckAgent,this.serverAgent,this.controllerAgent);
            this.agentDron = new AgentDron(dronAgent,this.serverAgent,this.controllerAgent);
        } catch (Exception ex) {
            Logger.getLogger(AgentCar.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ANSI_RED+"Error inicializando agentes");
        }
        this.agentCar1.start();
        this.agentCar2.start();
        this.agentTruck.start();
        this.agentDron.start();
        
        state = SUSCRIBE;
        
    }

    private void requestCheckin() {
        
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("command","checkin");
        
        this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"" ,this.car1Agent_name+"_checkin");
        this.sendMessage(this.car2Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"" ,this.car2Agent_name+"_checkin");
        this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"" ,this.truckAgent_name+"_checkin");
        this.sendMessage(this.dronAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"",this.dronAgent_name+ "_checkin");
        
        //state=WAIT_CHECKIN;
        state = WAIT_CHECKIN;
    }

    private void waitCheckin() {
        System.out.println(ANSI_RED+"controller esperando mensaje");
        ArrayList<String> checkin1 = this.receiveMessage();
        ArrayList<String> checkin2 = this.receiveMessage();
        ArrayList<String> checkin3 = this.receiveMessage();
        ArrayList<String> checkin4 = this.receiveMessage();
        JsonObject contenido = new JsonObject(); 
        String in_reply_to;
        boolean checkin = false;
        
        
        String performativa = checkin1.get(0);
        String conv_id = checkin1.get(1);
        String reply_with = checkin1.get(2);
        String content = checkin1.get(3);
        
        System.out.println(ANSI_RED+performativa);
        System.out.println(ANSI_RED+content);
        System.out.println(ANSI_RED+conv_id);
        System.out.println(ANSI_RED+reply_with);
        
        
        if(checkin1.get(0).equals("INFORM")&&checkin1.get(3).contains("OK") && checkin1.get(4).contains("_checkin")){
            if(checkin2.get(0).equals("INFORM")&&checkin2.get(3).contains("OK") && checkin2.get(4).contains("_checkin")){
                if(checkin3.get(0).equals("INFORM")&&checkin3.get(3).contains("OK") && checkin3.get(4).contains("_checkin")){
                    if(checkin4.get(0).equals("INFORM")&&checkin4.get(3).contains("OK") && checkin4.get(4).contains("_checkin")){
                       checkin = true; 
                    }
                }
            }
        }
        //in_reply_to = checkin1.get(2);
        if(checkin){
            
            contenido = Json.object().add("command","START");
            
        
            this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.car1Agent_name+"_ejec");
            this.sendMessage(this.car2Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"",this.car2Agent_name+"_ejec");
            this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.truckAgent_name+"_ejec");
            this.sendMessage(this.dronAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"", this.dronAgent_name+"_ejec");
            
            this.state = WAIT_IDLE;
        }else{
            
            contenido = Json.object().add("command","RESET");
            
            this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.car1Agent_name+"_checkin");
            this.sendMessage(this.car2Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"",this.car2Agent_name+"_checkin");
            this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.truckAgent_name+"_checkin");
            this.sendMessage(this.dronAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.dronAgent_name+"_checkin");
           // this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "","car1_checkin");
           // this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "","truck_checkin");
         
//   this.sendMessage(this.dronAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, in_reply_to);
            
            this.sendMessage(this.serverAgent, "", ACLMessage.CANCEL, "", "", "");
            ArrayList<String> agree = this.receiveMessage();
            ArrayList<String> mensaje_traza = this.receiveMessage();
            this.state = SUSCRIBE;
        }
        
    }
    
    public void next_iteration(){
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("command","START");
            
        
        this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.car1Agent_name+"_ejec");
        
    
        state= WAIT_IDLE;
    }
    
    
     @Override
    public void execute(){
        while(!finish)
        {
            if(DEBUG)
                System.out.println(ANSI_RED+"ESTADO_CONTROLLER : " + state);
             
            switch(state)
            {
                case SUSCRIBE:
                    //state = LOGIN_AGENTS;
                    suscribe();
                    break;
                case AWAKE_AGENTS:
                    awakeAgents();
                    break;
                case REQUEST_CHECKIN:
                    requestCheckin();
                    break;

                case WAIT_CHECKIN:
                    waitCheckin();
                    break;

                case WAIT_IDLE:
                    waitIdle();
                    break;
                
                case REQUEST_INFO:
                    requestInfo();
                    break;
                    
                case UPDATE_INFO:
                    updateInfo();
                    break;
                    
                case SELECT_POSITION:
                    selectPosition();
                    break;    
                case FINISH:
                    finish();
                break;

                case FINISH_ERROR:
                    finish_error();
                break;
                
                case NEXT_ITERATION:
                    next_iteration();
                break;
                    
            }
            
           
        }
       System.out.println(ANSI_RED+"------- CONTROLLER FINISHED -------");
    }
    
    public void finish_error(){ 
        this.finish=true; 
         
        this.sendMessage(this.serverAgent, "", ACLMessage.CANCEL, "","", ""); 
         
        ArrayList<String> agree = this.receiveMessage(); 
        System.out.println(agree.get(0)); 
        ArrayList<String> mensaje_traza = this.receiveMessage(); 
         
        BufferedImage im = null; 
        try{ 
                System.out.println(ANSI_RED+"Recibiendo traza ..."); 
 
                JsonObject injson = Json.parse(mensaje_traza.get(3)).asObject(); 
 
                JsonArray array = injson.get("trace").asArray(); 
                byte data[] = new byte[array.size()]; 
                for(int i = 0; i<data.length; i++) 
                    data[i] = (byte) array.get(i).asInt(); 
 
                FileOutputStream fos  = new FileOutputStream("mitraza.png"); 
                fos.write(data); 
                fos.close(); 
 
                 im = ImageIO.read(new File("mitraza.png")); 
                 
                System.out.println(ANSI_RED+"TAMANIO MAPA: " + im.getWidth()); 
                System.out.println(ANSI_RED+"Traza guardada"); 
            }catch(IOException ex){ 
                System.out.println(ANSI_RED+"Error procesando traza"); 
            } 
    } 

    private void guardarTraza(String content) {
BufferedImage im = null; 
        try{ 
                System.out.println(ANSI_RED+"Recibiendo traza ..."); 
 
                JsonObject injson = Json.parse(content).asObject(); 
 
                JsonArray array = injson.get("trace").asArray(); 
                byte data[] = new byte[array.size()]; 
                for(int i = 0; i<data.length; i++) 
                    data[i] = (byte) array.get(i).asInt(); 
 
                FileOutputStream fos  = new FileOutputStream("mitraza.png"); 
                fos.write(data); 
                fos.close(); 
 
                 im = ImageIO.read(new File("mitraza_error.png")); 
                 
                System.out.println(ANSI_RED+"TAMANIO MAPA: " + im.getWidth()); 
                System.out.println(ANSI_RED+"Traza guardada"); 
            }catch(IOException ex){ 
                System.out.println(ANSI_RED+"Error procesando traza"); 
            }     }
}
