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
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.Text;

public class NbiTextsDialog extends NbiDialog {
    private NbiTabbedPane textsTabbedPane;
    
    private String            title;
    private Map<String, Text> texts;
    
    public NbiTextsDialog(String title, Map<String, Text> texts) {
        super();
        
        this.title = title;
        this.texts = texts;
        
        initComponents();
        initialize();
    }
    
    public NbiTextsDialog(NbiFrame owner, String title, Map<String, Text> texts) {
        super(owner);
        
        this.title = title;
        this.texts = texts;
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        setTitle(title);
        
        textsTabbedPane.removeAll();

        texts.forEach((tabTitle, component) -> textsTabbedPane.addTab(tabTitle, createTab(component)));
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        textsTabbedPane = new NbiTabbedPane();
        
        add(textsTabbedPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 11, 11), 0, 0));
    }
    
    private JComponent createTab(Text text) {
        NbiTextPane   textPane;
        NbiPanel      textPanel;
        NbiScrollPane textScrollPane;
        
        textPane = new NbiTextPane();
        textPane.setText(text);
        
        textPanel = new NbiPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(textPane, BorderLayout.CENTER);
        
        textScrollPane = new NbiScrollPane(textPanel);
        textScrollPane.setViewportBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        return textScrollPane;
    }
}
