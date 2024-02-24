/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.util;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.Utilities;

/**
 *
 * Stores under a key the registered windows size and position when it si closed and
 * sets them back when the window gets open again.
 * 
 * @author Tomas Stupka
 */
public class DialogBoundsPreserver implements WindowListener {

    private static final String DELIMITER = "#";        // NOI18N
    private Preferences preferences;
    private String key;

    public DialogBoundsPreserver(Preferences preferences, String key) {
        this.preferences = preferences;            
        this.key = key;
    }

    public void windowOpened(WindowEvent evt) {
        Rectangle r = getDialogBounds();        
        if(r != null && checkBounds(r) ) {         
            evt.getWindow().setBounds(r);            
        }                
    }
    public void windowClosing(WindowEvent evt) {
        // ignore 
    }
    public void windowClosed(WindowEvent evt) {
        Rectangle r = evt.getWindow().getBounds();
        if(checkBounds(r)) {
            setDialogBounds(r);   
        }   
    }
    public void windowIconified(WindowEvent arg0) {
        // ignore
    }
    public void windowDeiconified(WindowEvent arg0) {
        // ignore
    }
    public void windowActivated(WindowEvent arg0) {
        // ignore
    }
    public void windowDeactivated(WindowEvent arg0) {
        // ignore
    }    

    private boolean checkBounds(Rectangle r) {
        Rectangle[] screens = getScreenBounds();
        for (Rectangle screen : screens) {
            if (r.getX() >= screen.getX() && r.getY() >= screen.getY()
                    && r.getX() + r.getWidth() < screen.getX() + screen.getWidth()
                    && r.getY() + r.getHeight() < screen.getY() + screen.getHeight()) {
                return true;
            }
        }
        return false;
    }   

    private void setDialogBounds(Rectangle r) {        
        preferences.put(key, r.getX() + DELIMITER + r.getY() + DELIMITER + r.getWidth() + DELIMITER + r.getHeight());         // NOI18N   
    }        

    private Rectangle getDialogBounds() {
        String size = preferences.get(key, DELIMITER);        
        if(size != null) {                                    
            String[] dim = size.split(DELIMITER);             
            if(dim.length != 4 || 
               dim[0].trim().equals("") ||                                      // NOI18N 
               dim[1].trim().equals("") ||                                      // NOI18N
               dim[2].trim().equals("") ||                                      // NOI18N    
               dim[3].trim().equals("") )                                       // NOI18N
            {
                return null;
            }
            Rectangle r = new Rectangle();
            r.setRect(Double.parseDouble(dim[0]), 
                      Double.parseDouble(dim[1]), 
                      Double.parseDouble(dim[2]), 
                      Double.parseDouble(dim[3]));
            return r;
        }
        return null;                
    }

    private Rectangle[] getScreenBounds() {
        GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        List<Rectangle> rects = new ArrayList<Rectangle>(gds.length);
        for (GraphicsDevice gd : gds) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            rects.add(Utilities.getUsableScreenBounds(gc));
        }
        return rects.toArray(new Rectangle[0]);
    }
    
}
