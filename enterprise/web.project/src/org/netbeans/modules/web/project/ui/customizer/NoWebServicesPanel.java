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

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class NoWebServicesPanel extends JPanel implements HelpCtx.Provider {

    private JLabel label;

    /** Creates a new instance of NoWebServiceClientsPanel */
    public NoWebServicesPanel() {
        this(NbBundle.getMessage(NoWebServicesPanel.class, "LBL_CustomizeWsServiceHost_NoWebServices")); //NOI18N
    }
    
    /** Creates a new instance of NoWebServiceClientsPanel */
    public NoWebServicesPanel(String text) {
        setLayout(new GridBagLayout());

        label = new JLabel(text);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(label, gridBagConstraints);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerWSServiceHost.class.getName() + "Disabled"); // NOI18N
    }

}
