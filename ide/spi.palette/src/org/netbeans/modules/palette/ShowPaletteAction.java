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


package org.netbeans.modules.palette;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;



/**
 * Opens Palette (Component Palette) TopComponent.
 *
 * @author S Aubrecht
 */
@ActionID(id = "org.netbeans.modules.palette.ShowPaletteAction", category = "Window")
@ActionRegistration(displayName = "#CTL_PaletteAction", iconBase = "org/netbeans/modules/palette/resources/palette.png")
@ActionReference(position = 100, name = "ShowPaletteAction", path = "Menu/Window/Tools")
public class ShowPaletteAction implements ActionListener {

    /** Opens component palette. */
    public void actionPerformed(ActionEvent evt) {
        // show ComponentPalette
        TopComponent palette = WindowManager.getDefault().findTopComponent("CommonPalette"); // NOI18N
        if( null == palette ) {
            Logger.getLogger( getClass().getName() ).log( Level.INFO, "Cannot find CommonPalette component." ); // NOI18N
                
            return;
        }
        Utils.setOpenedByUser( palette, true );
        palette.open();
        palette.requestActive();
    }
}

