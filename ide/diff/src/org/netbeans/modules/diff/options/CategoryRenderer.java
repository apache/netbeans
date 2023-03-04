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
package org.netbeans.modules.diff.options;

import org.netbeans.api.editor.settings.EditorStyleConstants;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.awt.Component;

/**
 * 
 * copied from editor/options.
 * @author Maros Sandor
 */
class CategoryRenderer extends DefaultListCellRenderer {
    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        setComponentOrientation(list.getComponentOrientation());
        if (isSelected) {
            setBackground(list.getSelectionBackground ());
            setForeground(list.getSelectionForeground ());
        } else {
            setBackground(list.getBackground ());
            setForeground(list.getForeground ());
        }
        setIcon((Icon) ((AttributeSet) value).getAttribute ("icon"));
        setText((String) ((AttributeSet) value).getAttribute (EditorStyleConstants.DisplayName));
    
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setBorder(cellHasFocus ? UIManager.getBorder ("List.focusCellHighlightBorder") : noFocusBorder);
        return this;
    }
}
