/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        return rects.toArray(new Rectangle[rects.size()]);
    }
    
}
