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
 * Clase que maneja al agente que controla al resto de vehículos.
 * 
 * @author Rubén Marín Asunción
 */
public class AgentController extends Agent{
    
    
    ArrayList<String> nombres_mapas = new ArrayList<>();
    
    
    String nombre_mapa = "map3";
    boolean objetivoManualActivo = false;
    int objetivoManual = 0;
    
    int tamanio_real_mapa;
    
    private AgentID serverAgent;
    String car1Agent_name = "car1_144244";
    String car2Agent_name = "car2_144442";
    String truckAgent_name = "truck_142444";
    String car3Agent_name = "car3_144424";
    AgentID car1Agent = new AgentID(this.car1Agent_name);
    AgentID car2Agent = new AgentID(this.car2Agent_name);
    AgentID truckAgent = new AgentID(this.truckAgent_name);
    AgentID car3Agent = new AgentID(this.car3Agent_name);
    
    private boolean finish = false;
    
    private String conversationID;
    
    private boolean DEBUG = true;
    private int numeroIteraciones = 0;
    
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
    private Vehicle agentCar3;
    private Vehicle agentTruck;
    private Vehicle agentCar2;
    private Vehicle agentCar1;
    private AgentID controllerAgent;
    
    private int turnoActual = 0;
    private ArrayList<AgentID> arrayVehiculos = new ArrayList<>();
    private ArrayList<Integer> abiertos = new ArrayList<>();
    private ArrayList<Integer> cerrados = new ArrayList<>();
    private HashMap<Integer, ArrayList<AgentID>> MapaAbiertos = new HashMap<>();
    private HashMap<Integer, ArrayList<AgentID>> MapaCerrados = new HashMap<>();
    private ArrayList<Integer> mapa = new ArrayList<>();
    private ArrayList<Integer> radar = new ArrayList<>();
    
    
    private int posicionVehiculoX;
    private int posicionVehiculoY;
    private int objetivePos;
    
    MapPoint nextObj;
    private ArrayList<MapPoint> vehiclesPositions = new ArrayList<>(4); 
    private ArrayList<MapPoint> nextPositions = new ArrayList<>(4);  
    
    ArrayList<ArrayList<AgentID>> coincidencias = new ArrayList<>();
    
    private static int tamanio_mapa = 510;
    private  static int m = tamanio_mapa;
    private  static int n = tamanio_mapa;
    private boolean goal;
    private boolean fly = false;
    
    
    /**
     * 
     * Constructor con parámetros
     * 
     * @param aid ID del agente controlador
     * @param server_id ID del servidor
     * @throws Exception 
     * 
     * @author Rubén Marín Asunción
     */
    
    public AgentController(AgentID aid, AgentID server_id) throws Exception {
        super(aid);
        
        this.nombres_mapas.add("map1");
        this.nombres_mapas.add("map2");
        this.nombres_mapas.add("map3");
        this.nombres_mapas.add("map4");
        this.nombres_mapas.add("map5");
        this.nombres_mapas.add("map6");
        this.nombres_mapas.add("map7");
        this.nombres_mapas.add("map8");
        this.nombres_mapas.add("map9");
        this.nombres_mapas.add("map10");
        
        
        int indiceMapa = this.nombres_mapas.indexOf(this.nombre_mapa);
        
        switch(indiceMapa){
            case 0: tamanio_real_mapa = 110; break;
            case 1: tamanio_real_mapa = 110; break;
            case 2: tamanio_real_mapa = 110; break;
            case 3: tamanio_real_mapa = 110; break;
            case 4: tamanio_real_mapa = 110; break;
            case 5: tamanio_real_mapa = 160; break;
            case 6: tamanio_real_mapa = 110; break;
            case 7: tamanio_real_mapa = 110; break;
            case 8: tamanio_real_mapa = 160; break;
            case 9: tamanio_real_mapa = 510; break;
        }
        
        this.arrayVehiculos.add(this.car1Agent);
        this.arrayVehiculos.add(this.car2Agent);
        this.arrayVehiculos.add(this.truckAgent);
        this.arrayVehiculos.add(this.car3Agent);
        
        ArrayList<AgentID> coincidencias_vehiculo1 = new ArrayList<>();
        coincidencias_vehiculo1.add(this.car1Agent);
        this.coincidencias.add(coincidencias_vehiculo1);
        
        ArrayList<AgentID> coincidencias_vehiculo2 = new ArrayList<>();
        coincidencias_vehiculo2.add(this.car2Agent);
        this.coincidencias.add(coincidencias_vehiculo2);
        
        ArrayList<AgentID> coincidencias_vehiculo3 = new ArrayList<>();
        coincidencias_vehiculo3.add(this.truckAgent);
        this.coincidencias.add(coincidencias_vehiculo3);
        
        ArrayList<AgentID> coincidencias_vehiculo4 = new ArrayList<>();
        coincidencias_vehiculo4.add(this.car3Agent);
        this.coincidencias.add(coincidencias_vehiculo4);
        
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
        
        this.objetivePos = -1;
        
        initMap();
        
    }
  
    
  /**
   *Función que inicializa el mapa con información del mundo 
   * 
   * @author Rubén Marín Asunción
   */  
    
