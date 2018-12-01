/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gugelvehicles;

import DBA.SuperAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;

/**
 * @author Antonio José Camarero Ortega
 * 
 * Clase de la que eredan todos los agentes.
 * Define funciones para el envio y recepción de mensajes
 * entre agentes.
 */
public class Agent extends SuperAgent{
    
    public static final String ANSI_RESET = "\033[30m";
    public static final String ANSI_RED = "\033[31m";
    public static final String ANSI_GREEN = "\033[32m";
    public static final String ANSI_YELLOW = "\033[33m";
    public static final String ANSI_BLUE = "\033[34m";
    public static final String ANSI_PURPLE = "\033[35m";

    
    public Agent(AgentID aid) throws Exception {
        super(aid);
    }
    
    /**
     * @author Antonio José Camarero Ortega
     * 
     * Función que devuelve el nombre local del agente.
     * 
     * @return Nombre local del agente.
     */
    public String getName(){
        return this.getAid().getLocalName();
    }
    
    /**
     * @author Antonio José Camarero Ortega
     * 
     * Función que envia un mensaje a otro agente.
     * 
     * @param sendTo nombre del agente que recibirá el mensaje.
     * @param content contenido del mensaje.
     */
    public void sendMessage(AgentID sendTo, String content,int performative, String conversation_id, String inReplyTo, String replyWith){
        ACLMessage outbox = new ACLMessage();
		
        outbox.setSender(this.getAid());
        outbox.setReceiver(sendTo);
        outbox.setContent(content);
        outbox.setPerformative(performative);
        outbox.setConversationId(conversation_id);
        outbox.setInReplyTo(inReplyTo);
        outbox.setReplyWith(replyWith);
		
        this.send(outbox);
    }
    
    
    /**
     * @author Antonio José Camarero Ortega
     * 
     * Función que recibe un mensaje de cualquier agente.
     * 
     * @return Contenido del mensaje recibido.
     */
    public ArrayList<String> receiveMessage(){
        try {
            ArrayList<String> message = new ArrayList<>();
            ACLMessage inbox = this.receiveACLMessage();
            
            message.add(inbox.getPerformative()); //0
            message.add(inbox.getConversationId()); //1
            message.add(inbox.getReplyWith()); //2
            message.add(inbox.getContent()); //3
            message.add(inbox.getInReplyTo());//4
            
            return message;
            
        } catch (InterruptedException ex) {
            
            System.err.println(ex.getMessage());
	    return null;
        
        }
    }
}
