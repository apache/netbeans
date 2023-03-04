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
package org.netbeans.modules.profiler.v2.ui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.netbeans.lib.profiler.ui.swing.PopupButton;

/**
 *
 * @author Jiri Sedlacek
 */
public class SettingsPanel extends JPanel {
    
    public SettingsPanel() {
        super(null);
        
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(false);
        
        add(Box.createVerticalStrut(defaultHeight()));
    }
    
    
    public void removeAll() {
        super.removeAll();
        add(Box.createVerticalStrut(defaultHeight()));
    }
    
    
    private static int DEFAULT_HEIGHT = -1;
    
    private static int defaultHeight() {
        if (DEFAULT_HEIGHT == -1) {
            JPanel ref = new JPanel(null);
            ref.setLayout(new BoxLayout(ref, BoxLayout.LINE_AXIS));
            ref.setOpaque(false);
            
            ref.add(new JLabel("XXX")); // NOI18N
            
            ref.add(new JButton("XXX")); // NOI18N
            ref.add(new PopupButton("XXX")); // NOI18N
            
            ref.add(new JCheckBox("XXX")); // NOI18N
            ref.add(new JRadioButton("XXX")); // NOI18N
            
            ref.add(new JTextField("XXX")); // NOI18N
            
            ref.add(new JExtendedSpinner(new SpinnerNumberModel(1, 1, 655535, 1)));
            
            Component separator = Box.createHorizontalStrut(1);
            Dimension d = separator.getMaximumSize(); d.height = 20;
            separator.setMaximumSize(d);
            ref.add(separator);
            
            DEFAULT_HEIGHT = ref.getPreferredSize().height;
        }
        return DEFAULT_HEIGHT;
    }
    
}
