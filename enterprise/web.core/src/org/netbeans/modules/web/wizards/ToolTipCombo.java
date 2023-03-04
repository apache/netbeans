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
package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class ToolTipCombo extends JComboBox {

    private static final long serialVersionUID = 1189442122448524856L;

    ToolTipCombo(Object[] o) {
        super(o);
        this.setRenderer(new PathRenderer());
        addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    setToolTipText(evt.getItem().toString());
                }
            }
        });
        if (o != null && o.length > 1) {
            setToolTipText(o[0].toString());
        }
    }

    static class PathRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = 1323260132420573174L;

        public PathRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            setToolTipText(value.toString());
            return this;
        }
    }
} 

