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

package org.netbeans.modules.welcome;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Show the welcome screen.
 * @author  Richard Gregor
 */
@ActionID(id = "org.netbeans.modules.welcome.ShowWelcomeAction", category = "Help")
@ActionRegistration(displayName = "#LBL_Action", iconBase="org/netbeans/modules/welcome/resources/welcome.gif", iconInMenu=false)
@ActionReference(path = "Menu/Help", name = "org-netbeans-modules-welcome-ShowWelcomeAction", position = 1400)
@Messages("LBL_Action=Start &Page")
public class ShowWelcomeAction implements ActionListener {

    @Override public void actionPerformed(ActionEvent e) {
        WelcomeComponent topComp = null;
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc: tcs) {
            if (tc instanceof WelcomeComponent) {                
                topComp = (WelcomeComponent) tc;               
                break;
            }
        }
        if(topComp == null){            
            topComp = WelcomeComponent.findComp();
        }
       
        topComp.open();
        topComp.requestActive();
    }
    
}
