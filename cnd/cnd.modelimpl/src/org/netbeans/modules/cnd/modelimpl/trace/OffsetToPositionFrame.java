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
package org.netbeans.modules.cnd.modelimpl.trace;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 *
 */
public class OffsetToPositionFrame extends JFrame {
    
    private final FileImpl file;
    
    private final JTextField inputField = new JTextField(10);
    
    private final JLabel outputField = new JLabel();

    public OffsetToPositionFrame(FileImpl file) {
        this.file = file;
        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitter.setDividerSize(2);
        splitter.setResizeWeight(.4d);        
        splitter.setTopComponent(inputField);
        splitter.setBottomComponent(outputField);
        content.add(splitter, BorderLayout.CENTER);
        
        inputField.setMaximumSize(inputField.getPreferredSize());
        inputField.setHorizontalAlignment(SwingConstants.CENTER);
        outputField.setHorizontalAlignment(SwingConstants.CENTER);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Frame f = (Frame)e.getSource();
                f.setVisible(false);
                f.dispose();
            }
        });
        
        setListeners();
        
        setSize(200, 100);
    }
    
    private void setListeners() {
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
              convertOffset();
            }
            public void removeUpdate(DocumentEvent e) {
              convertOffset();
            }
            public void insertUpdate(DocumentEvent e) {
              convertOffset();
            }
        });
    }
    
    private void convertOffset() {
        String text = inputField.getText();
        if (text != null && !text.isEmpty()) {
            try {
                int offset = Integer.parseInt(text);
                int lineColumn[] = file.getLineColumn(offset);
                outputField.setText("" + lineColumn[0] + ":" + lineColumn[1]); // NOI18N
            } catch (NumberFormatException ex) {
                outputField.setText("Not a number!"); // NOI18N
            }
        } else {
            outputField.setText(""); // NOI18N
        }
    }
}
