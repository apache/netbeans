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
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
public class DialogBoundsPreserver extends WindowAdapter {

    private static final String DELIMITER = "#";        // NOI18N
    private final Preferences preferences;
    private final String key;

    public DialogBoundsPreserver(Preferences preferences, String key) {
        this.preferences = preferences;            
        this.key = key;
    }

    /**
     * Register window before it is opened to restore its bounds the next time it is opened.
     * Bounds and position are stored on close.
     */
    public static void preserveAndRestore(Window window, Preferences preferences, String key) {
        DialogBoundsPreserver preserver = new DialogBoundsPreserver(preferences, key);
        window.addWindowListener(preserver);
        // init bounds from storage before it is made visible
        preserver.windowOpened(new WindowEvent(window, WindowEvent.WINDOW_OPENED));
    }

    @Override
    public void windowOpened(WindowEvent evt) {
        Rectangle r = getDialogBounds();        
        if (r != null && checkBounds(r)) {         
            evt.getWindow().setBounds(r);            
        }                
    }

    @Override
    public void windowClosed(WindowEvent evt) {
        Rectangle r = evt.getWindow().getBounds();
        if(checkBounds(r)) {
            setDialogBounds(r);   
        }   
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
        preferences.put(key, r.getX() + DELIMITER + r.getY() + DELIMITER + r.getWidth() + DELIMITER + r.getHeight());
    }        

    private Rectangle getDialogBounds() {
        String size = preferences.get(key, DELIMITER);        
        if(size != null) {                                    
            String[] dim = size.split(DELIMITER);             
            if (dim.length != 4 || dim[0].isBlank() || dim[1].isBlank()
                                || dim[2].isBlank() || dim[3].isBlank()) {
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
        List<Rectangle> rects = new ArrayList<>(gds.length);
        for (GraphicsDevice gd : gds) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            rects.add(Utilities.getUsableScreenBounds(gc));
        }
        return rects.toArray(Rectangle[]::new);
    }
    
}
