/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebathetaestrella;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Ruben
 */
public class PruebaThetaEstrella {
    private static int vision = 3;
    private static int num_iteraciones = 0;
    private static int x_actual = 1;
    private static int y_actual = 1;
    private static boolean encontradoObjetivo = false;
    
    
    private static int m_real=500;
    private static int n_real=500;
    
    
    private static int x_objetivo;
    private static int y_objetivo;
    private static final String MAPA = "map11";
    private static ArrayList<Integer> map_visto = new ArrayList<>();
    private static ArrayList<Integer> map_real = new ArrayList<>();
    
    private static ArrayList<MapPoint> abiertos = new ArrayList<>();
    private static ArrayList<MapPoint> cerrados = new ArrayList<>();
    private static ArrayList<MapPoint> traza = new ArrayList<>();
    
    public static void loadMap(String mapName){
        Scanner sc;
        
        
        try {
            sc = new Scanner(new BufferedReader(new FileReader(mapName+".map")));
            String[] line = sc.nextLine().trim().split(" ");
      
            m_real = Integer.valueOf(line[0]);
            n_real = Integer.valueOf(line[1]);
            x_objetivo = Integer.valueOf(line[2]);
            y_objetivo = Integer.valueOf(line[3]);
            
            while(sc.hasNextLine()) {
              for (int i=0; i<(m_real); i++) {
                  line = sc.nextLine().trim().split(",");
                 for (int j=0; j<line.length; j++) {
                    map_real.add( Integer.parseInt(line[j]));
                 }
              }
           }
            
            //Poner el mapa que ve a 1
            for(int i = 0; i < m_real*m_real; i++){
                map_visto.add(1);
            }
            
        } catch (FileNotFoundException ex) {

           // initMap(map);
            System.out.println("No existe mapa, se utilizan valores por defecto");
        }
      
    }
    
    public static void printMap(){
        ArrayList<Integer> map_aux = new ArrayList<>(map_real);
     
        for(int i = 0; i < abiertos.size(); i++){
            map_aux.set(abiertos.get(i).y * m_real + abiertos.get(i).x, 5);
        }
        
        for(int i = 0; i < cerrados.size(); i++){
            map_aux.set(cerrados.get(i).y * m_real + cerrados.get(i).x, 4);
        }
        
        map_aux.set(y_actual * m_real + x_actual, 9);
                
        
        System.out.println("MAP REAL");
        for(int i = 0; i < m_real; i++){
            for(int j = 0; j < m_real; j++){
                System.out.print(map_aux.get(i * m_real + j) + " ");
            }
            System.out.print("\n");
        }
        
        map_aux = new ArrayList<>(map_visto);
     
        for(int i = 0; i < abiertos.size(); i++){
            map_aux.set(abiertos.get(i).y * m_real + abiertos.get(i).x, 5);
        }
        
        for(int i = 0; i < cerrados.size(); i++){
            map_aux.set(cerrados.get(i).y * m_real + cerrados.get(i).x, 4);
        }
        
        map_aux.set(y_actual * m_real + x_actual, 9);
                
     /*   
        System.out.println("MAP VISTO");
        for(int i = 0; i < m_real; i++){
            for(int j = 0; j < m_real; j++){
                System.out.print(map_aux.get(i * m_real + j) + " ");
            }
            System.out.print("\n");
        }
        */
    }
    
    public static void actuarOrden(String orden){
        num_iteraciones++;
        traza.add(new MapPoint(x_actual, y_actual));
        System.out.println("NUMITERACIONES: " + num_iteraciones);
            if(orden == "moveN"){
                y_actual--;
            }
            if(orden == "moveNW"){
                y_actual--;
                x_actual--;
            }
            if(orden == "moveW"){
                
                x_actual--;
            }
            if(orden == "moveSW"){
                y_actual++;
                x_actual--;
            }
            if(orden == "moveS"){
                y_actual++;
                
            }
            if(orden == "moveSE"){
                y_actual++;
                x_actual++;
            }
            if(orden == "moveE"){
                
                x_actual++;
            }
            if(orden == "moveNE"){
                y_actual--;
                x_actual++;
            }
    }
    
