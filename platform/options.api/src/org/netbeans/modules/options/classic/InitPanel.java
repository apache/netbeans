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

package org.netbeans.modules.options.classic;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.openide.util.AsyncGUIJob;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Radek Matous
 */
public class InitPanel extends JPanel implements AsyncGUIJob {

    private javax.swing.JLabel initComponent;
    private OptionsAction.OptionsPanel oPanel;
    private static InitPanel defInstance;

    static InitPanel getDefault(OptionsAction.OptionsPanel oPanel) {
        if (defInstance == null) {
            defInstance = new InitPanel(oPanel);
        }
        return defInstance;
    }

    private InitPanel(OptionsAction.OptionsPanel oPanel) {
        super();
        this.oPanel = oPanel;        
        initComponents();
    }   

    protected void initComponents() {        
        if (!oPanel.isPrepared()) {
            initComponent = new JLabel(NbBundle.getMessage(InitPanel.class, "LBL_computing")); // NOI18N
            initComponent.setPreferredSize(new Dimension(850, 450));
            // avoid flicking ?
            Color c = UIManager.getColor("Tree.background"); // NOI18N
            if (c == null) {
                //GTK 1.4.2 will return null for Tree.background
                c = Color.WHITE;
            }
            initComponent.setBackground(c);    // NOI18N               
            initComponent.setHorizontalAlignment(SwingConstants.CENTER);
            initComponent.setOpaque(true);
            
            CardLayout card = new CardLayout();
            setLayout(card);            
            add(initComponent, "init");    // NOI18N
            card.show(this, "init"); // NOI18N        
            Utilities.attachInitJob(this, this);
        } else {
            finished();  
        }
    }
    
    public void construct() {
        oPanel.prepareNodes();        
    }

    public void finished() {
        //initComponent.setBackground((Color) javax.swing.UIManager.getDefaults().get("Tree.background"));    // NOI18N
        add(oPanel, "ready");   // NOI18N                
        CardLayout card = (CardLayout) getLayout();
        card.show(this, "ready"); // NOI18N            
        oPanel.requestFocus(); // #44487
    }
}
