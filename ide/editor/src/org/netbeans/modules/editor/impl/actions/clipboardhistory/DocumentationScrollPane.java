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


package org.netbeans.modules.editor.impl.actions.clipboardhistory;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 *
 *  @author  Martin Roskanin, Dusan Balek
 */
public final class DocumentationScrollPane extends JScrollPane {

    private JEditorPane view;
    private Dimension documentationPreferredSize;

    /** Creates a new instance of ScrollJavaDocPane */
    public DocumentationScrollPane(JTextComponent editorComponent) {
        super();
 
        // Determine and use fixed preferred size
        documentationPreferredSize = new Dimension(500, 300);
        setPreferredSize(null); // Use the documentationPopupPreferredSize
        
        Color bgColor = new JEditorPane().getBackground();
        bgColor = new Color(
                Math.max(bgColor.getRed() - 8, 0 ), 
                Math.max(bgColor.getGreen() - 8, 0 ), 
                bgColor.getBlue());
        
        // Add the completion doc view
        view = new JEditorPane();
        view.setEditable(false);
        view.setFocusable(true);
        view.setBackground(bgColor);
        view.setMargin(new Insets(0, 3, 3, 3));
        setViewportView(view);
        setFocusable(true);
    }
    
    public @Override void setPreferredSize(Dimension preferredSize) {
        if (preferredSize == null) {
            preferredSize = documentationPreferredSize;
        }
        super.setPreferredSize(preferredSize);
    }
    
    
    public void setData(String doc) {
        view.setText(doc);
        view.setCaretPosition(0);
    }
}