    public static MapPoint abiertoMasCercano(){
   
        MapPoint pMasCercano = abiertos.get(0);
        MapPoint pActual = new MapPoint(x_actual, y_actual);
        double distanciaMenor = distance(pMasCercano, pActual);
        
        for(int i = 1; i < abiertos.size(); i++){
            if(distance(abiertos.get(i),pActual) < distanciaMenor){
                distanciaMenor = distance(abiertos.get(i),pActual);
                pMasCercano = abiertos.get(i);
            }
        }
        
        return pMasCercano;
    }
    
    public static double distance(MapPoint p1, MapPoint p2){
        int xValue = (p1.x-p2.x)*(p1.x-p2.x);
        int yValue = (p1.y-p2.y)*(p1.y-p2.y);
        return Math.sqrt(xValue+yValue);
    } 
    
    public static void receiveRadar(){
        //Bucle que actualiza mapa
        for(int i = x_actual-vision; i <= x_actual+vision; i++){
            for(int j = y_actual-vision; j <= y_actual+vision; j++){
                if(i<0 || i >= m_real || j<0 || j>=m_real){
                    continue;   //siguiente iteracion si se sale de los limites
                }
                
                map_visto.set(j * m_real + i, map_real.get(j * m_real + i));
                
            }
        }
        
        for(int i = x_actual-vision; i <= x_actual+vision; i++){
            for(int j = y_actual-vision; j <= y_actual+vision; j++){
                //Actualiza mapa
                if(i<0 || i >= m_real || j<0 || j>=m_real){
                    continue;   //siguiente iteracion si se sale de los limites
                }

                 MapPoint punto = new MapPoint(i,j);
                 if(map_real.get(j * m_real + i) == 2){
                     encontradoObjetivo = true;
                 }
                 
                //Comprobar si es un limite de su campo de visión (si va a abiertos o no
                if(i == x_actual-vision || i == x_actual+vision || j == y_actual-vision || j == y_actual+vision){
                    if((map_real.get(j * m_real + i) == 1) || (map_real.get(j * m_real + i) == -1)){
                        if(!cerrados.contains(punto)){
                            if(abiertos.contains(punto)){
                                abiertos.remove(abiertos.indexOf(punto));
                            }
                            cerrados.add(punto);
                        }
                    }
                    
                    else{
                        if(!cerrados.contains(punto) && !abiertos.contains(punto)){
                            ThetaStar ts = new ThetaStar(m_real,map_visto);
                            ArrayList<MapPoint> path = ts.calculateThetaStar(new MapPoint(x_actual, y_actual), punto);
                            if(path != null){
                                abiertos.add(punto);
                            }
                        }
                    }
                }
                
                else{
                   
                    if(!cerrados.contains(punto)){
                        if(abiertos.contains(punto)){
                            abiertos.remove(abiertos.indexOf(punto));
                        }
                        cerrados.add(punto);
                    }
                }
            }
        }
    }
    
    public static void PrintMapImage(){
        
        byte [][] a = new byte[m_real][n_real];
        
        for(int i = 0; i < m_real; i++)
            for(int j = 0; j < n_real; j++){
                if((map_visto.get(i*m_real+j) == 1) || ((map_visto.get(i*m_real+j) == -1)))
                a[i][j] = 0;
                else a[i][j] = 1;
            }
        
        byte raw[] = new byte[m_real * n_real];
        for (int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, raw, i*m_real, n_real);
        }

