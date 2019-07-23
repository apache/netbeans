package org.netbeans.test;

import java.util.*;

public class MyBean {
    
    public MyBean() {
    }

    public String getTime() {
        return Calendar.getInstance().getTime().toString();
    }
    
    public static void main(String[] args) {
        System.out.println("Current time is: "+new MyBean().getTime());
    }
}





