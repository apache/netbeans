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
package org.netbeans.modules.editor.search;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class SearchButton {

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);

    public static JButton createButton(final String imageIcon, final String resName) {
        JButton button = new JButton(
                ImageUtilities.loadImageIcon(imageIcon, false));
        if (resName != null) {
            Mnemonics.setLocalizedText(button, NbBundle.getMessage(SearchButton.class, resName));
        }
        processButton(button);

        return button;
    }

    public static JToggleButton createToggleButton(final String imageIcon) {
        JToggleButton button = new JToggleButton(ImageUtilities.loadImageIcon(imageIcon, false)) {

            @Override
            public void setSelected(boolean b) {
                setContentAreaFilled(b);
                setBorderPainted(b);
                super.setSelected(b);
            }       
        };
        
        processButton(button);

        return button;
    }
    
    private static void processButton(AbstractButton button) {
        removeButtonContentAreaAndBorder(button);
        button.setMargin(BUTTON_INSETS);
        button.addMouseListener(sharedMouseListener);
        button.setFocusable(false);
    }

    private static void removeButtonContentAreaAndBorder(AbstractButton button) {
        boolean canRemove = true;
        if (button instanceof JToggleButton) {
            canRemove = !button.isSelected();
        }
        if (canRemove) {
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
        }
    }
    private static final MouseListener sharedMouseListener = new org.openide.awt.MouseUtils.PopupMouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent evt) {
            Object src = evt.getSource();

            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                if (button.isEnabled()) {
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            Object src = evt.getSource();
            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                removeButtonContentAreaAndBorder(button);
            }
        }

        @Override
        protected void showPopup(MouseEvent evt) {
        }
    };
}
