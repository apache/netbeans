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
package org.netbeans.modules.search.ui;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Cell renderer that checks size of displayed items, and shortens them if they
 * are too long.
 *
 * @author jhavlin
 */
public class ShorteningCellRenderer extends DefaultListCellRenderer {

    private static final int COMBO_TEXT_LENGHT_LIMIT = 100;
    private static final String THREE_DOTS = "...";                     //NOI18N

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        String s = value == null ? null : value.toString().replaceAll("[\r\n]+", " ");
        Component component = super.getListCellRendererComponent(list,
                s, index, isSelected, cellHasFocus);
        if (s != null && s.length() > COMBO_TEXT_LENGHT_LIMIT
                && component instanceof JLabel) {
            ((JLabel) component).setText(s.substring(0,
                    COMBO_TEXT_LENGHT_LIMIT - THREE_DOTS.length())
                    + THREE_DOTS);
        }
        return component;
    }
}
