/*
 * Multiplier.java
 *
 * Created on 07 January 2005, 15:46
 */

package org.netbeans.test.freeformlib;

/**
 *
 * @author Administrator
 */
public class Multiplier {
    
    private double x = 0;
    private double y = 1;
    
    
    /** Creates a new instance of Multiplier */
    public Multiplier() {
    }
    
    public Multiplier(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getMultiplication() {
        return x*y;
    }
    
    
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
}
