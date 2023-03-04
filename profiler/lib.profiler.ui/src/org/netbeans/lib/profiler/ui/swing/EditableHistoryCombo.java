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
package org.netbeans.lib.profiler.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
class EditableHistoryCombo extends JComboBox {
    
    private Runnable onTextChange;
    
    EditableHistoryCombo() {
        super(new ComboHistoryModel(10));
        
        setEditable(true);
//        putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
        
        setPrototypeDisplayValue("org.netbeans.lib.profiler.ui.swing.XXXXX"); // NOI18N
        Dimension dim = getPreferredSize();
        dim.height = !UIUtils.isNimbusLookAndFeel() ? getMinimumSize().height :
                     new JTextField("X").getPreferredSize().height; // NOI18N
        
        setMinimumSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);
        
        JTextComponent comp = getTextComponent();
        if (comp != null) comp.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { onChange(); }
            public void removeUpdate(DocumentEvent e)  { onChange(); }
            public void changedUpdate(DocumentEvent e) { onChange(); }
            private void onChange() { if (onTextChange != null) onTextChange.run(); }
        });
    }
    
    
    JTextComponent getTextComponent() {
        Component comp = getEditor().getEditorComponent();
        return comp instanceof JTextComponent ? (JTextComponent)comp : null;
    }
    
    String getText() {
        JTextComponent textC = getTextComponent();
        return textC != null ? textC.getText() : getSelectedItem().toString();
    }
    
    void setOnTextChangeHandler(Runnable handler) {
        onTextChange = handler;
    }
    
    
    private static class ComboHistoryModel extends DefaultComboBoxModel {
        
        private final int historySize;
        
        
        ComboHistoryModel(int historySize) {
            this.historySize = historySize;
        }
        
        
        public void addElement(Object item) {
            insertElementAt(item, 0);
        }
        
        public void insertElementAt(Object item, int index) {
            int current = getIndexOf(item);
            if (current == index) return;
            
            if (current != -1) removeElementAt(current);
            super.insertElementAt(item, index);
            
            if (getSize() > historySize) removeElementAt(historySize);
            
            setSelectedItem(item);
        }
        
    }
    
}
