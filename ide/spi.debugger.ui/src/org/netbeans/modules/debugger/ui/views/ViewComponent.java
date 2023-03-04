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
package org.netbeans.modules.debugger.ui.views;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Martin Entlicher
 */
public class ViewComponent extends JComponent implements org.openide.util.HelpCtx.Provider {
    
    private String icon;
    private String name;
    private String helpID;
    private String propertiesHelpID;
    private transient JComponent contentComponent;
    private ViewModelListener viewModelListener;
    
    public ViewComponent(String icon, String name, String helpID, String propertiesHelpID) {
        this.icon = icon;
        this.name = name;
        this.helpID = helpID;
        this.propertiesHelpID = propertiesHelpID;
        initComponents();
    }
    
    private void initComponents() {
        setLayout (new BorderLayout ());
        contentComponent = new javax.swing.JPanel(new BorderLayout ());
        add (contentComponent, BorderLayout.CENTER);  //NOI18N
        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorderPainted(true);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
        toolBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                javax.swing.UIManager.getDefaults().getColor("Separator.background")),
                javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                javax.swing.UIManager.getDefaults().getColor("Separator.foreground"))));
        add(toolBar, BorderLayout.WEST);
        JComponent buttonsPane = toolBar;
        viewModelListener = new ViewModelListener (
            name,
            contentComponent,
            buttonsPane,
            propertiesHelpID,
            ImageUtilities.loadImage(icon)
        );
    }

    @Override
    public void removeNotify() {
        if (viewModelListener != null) {
            viewModelListener.destroy ();
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(helpID);
    }
}
