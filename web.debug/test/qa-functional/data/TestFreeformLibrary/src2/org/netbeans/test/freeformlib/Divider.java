/*
 * TestMain2.java
 *
 * Created on June 17, 2004, 11:52 AM
 */

package org.netbeans.test.freeformlib;

/**
 *
 * @author  mkubec
 */
public class Divider {
    private double x = 0;
    private double y = 1;
    
    
    public Divider() {
    }
    public Divider(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getDivision() {
        return x/y;
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