        byte levels[] = new byte[]{0, -1};
        BufferedImage image = new BufferedImage(m_real, n_real, 
                BufferedImage.TYPE_BYTE_INDEXED,
                new IndexColorModel(8, 2, levels, levels, levels));
        DataBuffer buffer = new DataBufferByte(raw, raw.length);
        SampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, m_real, n_real, 1, m_real * 1, new int[]{0});
        Raster raster = Raster.createRaster(sampleModel, buffer, null);
        image.setData(raster);
        try {
            ImageIO.write(image, "png", new File("test_BN"+MAPA+"it"+num_iteraciones+".png"));
        } catch (IOException ex) {
          //  Logger.getLogger(AgentExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
    }
    
    public static void DrawColor(){
       //image dimension
       int width = m_real;
       int height = m_real;
       //create buffered image object img
       BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
       //file object
       File f = null;
       //create random image pixel by pixel
       for(int y = 0; y < height; y++){
           //Pintar mapa blanco y negro
         for(int x = 0; x < width; x++){
           int a = 255; //alpha
           int r = 255;
           int g = 255;
           int b = 255;

           int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

           img.setRGB(y, x, p);
         }
       }
       
              
        for(int y = 0; y < height; y++){
           //Pintar mapa de abiertos y cerrados
            for(int x = 0; x < width; x++){

                if(abiertos.contains(new MapPoint(y,x))){
                 int a = 255; //alpha
                 int r = 255;
                 int g = 255;
                 int b = 0;

                 int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                 img.setRGB(y, x, p);
                }
                
            if(cerrados.contains(new MapPoint(y,x))){
                 int a = 255; //alpha
                 int r = 255;
                 int g = 0;
                 int b = 0;

                 int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                 img.setRGB(y, x, p);
                }
            
                if((map_visto.get(x*m_real+y) == 1) || (map_visto.get(x*m_real+y) == 1)){
                    int a = 255; //alpha
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                    img.setRGB(y, x, p);
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
           if(traza.contains(new MapPoint(y,x))){

            int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

            img.setRGB(y, x, p);
           }
         }
         
         //pintar posicion acutal
           int a = 255; //alpha
           int r = 0;
           int g = 0;
           int b = 255;
           int p = (a<<24) | (r<<16) | (g<<8) | b;
           img.setRGB(x_actual, y_actual, p);
       }
       //write image
       try{
           ImageIO.write(img, "png", new File("test_COLOR"+MAPA+"it"+num_iteraciones+".png"));
         //f = new File("C:\\Cuarto\\Output.png");
       //  ImageIO.write(img, "png", f);
       }catch(IOException e){
         System.out.println("Error: " + e);
       }
    }//main() ends here

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        loadMap(MAPA);
        System.out.println("tamanio map_real: " + map_real.size());
        ThetaStar z = new ThetaStar(m_real, map_visto);
        x_actual = 50;
        y_actual = 2;
        
        receiveRadar();
        
        System.out.println("Abiertos: " + abiertos.toString());
        System.out.println("Cerrados: " + cerrados.toString());
        System.out.println("Pruba indexOf (4,1): " + abiertos.indexOf(new MapPoint(4,1)));
     //   abiertos.remove(abiertos.remove(abiertos.indexOf(new MapPoint(4,1))));
        //De momento coge siempre el primero
        printMap();
        while(!encontradoObjetivo){
            ArrayList<MapPoint> path = null;
            MapPoint puntoObjetivo = null;
            while(path == null){
                puntoObjetivo = abiertoMasCercano();
                z = new ThetaStar(m_real, map_visto);
                path = z.calculateThetaStar(new MapPoint(x_actual, y_actual), puntoObjetivo);
                if(path == null){
                    abiertos.remove(abiertos.indexOf(puntoObjetivo));
                }
            }
            System.out.println("\n-----Yendo al punto: " + puntoObjetivo.toString());
            
            ArrayList<String> ordenes = z.convertToInstructions(path, new MapPoint(x_actual, y_actual));
            
            System.out.println("Size ordenes: " + ordenes.size());
            
            for(int i = 0; i < ordenes.size() && abiertos.contains(puntoObjetivo) && !encontradoObjetivo; i++){
        
                actuarOrden(ordenes.get(i));
                receiveRadar();
                System.out.println("\n\nOrden ejecutada: " + ordenes.get(i));
                System.out.println("PosActual- x:" + x_actual + " y:" + y_actual);
     //           System.out.println("Abiertos: " + abiertos.toString());
     //           System.out.println("Cerrados: " + cerrados.toString());
              //  printMap();
                  //      PrintMapImage();
            
            }
        }
   
        DrawColor();
        PrintMapImage();
       //  PrintMapImage();
        
        //ArrayList<MapPoint> path = null;
        
        //PRUEBAS SUPONIENDO QUE SE TIENE UNA VISION DE 3X3
        
        //ACTUALIZAR LO QUE VE
        
        
        //PRUEBAS ENCONTRAR CAMINO
        /*
        ArrayList<MapPoint> path = z.calculateThetaStar(new MapPoint(5,1), new MapPoint(5,8));
        ArrayList<String> ordenes = z.convertToInstructions(path, new MapPoint(1,1));
        
        if(path == null){
            System.out.println("CAMINO NO ENCONTRADO");
            return;
        }
        
        for(int i = 0; i < path.size(); i++){
            System.out.println("i: " + i + " Pos: " + path.get(i));
            System.out.println("i: " + i + " Orden: " + ordenes.get(i));
            actuarOrden(ordenes.get(i));
            
            
            System.out.println("X actual: " + x_actual + " Y actual: " + y_actual);
        }
        */
    }
    
}
