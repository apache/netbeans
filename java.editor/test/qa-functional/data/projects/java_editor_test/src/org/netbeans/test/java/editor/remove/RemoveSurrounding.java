package org.netbeans.test.java.editor.remove;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class RemoveSurrounding {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);            
        }
        
        if(args.length==1) {
            System.out.println("Usage:");
            System.out.println(" java -jar Dist.jar <param1>");            
        } else {
            System.out.println("ok");
        }
        
        int x = 0;
        while(true) {
            x++;
            if(x==10) break;
        }
        
        try {
            FileReader fr = new FileReader("");
        } catch(FileNotFoundException exception) {
            Logger.getGlobal().info(exception.getMessage());
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(""))) {
            String line = br.readLine();
        } catch (IOException ioe) {
            
        }        
        
        synchronized(RemoveSurrounding.class) {
            System.out.println("");
        }
    }
        
}
