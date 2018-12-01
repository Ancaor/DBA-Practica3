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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public class AgentController extends Agent{
    
    private AgentID serverAgent;
    AgentID car1Agent = new AgentID("car11");
    AgentID car2Agent = new AgentID("car12");
    AgentID truckAgent = new AgentID("truck1");
    AgentID dronAgent = new AgentID("dron1");
    
    private boolean finish = false;
    
    private String conversationID;
    
    private boolean DEBUG = true;
    
    private static final int SUSCRIBE = 0;
    private static final int AWAKE_AGENTS = 1;
    private static final int REQUEST_CHECKIN = 2;
    private static final int WAIT_AGENTS = 3;
    private static final int FINISH = 5;
    private static final int SEND_COMMAND = 6;
    
    private int state;
    private AgentDron agentDron;
    private AgentTruck agentTruck;
    private AgentCar agentCar2;
    private AgentCar agentCar1;
    
    
    public AgentController(AgentID aid, AgentID server_id) throws Exception {
        super(aid);
        this.serverAgent = server_id;
        this.state = SUSCRIBE;
    }
    
    public void suscribe(){
        
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("world","map1");
        
        this.sendMessage(serverAgent, contenido.toString(), ACLMessage.SUBSCRIBE, "", "");
        
        ArrayList<String> response = this.receiveMessage();
        
        String performativa = response.get(0);
        if(performativa.equals("INFORM")){
            System.out.println(ANSI_RED + "Suscribicionado correctamente");
            
            this.conversationID = response.get(1);
            if(DEBUG)
            System.out.println(ANSI_RED+"ConversationID : " + this.conversationID);
            
            state=AWAKE_AGENTS;
        }else{
            System.out.println(ANSI_RED+"Fallo al suscribirse");
            System.out.println(response.get(3));
            state=FINISH;
        }
        
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
/*
                case WAIT_AGENTS:
                    waitAgents();
                    break;
*/
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
    }

    private void awakeAgents() {
        try {
            this.agentCar1 = new AgentCar(car1Agent);
            this.agentCar2 = new AgentCar(car2Agent);
            this.agentTruck = new AgentTruck(truckAgent);
            this.agentDron = new AgentDron(dronAgent);
        } catch (Exception ex) {
            Logger.getLogger(AgentCar.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ANSI_RED+"Error inicializando agentes");
        }
        this.agentCar1.start();
        this.agentCar2.start();
        this.agentTruck.start();
        this.agentDron.start();
        
        state = REQUEST_CHECKIN;
        
    }

    private void requestCheckin() {
        
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("command","checkin");
        
        this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "");
        this.sendMessage(this.car2Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "");
        this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "");
        this.sendMessage(this.dronAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "");
        
        state=FINISH;
    }
    
}
