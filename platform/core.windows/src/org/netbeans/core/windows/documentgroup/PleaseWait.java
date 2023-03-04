/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.windows.documentgroup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.netbeans.core.windows.WindowManagerImpl;

/**
 * A glass pane to paint over the main window while document groups are being switched.
 * 
 * @author S. Aubrecht
 */
class PleaseWait extends JPanel {
    
    private static final Color FILL_COLOR = new Color(0,0,0,128);
    private Component oldGlass;
    
    public PleaseWait() {
        setOpaque(false);
    }
    
    public void install() {
        JFrame frame = (JFrame) WindowManagerImpl.getInstance().getMainWindow();
        oldGlass = frame.getGlassPane();
        if( oldGlass instanceof PleaseWait )
            oldGlass = null;
        frame.setGlassPane( this );
        setVisible(true);
        invalidate();
        revalidate();
        repaint();
        
    }
    
    public void uninstall() {
        setVisible(false);
        JFrame frame = (JFrame) WindowManagerImpl.getInstance().getMainWindow();
        frame.setGlassPane( oldGlass );
    }
    
    @Override
    public void paint(Graphics g) {
        g.setColor( FILL_COLOR );
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
