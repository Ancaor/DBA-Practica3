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

/**
 *
 * @author Anton
 */
public class AgentController extends Agent{
    
    private AgentID serverAgent;
    String car1Agent_name = "car111111";
    String car2Agent_name = "car1222";
    String truckAgent_name = "truck111";
    String dronAgent_name = "dron111";
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
    private static final int FINISH = 5;
    private static final int SEND_COMMAND = 6;
    
    private int state;
    private AgentDron agentDron;
    private AgentTruck agentTruck;
    private AgentCar agentCar2;
    private AgentCar agentCar1;
    private AgentID controllerAgent;
    
    
    public AgentController(AgentID aid, AgentID server_id) throws Exception {
        super(aid);
        
        this.controllerAgent = aid;
        
        this.serverAgent = server_id;
        this.state = AWAKE_AGENTS;
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

                case FINISH:
                    finish();
                break;
/*
                case SEND_COMMAND:
                    sendCommand();
                break;
                    */
            }
            
           
        }
       System.out.println(ANSI_RED+"------- CONTROLLER FINISHED -------");
    }

    private void finish() {
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
            System.out.println(response.get(3));
            state=FINISH;
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
            
            this.state = FINISH;
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
    
}
