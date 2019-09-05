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
package org.netbeans.modules.form.menu;

import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADComponentCookie;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * An action which adds a JMenu to a JMenuBar
 * @author Joshua Marinacci
 */
public class InsertMenuAction extends NodeAction {

    private static String name;
    
    @Override
    public String getName() {
        if (name == null) 
            name = org.openide.util.NbBundle.getBundle(InsertMenuAction.class)
                     .getString("ACT_InsertMenu"); // NOI18N/
        return name;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            final RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            
            //find the first JMenuBar item in the palette
            PaletteItem[] items = PaletteUtils.getAllItems();
            for (PaletteItem item : items) {
                Class<?> clazz = item.getComponentClass();
                if (clazz != null && JMenu.class.isAssignableFrom(clazz)) {
                    final PaletteItem it = item;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            MenuEditLayer.addComponentToEndOfMenu(metacomp, it);
                        }
                    });
                    return;
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.containers.designing"); // NOI18N
    }
}
