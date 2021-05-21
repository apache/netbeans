/*
 * SumNmbers.java
 *
 * Created on 07 January 2005, 14:48
 */

package org.netbeans.test.freeformlib;

/**
 *
 * @author Administrator
 */
public class SumNumbers {
    
    private int x = 0;
    private int y = 0;
    
    /** Creates a new instance of SumNmbers */
    public SumNumbers() {
    }
    /** Creates a new instance of SumNmbers */
    public SumNumbers(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Count the sum from parameters.
     * @param x first argument for sum.
     * @param y second argument for sum.
     */
    public static int sum(int x, int y) {
        return x + y;
    }
    
    /**
     * Count the sum from initialized variables of object.
     */
    public int getSum() {
        return x + y;
    }
    
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return x;
    }
    public void setY(int y) {
        this.y = y;
    }
}
