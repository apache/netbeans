package beans;

import java.util.TooManyListenersException;

public class Source {

    public void setX(int i){}    
    public int getX(){return 1;}
    
    public void setY(int i,int a){}    
    public int getY(int a){return 1;}
    
    public void setY(int[] a){}    
    public int[] getY(){return null;}
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {}
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {}
    
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener listener) {}
    public void addVetoableChangeListener(java.beans.VetoableChangeListener listener) throws TooManyListenersException {}
    
    public int method(){return 1;}
}
