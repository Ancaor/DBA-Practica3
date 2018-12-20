/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ThetaStar;

import java.util.Comparator;

/**
 *
 * @author Ruben Mogica Garrido
 * @author Rubén Marín Asunción
 */
public class ComparatorNode implements Comparator<Node>
{
    
    public ComparatorNode(){}
    
    @Override
    public int compare(Node a, Node b)
    {

        if (a.getFValue() < b.getFValue())
        {
            return -1;
        }
        if (a.getFValue() > b.getFValue())
        {
            return 1;
        }
        return 0;
    }
}