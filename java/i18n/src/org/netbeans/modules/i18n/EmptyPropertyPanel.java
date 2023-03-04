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

package org.netbeans.modules.i18n;

import java.util.MissingResourceException;
import org.openide.util.NbBundle;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JLabel;

/**
 * @author  or141057
 */
public class EmptyPropertyPanel extends javax.swing.JPanel {

    /** Creates new form EmptyPropertyPanel */
    public EmptyPropertyPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        theLabel = new javax.swing.JLabel();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(100));

        theLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        theLabel.setText("....");
        theLabel.setAlignmentX(0.5f);
        theLabel.setAlignmentY(0.5f);
        add(theLabel);
    }
    
    
    private JLabel theLabel;
    
    public void setBundleText(String textID) throws MissingResourceException {
        theLabel.setText(NbBundle.getMessage(EmptyPropertyPanel.class, textID));
    }
    
    public String getText() {
        return theLabel.getText();
    }
}