    public void initMap(){
        
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
    
   
    /**
     * Función que transforma un entero en un MapPoint
     * @param a el entero que se quiere transformar
     * @return MapPoint con los valores x e y correspondientes.
     * 
     * @author Rubén Marín Asunción
     */
    public MapPoint iniciarMapPoint(int a){
        int x = a%tamanio_mapa;
        int y = a/tamanio_mapa;
        MapPoint resultado = new MapPoint(x,y);
        return resultado;
    }
    
    
    /**
     * Función que transforma un MapPoint en un movimiento para llegar a él
     * @param m El MapPoint al que queremos mover un agente vehiculo
     * @return String con el movimiento que se debe realizar.
     * 
     * @author Rubén Marín Asunción
     */
    
    public String transformarMapPoint(MapPoint m){
        
        if(DEBUG)
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

    }
   
    
    /**
     * Función que calcula la distancia entre dos MapPoint
     * @param p1 Primer MapPoint
     * @param p2 Segundo MapPoint
     * @return Double con la distancia entre los dos MapPoint
     * 
     * @author Rubén Marín Asunción
     */
    
    public double distance(MapPoint p1, MapPoint p2){
        int xValue = (p1.x-p2.x)*(p1.x-p2.x);
        int yValue = (p1.y-p2.y)*(p1.y-p2.y);
        return Math.sqrt(xValue+yValue);
    }
    
    
    /**
     * Función que determina si el vehículo actual ha llegado a la casilla objetivo.
     * 
     * @return True si se ha pisado la casilla objetivo, false si no.
     * 
     * @author Rubén Marín Asunción
     */
    
    private boolean IsOnObjetive(){
        if(this.objetivePos == -1)
            return false;
        else{
            int posicion_vehiculo = this.posicionVehiculoY * tamanio_mapa + this.posicionVehiculoX;
    
            return posicion_vehiculo == objetivePos;

        }
    }
    
    /**
     * Función que traduce información de una posicion local del radar a una global del mapa.
     * 
     * @param i_local Int que indica la i de una casilla del radar.
     * @param j_local Int que indica la j de una casilla del radar.
     * @return Int que representa la casilla en el mapa.
     * 
     * @author Rubén Marín Asunción
     */
    
    private int convertRadarToPosition(int i_local, int j_local){
        int pos_real = 0;
        
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
        
        int i_real = this.posicionVehiculoY - tamanio + i_local;
        int j_real = this.posicionVehiculoX - tamanio + j_local;
        
        pos_real = i_real * tamanio_mapa + j_real; 
        
        
        return pos_real;
    }
    
    /**
     * Función que realiza un cambio de objetivo a otra casilla del mapa con un objetivo.
     * 
     * @author Rubén Marín Asunción
     */
    
    private void cambiaObjetivePos(){
    
        int range = 0;
        switch(this.radar.size()){
            case 9: range = 3; break;
            case 25: range = 5; break;
            case 121: range = 11; break;
        }
        
        boolean encontrado = false;
        
        
        for(int i=0; i < range && !encontrado; i++){                
            for(int j=0; j < range && !encontrado; j++){
                    if(this.radar.get((range*i)+j) == 3){
                        this.objetivePos = this.convertRadarToPosition(i, j);
                        if(DEBUG)
                            System.out.println(ANSI_RED + "El nuevo objetivo esta en " + this.objetivePos);
                        encontrado = true;
                    }
            }
                        
        }
        
    }
    
    
    /**
     * Función que con la información de nodos abiertos y cerrados calcula una
     * posición donde se puede mover un vehiculo y le ordena que se mueva. Si el
     * vehículo ha llegado al objetivo se le envía un FINISH y se elimina del array
     * de vehiculos ya que su ejecución ha finalizado.
     * 
     * @author Rubén Marín Asunción
     */
    
    private void selectPosition(){
        if(DEBUG)
            System.out.println(ANSI_RED + "seleccionando posición");
        
        
        if(!this.IsOnObjetive() && !this.goal){
            if(DEBUG)
                System.out.println(ANSI_RED + "No esta en el objetivo");
            
            Objetivo proxObj = new Objetivo();
            ArrayList<Integer> abi = new ArrayList<>();
            ArrayList<Integer> cer = new ArrayList<>();
            
            int indexCoincidenciasActual = -1;
        
            for(int i = 0; i < this.coincidencias.size(); i++){
                if(this.arrayVehiculos.get(turnoActual) == this.coincidencias.get(i).get(0) )
                   indexCoincidenciasActual = i;
            }
            
            ArrayList<AgentID> coincidencias_actual = this.coincidencias.get(indexCoincidenciasActual);
            
            if(DEBUG){
                System.out.println("Tamaño map abiertos: " + MapaAbiertos.size());
                System.out.println(ANSI_RED + "COINCIDENCIAS: ");
                System.out.print(ANSI_RED + coincidencias_actual.get(0));
            }

            for(Map.Entry<Integer, ArrayList<AgentID>> entry : MapaAbiertos.entrySet()){
                if(DEBUG)
                    System.out.print(ANSI_RED + "Entrada de abiertos: " + entry.getValue());
                
               for(int i = 0; i < coincidencias_actual.size(); i++){
                    if(entry.getValue().contains(coincidencias_actual.get(i))){                    
                        abi.add(entry.getKey());
                    }
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
            
            
            if(DEBUG){
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
            
                System.out.println(ANSI_RED + "SIZE POSOCUPADAS: " + posOcupadas.size());
                System.out.println(ANSI_RED + "POSOCUPADAS: " + posOcupadas.toString());
            }
            nextObj = proxObj.nextPosition(posicionVehiculoX,posicionVehiculoY, objetivePos,
                    abi,mapa, posOcupadas, fly);

            if(DEBUG)
                System.out.println(ANSI_RED + "nextObj: " + nextObj);
            
            nextPositions.set(turnoActual, nextObj);
            
            JsonObject response = Json.object();
            response.add("next_pos", transformarMapPoint(nextObj));

            if(DEBUG){
                System.out.println(nextObj.x + ","+ nextObj.y);
                System.out.println(ANSI_RED + "No esta en el objetivo");
                System.out.println(ANSI_RED + arrayVehiculos.get(turnoActual).getLocalName() + "te envío: ");
                System.out.print(ANSI_RED + response.toString());
            }

            this.sendMessage(arrayVehiculos.get(turnoActual), response.toString(), ACLMessage.REQUEST, conversationID,
                    "", "");
            
            
        }else{
            if(DEBUG)
                System.out.println(ANSI_RED + "ESTA EN OBJETIVO");
            
            JsonObject response = Json.object();

            response.add("command", "FINISH");
            
            this.sendMessage(arrayVehiculos.get(turnoActual), response.toString(), ACLMessage.REQUEST, conversationID,
                    "", "");
            
            this.arrayVehiculos.remove(turnoActual);
            this.turnoActual--;
            
            if(DEBUG)
                System.out.println(ANSI_RED + "ARRAY DE VEHICULOS: " + this.arrayVehiculos);
            
            this.cambiaObjetivePos();
        }

        
        
        
        
        
        if(turnoActual < arrayVehiculos.size()-1){
            turnoActual++;
            
        }
        else{
            numeroIteraciones++;
            DrawColor();
            turnoActual = 0;
        }
        
        if(DEBUG){
            System.out.println("\n\n\n\n" + ANSI_RED + "**********************************************************************\n\n\n\n");
            if(this.arrayVehiculos.size()!= 0)
            System.out.println(ANSI_RED + "********TURNO DE " + this.arrayVehiculos.get(turnoActual).getLocalName() +"\"********\n\n\n\n");
            System.out.println(ANSI_RED + "**********************************************************************\n\n\n\n");
        }
        
        this.receiveMessage(); // fin de turno
        
        vehiclesPositions.set(turnoActual, nextObj);  
        
        if(this.arrayVehiculos.size() == 0)
            this.state = FINISH;
        else
            this.state = WAIT_IDLE;
    }
    
    
    /**
     * Función para obtener una imagen del mapa de nodos abiertos y cerrados.
     * 
     * @author Rubén Marín Asunción
     */
    
    public void DrawColor(){
       //image dimension
       int width = this.tamanio_real_mapa;
       int height = this.tamanio_real_mapa;
       int tamanio_matriz = 510;
       
       //create buffered image object img
       BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
       //file object
       File f = null;
       //create random image pixel by pixel
       for(int y = 0; y < height; y++){
           //Pintar mapa blanco y negro
         for(int x = 0; x < width; x++){
           int a = 255; //alpha
           int r = 0;
           int g = 0;
           int b = 0;

           int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

           img.setRGB(x, y, p);
         }
       }
       
              
        for(int y = 0; y < height; y++){
           //Pintar mapa de abiertos y cerrados
            for(int x = 0; x < width; x++){

                if(MapaAbiertos.containsKey(y*tamanio_matriz+x)){
                 int a = 255; //alpha
                 int r = 255;
                 int g = 255;
                 int b = 0;

                 int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                 img.setRGB(x, y, p);
                }
                
            if(MapaCerrados.containsKey(y*tamanio_matriz+x)){
                 int a = 255; //alpha
                 int r = 255;
                 int g = 0;
                 int b = 0;

                 int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                 img.setRGB(x, y, p);
                }
            
                if((mapa.get(y*tamanio_matriz+x) == 1) || (mapa.get(y*tamanio_matriz+x) == -1) || (mapa.get(y*tamanio_matriz+x) == 2) || (mapa.get(y*tamanio_matriz+x) == 3)){
                    int a = 255; //alpha
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                    img.setRGB(x, y, p);
                }
            }
        }
       
        for(int y = 0; y < height; y++){
           //Pintar mapa traza
         for(int x = 0; x < width; x++){
           int a = 255; //alpha
           int r = 0;
           int g = 255;
           int b = 0;
         }
         
         //pintar posicion acutal
           int a = 255; //alpha
           int r = 0;
           int g = 0;
           int b = 255;
           int p = (a<<24) | (r<<16) | (g<<8) | b;

       }
       //write image
       try{
           ImageIO.write(img, "png", new File("./imagenes/test_COLOR"+"mapapruebas"+"it"+this.numeroIteraciones+".png"));
       }catch(IOException e){
         System.out.println("Error: " + e);
       }
    }


    /**
     * Función que actualiza la información que maneja el controlador con los datos
     * obtenidos por los vehiculos.
     * 
     * @author Rubén Marín Asunción
     */
    
    private void updateInfo(){
        if(DEBUG)
            System.out.println(ANSI_RED+"esta en update info");
        
        int indexCoincidenciasActual = -1;
        
        for(int i = 0; i < this.coincidencias.size(); i++){
            if(this.arrayVehiculos.get(turnoActual) == this.coincidencias.get(i).get(0) )
                indexCoincidenciasActual = i;
        }
                
        
        for(int i = 0; i < cerrados.size(); i++){
            //System.out.println(cerrados.get(i));
            ArrayList<AgentID> coincidencias = new ArrayList<>();
            if(!MapaCerrados.containsKey(cerrados.get(i))){                
                MapaCerrados.put(cerrados.get(i), coincidencias);
                if(MapaAbiertos.containsKey(cerrados.get(i))){ 
                    MapaAbiertos.remove(cerrados.get(i));
                } 
            }
            else{
                coincidencias = MapaCerrados.get(cerrados.get(i));
                
                if(this.numeroIteraciones == 0){
                        
                        //coincidencias = MapaAbiertos.get(abiertos.get(i));
                        for(int j = 0; j < coincidencias.size(); j++){
                            AgentID aux = coincidencias.get(j);
                            if(!this.coincidencias.get(indexCoincidenciasActual).contains(aux)){
                                if(DEBUG)
                                    System.out.println("Se juntan caminos de " + aux + " y " + this.arrayVehiculos.get(this.turnoActual) );
                                this.coincidencias.get(indexCoincidenciasActual).add(aux);
                            }
                            int indexOtro = -1;
                            for(int k = 0; k < this.coincidencias.size(); k++){
                                if(aux == this.coincidencias.get(k).get(0) )
                                indexOtro = k;
                            }
                            if(!this.coincidencias.get(indexOtro).contains(arrayVehiculos.get(turnoActual))){
                                if(DEBUG)
                                    System.out.println("Se juntan caminos de " + aux + " y " + this.arrayVehiculos.get(this.turnoActual) );
                                this.coincidencias.get(indexOtro).add(arrayVehiculos.get(turnoActual));
                            }
                        }
                    }
            }
            
            if(!coincidencias.contains(arrayVehiculos.get(turnoActual))){
                    coincidencias.add(arrayVehiculos.get(turnoActual));
                    MapaCerrados.put(cerrados.get(i), coincidencias);
            }
        }
        

        for(int i = 0; i < abiertos.size(); i++){
            if(!MapaCerrados.containsKey(abiertos.get(i))){
                ArrayList<AgentID> coincidencia = new ArrayList<>();
                if(!MapaAbiertos.containsKey(abiertos.get(i))){                
                    MapaAbiertos.put(abiertos.get(i), coincidencia);
                }
                else{
                    ArrayList<AgentID> auxs = MapaAbiertos.get(abiertos.get(i));
                    //coincidencias = MapaAbiertos.get(abiertos.get(i));
                    for(int j = 0; j < auxs.size(); j++){
                        AgentID aux = auxs.get(j);
                        if(!this.coincidencias.get(indexCoincidenciasActual).contains(aux)){
                            if(DEBUG)
                                System.out.println("Se juntan caminos de " + aux + " y " + this.arrayVehiculos.get(this.turnoActual) );
                            this.coincidencias.get(indexCoincidenciasActual).add(aux);
                        }
                        int indexOtro = -1;
                        for(int k = 0; k < this.coincidencias.size(); k++){
                            if(aux == this.coincidencias.get(k).get(0) )
                            indexOtro = k;
                        }
                        if(!this.coincidencias.get(indexOtro).contains(arrayVehiculos.get(turnoActual))){
                            if(DEBUG)
                                System.out.println("Se juntan caminos de " + aux + " y " + this.arrayVehiculos.get(this.turnoActual) );
                            this.coincidencias.get(indexOtro).add(arrayVehiculos.get(turnoActual));
                        }
                    }
                    
                    
      
                }

                if(!coincidencia.contains(arrayVehiculos.get(turnoActual))){
                        coincidencia.add(arrayVehiculos.get(turnoActual));
                        MapaAbiertos.put(abiertos.get(i), coincidencia);
                }
            }
        }
        
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

        int index = 0;
        
        /**
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
*/
        state = SELECT_POSITION;
    }
    
    
    /**
     * Función que recibe la información de un coche y la almacena en variables
     * @author Rubén Marín Asunción
     */
    private void requestInfo(){
        abiertos.clear();
        cerrados.clear();
        radar.clear();

        ArrayList<String> msg = this.receiveMessage();
        String contenido = msg.get(3);
        JsonObject object = Json.parse(contenido).asObject();
        
        if(DEBUG){
            System.out.println(ANSI_RED + "recibe la info");
            System.out.println(ANSI_RED+contenido);
        }
        
        
        
        JsonArray abiertosJson = object.get("abiertos").asArray();
        
        this.goal = object.get("goal").asBoolean();
        
        
        for(int i = 0; i < abiertosJson.size(); i++){
            abiertos.add(abiertosJson.get(i).asInt());
        }
        
        
        
        JsonArray cerradosJson = object.get("cerrados").asArray();
        
        for(int i = 0; i < cerradosJson.size(); i++){
            cerrados.add(cerradosJson.get(i).asInt());
        }
        
        
        int pos_ = object.get("pos").asInt();
        MapPoint aux = iniciarMapPoint(pos_);
        posicionVehiculoX = aux.x;
        posicionVehiculoY = aux.y;
        
 
        MapPoint pos = new MapPoint(posicionVehiculoX,posicionVehiculoY);
        
        if(DEBUG)
            System.out.println("OBJETIVO SENSORES: " + object.get("objetive_pos").asInt());
        
        if(this.objetivePos == -1){
            
            this.objetivePos = object.get("objetive_pos").asInt();
            
            if(this.objetivoManualActivo)
                this.objetivePos = this.objetivoManual; ///////////////////////////////////////ASIGNA OBJETIVO MANUAL
        }
        else{
            System.out.println(ANSI_RED+"El OBJETIVO ESTA EN "+this.objetivePos);
        }

        JsonArray radarJson = object.get("radar").asArray();
        
        for(int i = 0; i < radarJson.size(); i++){
            radar.add(radarJson.get(i).asInt());
        }
        
        if(radar.size() == 9){
            fly = true;
        }
        else{
            fly = false;
        }
        
        if(DEBUG)
            System.out.println(ANSI_RED+"RADAR RECIBIDO: " + radar.toString());
        
        this.state = UPDATE_INFO;
    }
    
    
    /**
     * Función que da paso al vehiculo que tenga el turno.
     * @author Rubén Marín Asunción
     */
    private void waitIdle(){

      this.sendMessage(this.arrayVehiculos.get(this.turnoActual), "", ACLMessage.INFORM, conversationID, "", "");
      
      state = REQUEST_INFO;
        
    }

    
    /**
     * Función que indica al servidor la finalización de la ejecución y recibe la traza.
     * 
     * @author Rubén Marín Asunción
     */
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
    
    
    /**
     * Función que elije el mapa y le envia el SUBSCRIBE al servidor.
     * 
     * @author Rubén Marín Asunción
     */
    public void suscribe(){
        
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("world", this.nombre_mapa );
        
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

    /**
     * Función que crea los agentes y los despierta.
     * 
     * @author Rubén Marín Asunción
     */
    private void awakeAgents() {
        try {
            this.agentCar1 = new AgentTruck(car1Agent,this.serverAgent,this.controllerAgent);
            this.agentCar2 = new AgentTruck(car2Agent,this.serverAgent,this.controllerAgent);
            this.agentTruck = new AgentTruck(truckAgent,this.serverAgent,this.controllerAgent);
            this.agentCar3 = new AgentTruck(car3Agent,this.serverAgent,this.controllerAgent);
        } catch (Exception ex) {
            Logger.getLogger(AgentCar.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ANSI_RED+"Error inicializando agentes");
        }
        this.agentCar1.start();
        this.agentCar2.start();
        this.agentTruck.start();
        this.agentCar3.start();
        
        state = SUSCRIBE;
        
    }

    
    /**
     * Función que envía los CHECKIN de los vehiculos.
     * 
     * @author Rubén Marín Asunción
     */
    
    private void requestCheckin() {
        
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("command","checkin");
        
        this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"" ,this.car1Agent_name+"_checkin");
        this.sendMessage(this.car2Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"" ,this.car2Agent_name+"_checkin");
        this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"" ,this.truckAgent_name+"_checkin");
        this.sendMessage(this.car3Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"",this.car3Agent_name+ "_checkin");
        
        //state=WAIT_CHECKIN;
        state = WAIT_CHECKIN;
    }

    
    /**
     * Función que espera los mensajes del servidor tras realizar los CHECKIN y les envía un
     * COMMAND START a los vehiculos si el CHECKIN ha sido correcto o le envía un
     * COMMAND RESTART si no.
     * 
     * @author Rubén Marín Asunción
     */
    
    private void waitCheckin() {
        if(DEBUG)
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
        
        if(DEBUG){
            System.out.println(ANSI_RED+performativa);
            System.out.println(ANSI_RED+content);
            System.out.println(ANSI_RED+conv_id);
            System.out.println(ANSI_RED+reply_with);
        }
        
        if(checkin1.get(0).equals("INFORM")&&checkin1.get(3).contains("OK") && checkin1.get(4).contains("_checkin")){
            if(checkin2.get(0).equals("INFORM")&&checkin2.get(3).contains("OK") && checkin2.get(4).contains("_checkin")){
                if(checkin3.get(0).equals("INFORM")&&checkin3.get(3).contains("OK") && checkin3.get(4).contains("_checkin")){
                    if(checkin4.get(0).equals("INFORM")&&checkin4.get(3).contains("OK") && checkin4.get(4).contains("_checkin")){
                       checkin = true; 
                    }
                }
            }
        }

        if(checkin){
            
            contenido = Json.object().add("command","START");
            
        
            this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.car1Agent_name+"_ejec");
            this.sendMessage(this.car2Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"",this.car2Agent_name+"_ejec");
            this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.truckAgent_name+"_ejec");
            this.sendMessage(this.car3Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"", this.car3Agent_name+"_ejec");
            
            this.state = WAIT_IDLE;
        }else{
            
            contenido = Json.object().add("command","RESET");
            
            this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.car1Agent_name+"_checkin");
            this.sendMessage(this.car2Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID,"",this.car2Agent_name+"_checkin");
            this.sendMessage(this.truckAgent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.truckAgent_name+"_checkin");
            this.sendMessage(this.car3Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.car3Agent_name+"_checkin");

            
            this.sendMessage(this.serverAgent, "", ACLMessage.CANCEL, "", "", "");
            ArrayList<String> agree = this.receiveMessage();
            ArrayList<String> mensaje_traza = this.receiveMessage();
            this.state = SUSCRIBE;
        }
        
    }
    
    /**
     * Función que indica el final de una ronda y vuelve a darle el turno al primer agente.
     * 
     * @author Rubén Marín Asunción
     */
    public void next_iteration(){
        JsonObject contenido = new JsonObject(); 
        contenido = Json.object().add("command","START");
            
        
        this.sendMessage(this.car1Agent, contenido.toString(), ACLMessage.REQUEST, this.conversationID, "",this.car1Agent_name+"_ejec");
        
    
        state= WAIT_IDLE;
    }
    
    /**
     * Función que lleva a cabo toda la lógica de los estados por los que pasa el agente controlador.
     * 
     * @author Rubén Marín Asunción
     */
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
    
    
    /**
     * Función que se ejecuta cuando se produce un error en el SUBSCRIBE y envía
     * un CANCEL para recibir la traza. Se ejecuta cuando una comunicación con el servidor
     * se ha interrumpido para recuperar la traza y resetear el estado del servidor.
     * 
     */
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

    /**
     * Función que guarda una traza.
     * @param content Contenido del mensaje devuelto por el servidor para crear
     * una traza.
     * 
     */
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
