/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.test.java.editor.breadcrumbs;

import java.io.FileReader;
// >

/**
 *
 * @author jprox
 * @param <T>
 */
public class Breadcrumbs<T> {
// Breadcrumbs >

    int x; // Breadcrumbs > x

    public void method(T in, int x) {
        // Breadcrumbs > method >
        for (int i = 0; i < 10; i++) {
            // Breadcrumbs > method > for (int i = 0; i < 10; i++) > 
        }
        try (FileReader rd = new FileReader("")/* Breadcrumbs > method > try > rd > */) {
            // Breadcrumbs > method > try
        } catch (Exception ex) {
            // Breadcrumbs > method > try > catch Exception ex >
            ex.printStackTrace();
        } finally {
            // Breadcrumbs > method > try > finally >
            System.out.println("Clossing");
        }
        while (x > 0) {
            //Breadcrumbs > method > while (x>0) >
            x--;
        }

        do {
            //Breadcrumbs > method > do ... while (x < 10) >
            x++;
        } while (x < 10);
        if (x == 10) {
            //Breadcrumbs > method > if(x == 10) >
        } else {
            //Breadcrumbs > method > if(x == 10) else >
            if (x == 2) {
                //Breadcrumbs > method > if(x == 10) else > if(x==2) >
            }
        }
        for (Object object : new String[]{""}) {
            //Breadcrumbs > method > for (Object object : new String[]{""}) >
        }
        synchronized (this) {
            // Breadcrumbs > method  > synchronized(this) >
            System.out.println("");
        }
        new Runnable() {
            //Breadcrumbs > method > Runnable >
            @Override
            public void run() {
                //Breadcrumbs > method > Runnable > run
            }
        };
        switch (x) {
            //Breadcrumbs > method > switch(x) >
            case 1:
                System.out.println("1");
                //Breadcrumbs > method > switch(x) > case 1: >
                break;
            default:
                //Breadcrumbs > method > switch(x) > default: >
                System.out.println("default");                
        }
    }

    class Inner {
        //Breadcrumbs > Inner

        public void innerMethod() {
            //Breadcrumbs > Inner > innerMethod >
        }
    }

    enum E {
        //Breadcrumbs > E

        A {
            //Breadcrumbs > E > A > E
            public void m() {
                //Breadcrumbs > E > A > E > m >
            }
        },
        B/* Breadcrumbs > E > B > */;
    }
}
// >
