/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gugelvehicles;

import static gugelvehicles.Agent.ANSI_YELLOW;
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
import ThetaStar.*;

/**
 *
 * @author Ruben
 */
public class Objetivo {
    private static int vision = 3;
    private static int num_iteraciones = 0;
    private static int x_actual = 1;
    private static int y_actual = 1;
    private static boolean encontradoObjetivo = false;
    
    
    private static int x_fin = 46;
    private static int y_fin = 46;
    private static MapPoint pFin;
    
    private static int m_real=510;
    private static int n_real=510;
    
    
    private static int x_objetivo;
    private static int y_objetivo;
    private static final String MAPA = "map11";
    private static ArrayList<Integer> map_visto = new ArrayList<>();
    private static ArrayList<Integer> map_real = new ArrayList<>();
    
    private static ArrayList<MapPoint> abiertos = new ArrayList<>();
    private static ArrayList<MapPoint> cerrados = new ArrayList<>();
    private static ArrayList<MapPoint> traza = new ArrayList<>();
    
    
    private static int m = 500;
     
       
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
    
    public static MapPoint abiertoMasCercanoFin(){
   
        MapPoint pMasCercano = abiertos.get(0);
        
        double distanciaMenor = distance(pMasCercano, pFin);
        
        for(int i = 1; i < abiertos.size(); i++){
            if(distance(abiertos.get(i),pFin) < distanciaMenor){
                distanciaMenor = distance(abiertos.get(i),pFin);
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
            
                if((map_visto.get(x*m_real+y) == 1) || (map_visto.get(x*m_real+y) == -1)){
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

    
    public static void DrawTraza(){
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

            
                if((map_visto.get(x*m_real+y) == 1) || (map_visto.get(x*m_real+y) == -1)){
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
    }
    
    public MapPoint iniciarMapPoint(int a){
        int x = a/510;
        int y = a%510;
        MapPoint resultado = new MapPoint(x,y);
        return resultado;
    }
    
    public static void printMap(ArrayList<Integer> map, ArrayList<Integer> abiert){
        System.out.println(" ");
        for(int i = 0; i < 510; i++){
            for(int j = 0; j < 510; j++){
                
                System.out.print(map.get(i*510+j));
                    
                
            }
            System.out.println(" ");
        }
        
                
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
    
    public MapPoint nextPosition(int actual_x,int actual_y, boolean objetivoDefinido, int objetivo, ArrayList<Integer> abie, ArrayList<Integer> mapa, ArrayList<MapPoint> vehiclePosition) {
        
        map_visto = mapa;
        abiertos.clear();
        System.out.println("casillas abiertas: ");
        for(int i = 0; i <abie.size();i++){
            abiertos.add(iniciarMapPoint(abie.get(i)));
            System.out.print(abiertos.get(i));
        }
        
        x_actual = actual_x;
        y_actual = actual_y;
        printMap(mapa,abie);
        /*
        for(int i = 0; i <vehiclePosition.size();i++){
         map_visto.set(vehiclePosition.get(i).y*504+vehiclePosition.get(i).x, 1);           
        }
*/
        
        ThetaStar z = new ThetaStar(m_real, map_visto);
        
        //MapPoint act = new MapPoint(x_actual,y_actual);
        
        MapPoint puntoObjetivo = null;
        ArrayList<MapPoint> path = null;
        
        
        while(path == null){
            z = new ThetaStar(m_real, map_visto);
        
            if(!objetivoDefinido){
                puntoObjetivo = abiertoMasCercano();
            }else{
                
                pFin = iniciarMapPoint(objetivo);
                puntoObjetivo = abiertoMasCercanoFin();
            }
            path = z.calculateThetaStar(new MapPoint(x_actual, y_actual), puntoObjetivo);
            System.out.println("PObjetivo: " + puntoObjetivo + " Path: " + path + " que hay en ese punto: " + map_visto.get(actual_x*m_real+actual_y));
            if(path == null){
                abiertos.remove(abiertos.indexOf(puntoObjetivo));
            }else{
                puntoObjetivo = path.get(path.size()-1);
            }
        }
        
        return puntoObjetivo;
    }
    
    
}
