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

package org.netbeans.modules.httpserver;

import javax.swing.*;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
* message panel for granting access to internal HTTP server.
* @author Radim Kubacki
*/
class GrantAccessPanel extends javax.swing.JPanel {

    private final String msg;

    /** Creates new panel */
    public GrantAccessPanel (String msg) {
        this.msg = msg;
        initComponents ();
    }

    private void initComponents() {
        JTextArea localTopMessage = new javax.swing.JTextArea();

        jCheckBox1 = new javax.swing.JCheckBox();
        
        setLayout(new java.awt.BorderLayout(0, 12));
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        getAccessibleContext().setAccessibleDescription(msg);
        
        localTopMessage.setLineWrap (true);
        localTopMessage.setWrapStyleWord (true);
        localTopMessage.setEditable (false);
        localTopMessage.setEnabled (false);
        localTopMessage.setOpaque (false);
        localTopMessage.setDisabledTextColor (javax.swing.UIManager.getColor ("Label.foreground"));  // NOI18N
        localTopMessage.setFont (javax.swing.UIManager.getFont ("Label.font")); // NOI18N

        localTopMessage.setText(msg != null ? msg : "");
        add(localTopMessage, java.awt.BorderLayout.NORTH);
        
        Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage (GrantAccessPanel.class, "CTL_DNSTDNT"));
        jCheckBox1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (GrantAccessPanel.class, "ACSD_CTL_DNSTDNT"));
        add(jCheckBox1, java.awt.BorderLayout.SOUTH);
    }

    private javax.swing.JCheckBox jCheckBox1;

    // main methods ....................................................................................

    public void setShowDialog (boolean show) {
        jCheckBox1.setSelected (!show);
    }

    public boolean getShowDialog () {
        return !jCheckBox1.isSelected ();
    }
}
