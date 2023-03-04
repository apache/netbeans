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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.netbeans.api.project.SourceGroup;

/**
 *
 * @author Andrei Badea
 */
public class SourceGroupUISupport {
    
    private SourceGroupUISupport() {
    }
    
    public static void connect(JComboBox comboBox, SourceGroup[] sourceGroups) {
        comboBox.setModel(new DefaultComboBoxModel(sourceGroups));
        comboBox.setRenderer(new SourceGroupRenderer());
    }
    
    private static final class SourceGroupRenderer extends DefaultListCellRenderer {
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object displayName = null;
            
            if (value instanceof SourceGroup) {
                displayName = ((SourceGroup)value).getDisplayName();
            } else {
                displayName = value;
            }
            
            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
    }
}
