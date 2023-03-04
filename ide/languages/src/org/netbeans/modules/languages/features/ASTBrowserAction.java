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

package org.netbeans.modules.languages.features;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action which shows ASTBrowser component.
 */
public class ASTBrowserAction extends AbstractAction {
    
    public ASTBrowserAction () {
        super (NbBundle.getMessage (ASTBrowserAction.class, "CTL_ASTBrowserAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(ASTBrowserTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed (ActionEvent evt) {
        TopComponent win = ASTBrowserTopComponent.findInstance ();
        win.open ();
        win.requestActive ();
    }
    
}
