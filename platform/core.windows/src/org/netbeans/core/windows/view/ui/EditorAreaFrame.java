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


package org.netbeans.core.windows.view.ui;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.netbeans.core.windows.view.Controller;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;


/**
 * Frame representing separate editor area.
 *
 * @author  Peter Zavadsky
 */
public class EditorAreaFrame extends JFrame {


    private Component desktop;
    private Controller controller;
    private long frametimestamp = 0;

    /** Creates a new instance of EditorAreaFrame */
    public EditorAreaFrame() {
        super(NbBundle.getMessage(EditorAreaFrame.class, "LBL_EditorAreaFrameTitle"));
        
        MainWindow.initFrameIcons(this);
    }
    
    public void setWindowActivationListener(Controller control) {
        controller = control;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent evt) {
                if (frametimestamp != 0 && System.currentTimeMillis() > frametimestamp + 500) {
                    controller.userActivatedEditorWindow();
                }
            }
            @Override
            public void windowOpened(WindowEvent event) {
                frametimestamp = System.currentTimeMillis();
            }
        });
    }

    @Override
    public void toFront() {
        // ignore the window activation event, is not done by user.
        frametimestamp = System.currentTimeMillis();
        super.toFront();
    }

    @Override
    public void setVisible(boolean visible) {
        frametimestamp = System.currentTimeMillis();
        super.setVisible(visible);
    }
    
    public void setDesktop(Component component) {
        if(desktop == component) {
            return;
        }
        
        if(desktop != null) {
            getContentPane().remove(desktop);
        }
        
        desktop = component;
        
        if(component != null) {
            getContentPane().add(component);
        }
    }

    private long timeStamp = 0; 
    
    public void setUserStamp(long stamp) {
        timeStamp = stamp;
    }
    
    public long getUserStamp() {
        return timeStamp;
    }
    
    private long mainWindowStamp = 0;
    
    public void setMainWindowStamp(long stamp) {
        mainWindowStamp = stamp;
    }
    
    public long getMainWindowStamp() {
        return mainWindowStamp;
    }
    
    
    
}
