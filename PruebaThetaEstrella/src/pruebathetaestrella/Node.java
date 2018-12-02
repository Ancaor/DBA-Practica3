/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebathetaestrella;

/**
 *
 * @author Ruben
 */
public class Node {

    public final MapPoint point;

    public Node parent;

    public int gValue; //Pasos dados desde el inicio
    public int hValue; //Distancia hasta el objetivo
    public int fValue; //Peso del nodo
    public boolean isWall = false;

    public Node(MapPoint point, Node p) {
        this.point = point;
        this.parent = p;
    }

    public void setGValue(int amount) {
        this.gValue = amount;
    }

    @Override
    public String toString() {
        return "AStarNode{" + "point=" + point + ", parent=" + parent.point + ", gValue=" + gValue + ", hValue=" + hValue + ", isWall=" + isWall + "}";
    }

    public void calculateFValue() {
        this.fValue = this.gValue + this.hValue;
    }
    
    public void calculateGValue() {
        this.gValue = this.parent.getGValue() + 1;
    }
    
    public void calculateHValue(MapPoint destino) {
        int dif_x = Math.abs(point.x - destino.x);
        int dif_y = Math.abs(point.y - destino.y);
        
        if(dif_x >= dif_y)
            this.hValue = dif_x;
        else
            this.hValue = dif_y;
        
    }
    
    
    public void calculateValues(MapPoint destino){
        calculateHValue(destino);
        calculateGValue();
        calculateFValue();
    }

    public int getFValue() {
        return this.fValue;
    }
    
    public int getGValue() {
        return this.gValue;
    }
    

    public int getHValue() {
        return this.hValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (!this.point.equals(other.point)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
