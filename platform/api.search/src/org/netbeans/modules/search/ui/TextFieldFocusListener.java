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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.text.JTextComponent;

/**
 * Listener that selects all text in a text field when the text field gains
 * permanent focus.
 */
public class TextFieldFocusListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {
        if (!e.isTemporary()) {
            JTextComponent textComp = (JTextComponent) e.getSource();
            if (textComp.getText().length() != 0) {
                textComp.selectAll();
            }
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        /*
         * do nothing
         */
    }
}
