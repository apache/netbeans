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

package org.netbeans.installer.utils.helper.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.Text;

public class NbiTextDialog extends NbiDialog {
    private NbiTextPane   textPane;
    private NbiPanel      textPanel;
    private NbiScrollPane textScrollPane;
    
    private String title;
    private Text   text;
    
    public NbiTextDialog(String title, Text text) {
        super();
        
        this.title = title;
        this.text = text;
        
        initComponents();
        initialize();
    }
    
    public NbiTextDialog(NbiFrame owner, String title, Text text) {
        super(owner);
        
        this.title = title;
        this.text = text;
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        setTitle(title);
        
        textPane.setText(text);
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        textPane = new NbiTextPane();
        
        textPanel = new NbiPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(textPane, BorderLayout.CENTER);
        
        textScrollPane = new NbiScrollPane(textPanel);
        textScrollPane.setViewportBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        add(textScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 11, 11), 0, 0));
    }
}
