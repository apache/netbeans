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
package org.netbeans.modules.print.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.03.09
 */
public enum Macro {

    NAME, // the name of area
    USER, // the user name
    ROW, // row number
    COLUMN, // column number
    COUNT, // total count
    MODIFIED_DATE, // date of the last modification
    MODIFIED_TIME, // time of the last modification
    PRINTED_DATE, // date of printing
    PRINTED_TIME; // time of printing

    public interface Listener {
        void pressed(Macro macro);
    }

    private Macro() {
        myName = "%" + name() + "%"; // NOI18N
        myButton = new JButton();
        myButton.setFocusable(false);
        myButton.setToolTipText(getToolTipText());
        myButton.setMnemonic(KeyEvent.VK_1 + ordinal());
        myButton.setIcon(icon(getClass(), name().toLowerCase()));
    }

    public String getName() {
        return myName;
    }

    public JButton getButton() {
        return myButton;
    }

    private String getToolTipText() {
        String alt = " (Alt-" + (ordinal() + 1) + ")"; // NOI18N
        return i18n(Macro.class, name()) + alt;
    }

    public JButton getButton(final Listener listener) {
        myButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                listener.pressed(Macro.this);
            }
        });
        return getButton();
    }

    private String myName;
    private JButton myButton;
}
