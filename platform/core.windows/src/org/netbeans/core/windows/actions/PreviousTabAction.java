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


package org.netbeans.core.windows.actions;


import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;



/**
 * Action which selects previous TopComponent in container.
 *
 * @author  Peter Zavadsky
 */
public class PreviousTabAction extends AbstractAction {

    /** Creates a new instance of PreviouesTabAction */
    public PreviousTabAction() {
        putValue(NAME, NbBundle.getMessage(PreviousTabAction.class, "CTL_PreviousTabAction"));
    }

    
    public void actionPerformed(ActionEvent evt) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if(tc == null) {
            return;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);

        List openedTcs = mode.getOpenedTopComponents();

        int index = openedTcs.indexOf(tc);

        if(index == -1) {
            return;
        }

        index--; // Previous tab.

        if(index < 0) {
            index = openedTcs.size() - 1;
        }

        TopComponent select = (TopComponent)openedTcs.get(index);
        if(select == null) {
            return;
        }
        
        mode.setSelectedTopComponent(select);
        select.requestActive();
    }
}

