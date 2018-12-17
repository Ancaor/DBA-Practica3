/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ThetaStar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 *
 * @author Ruben
 */
public class ThetaStar {
    private ArrayList<Integer> map_real = new ArrayList<>();
    private final int width;
    private PriorityQueue<Node> openList;
    private ArrayList<Node> closedList;
    
        public ThetaStar(int width, ArrayList<Integer> map) {
        this.width = width;
        map_real = map;      
    }
    
    public ArrayList<MapPoint> calculateThetaStar(MapPoint start, MapPoint goal){
        System.out.println("GOAL: " + (this.map_real.get(goal.y * this.width + goal.x)));
        if((this.map_real.get(goal.y * this.width + goal.x) == 1) || (this.map_real.get(goal.y * this.width + goal.x) == -1)){
            return null;
        }
        
        Comparator<Node> comparator = new ComparatorNode();
        openList = new PriorityQueue<Node>(comparator);
        closedList = new ArrayList<Node>();
        Node currentNode = new Node(start, null);
        currentNode.parent = currentNode;
        currentNode.calculateValues(goal);
        openList.add(currentNode);
        
        while(!openList.isEmpty()){
           // System.out.println("SIZE ANTES DE POLL: " + openList.size());
            currentNode = openList.poll();
           // System.out.println("SIZE DESPUES DE POLL: " + openList.size());
            if(currentNode.point.equals(goal)){
                return reconstructPath(currentNode);
            }
            closedList.add(currentNode);

              
                for (int i = 0; i < 8; i++) {   //Recorrer 8 direcciones en sentido horario empezando por el norte
                
                    MapPoint currentPoint = currentNode.point;
                    MapPoint adjPoint ;
                    if(i == 0){     
                        adjPoint = new MapPoint(currentPoint.x, currentPoint.y-1);
                    }
                    else if(i == 1){     
                        adjPoint = new MapPoint(currentPoint.x+1, currentPoint.y-1);
                    }

                    else if(i == 2){     
                        adjPoint = new MapPoint(currentPoint.x+1, currentPoint.y);
                    }

                    else if(i == 3){    
                        adjPoint = new MapPoint(currentPoint.x+1, currentPoint.y+1);
                    }

                    else if(i == 4){    
                        adjPoint = new MapPoint(currentPoint.x, currentPoint.y+1);
                    }

                    else if(i == 5){    
                        adjPoint = new MapPoint(currentPoint.x-1, currentPoint.y+1);
                    }

                    else if(i == 6){     
                        adjPoint = new MapPoint(currentPoint.x-1, currentPoint.y);
                    }

                    else{     
                        adjPoint = new MapPoint(currentPoint.x-1, currentPoint.y-1);
                    }
                    
                    
                    if((this.map_real.get(adjPoint.y * this.width + adjPoint.x) != -1) && (this.map_real.get(adjPoint.y * this.width + adjPoint.x) != 1) && (this.map_real.get(adjPoint.y * this.width + adjPoint.x) != 2)){
                       Node adjNode = new Node(adjPoint,currentNode);
                       adjNode.calculateValues(goal);
                 //      System.out.println("ENTRA EN IF: " + this.map_real.get(adjPoint.y * this.width + adjPoint.x));
                //       System.out.println("COORDENADAS: "+ adjNode.point.toString());
                       if(!closedList.contains(adjNode)){                          
                           if (!openList.contains(adjNode)){
                               adjNode.gValue = 99999999;
                               adjNode.parent = null;
                              
                           }
                            updateVertex(currentNode, adjNode);
                       }
                    }
                }
        }
        
        return null;
    } 
    
    public void updateVertex(Node s, Node neighbor){
        //Versión Lazy Theta*
        int g_old = neighbor.gValue;
        computeCost(s, neighbor);
        if(neighbor.gValue < g_old){
     
            if(openList.contains(neighbor)){
                openList.remove(neighbor);
            }
            neighbor.calculateValues(neighbor.point);
            openList.add(neighbor);
      //      System.out.println("Nodo añadido: " + neighbor.point.toString());
        }
    }   
    
    public void computeCost(Node a, Node b){
        a.calculateValues(b.point);
        if(a.fValue < b.gValue){
            b.parent = a;
            b.gValue = a.fValue;
        }
    }
    
    public ArrayList<MapPoint> reconstructPath(Node destinationNode){
    //    System.out.println("Entra en reconstruct Path----------");
        ArrayList<MapPoint> path = new ArrayList<MapPoint>();
        Node node = destinationNode;
        while (node.parent != node) {
            path.add(node.point);
            node = node.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
    
    public ArrayList<String> convertToInstructions(ArrayList<MapPoint> points, MapPoint startingPosition){
        ArrayList<MapPoint> ordenado = new ArrayList<MapPoint>();
       
    
            for(int i=0; i<points.size(); i++){
                ordenado.add(points.get(points.size()-i-1));
            }
        
        
        ArrayList<String> result = new ArrayList<String>();

      
        if(startingPosition.x == points.get(0).x && startingPosition.y < points.get(0).y){
            result.add("moveS");
        }

        else if(startingPosition.x > points.get(0).x && startingPosition.y < points.get(0).y){
            result.add("moveSW");
        }
        
        else if(startingPosition.x > points.get(0).x && startingPosition.y == points.get(0).y){
            result.add("moveW");
        }
        
        else if(startingPosition.x > points.get(0).x && startingPosition.y > points.get(0).y){
            result.add("moveNW");
        }
        
        
        else if(startingPosition.x == points.get(0).x && startingPosition.y > points.get(0).y){
            result.add("moveN");
        }
        
        else if(startingPosition.x < points.get(0).x && startingPosition.y > points.get(0).y){
            result.add("moveNE");
        }
        
        else if(startingPosition.x < points.get(0).x && startingPosition.y == points.get(0).y){
            result.add("moveE");
        }
      
        else {
            result.add("moveSE");
        }   
        
       
        for(int i = 0; i < ordenado.size()-1; i++){
            
            //Norte
            if(points.get(i).x == points.get(i+1).x && points.get(i).y < points.get(i+1).y){
                result.add("moveS");
            }

            //Norteste
            else if(points.get(i).x > points.get(i+1).x && points.get(i).y < points.get(i+1).y){
                result.add("moveSW");
            }

            //Este
            else if(points.get(i).x > points.get(i+1).x && points.get(i).y == points.get(i+1).y){
                result.add("moveW");
            }

            //Sureste
            else if(points.get(i).x > points.get(i+1).x && points.get(i).y > points.get(i+1).y){
                result.add("moveNW");
            }

            //Sur
            else if(points.get(i).x == points.get(i+1).x && points.get(i).y > points.get(i+1).y){
                result.add("moveN");
            }

            //Suroeste
            else if(points.get(i).x < points.get(i+1).x && points.get(i).y > points.get(i+1).y){
                result.add("moveNE");
            }

            //Oeste
            else if(points.get(i).x < points.get(i+1).x && points.get(i).y == points.get(i+1).y){
                result.add("moveE");
            }

            //Oeste
            else {
                result.add("moveSE");
            }
        }
                

            System.out.println("Tamanio real result: " + result.size() );
        
        return result;
    }

}
