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

package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JList;

/**
 *
 * @author Andrei Badea
 */
public class CheckListener extends MouseAdapter implements KeyListener {

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            return;
        }
        if (!(e.getSource() instanceof JList)) {
            return;
        }
        JList list = (JList) e.getSource();
        int index = list.locationToIndex(e.getPoint());
        if (index < 0) {
            return;
        }
        toggle(list.getModel().getElementAt(index));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_SPACE) {
            return;
        }
        if (!(e.getSource() instanceof JList)) {
            return;
        }
        JList list = (JList) e.getSource();
        Object selected = list.getSelectedValue();
        if (selected == null) {
            return;
        }
        toggle(selected);
        e.consume();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void toggle(Object obj) {
        if (obj instanceof Selectable) {
            Selectable selectable = (Selectable) obj;
            selectable.setSelected(!selectable.isSelected());
        }
    }
}
