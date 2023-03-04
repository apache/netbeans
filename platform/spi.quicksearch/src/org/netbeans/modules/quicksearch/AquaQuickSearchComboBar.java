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

package org.netbeans.modules.quicksearch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

/**
 * Quick search toolbar component
 * @author  Jan Becicka
 */
public class AquaQuickSearchComboBar extends AbstractQuickSearchComboBar {

    public AquaQuickSearchComboBar(KeyStroke ks) {
        super( ks );

        setLayout(new BorderLayout());
        add( command, BorderLayout.CENTER);
    }

    @Override
    protected JTextComponent createCommandField() {
        JTextField res = new DynamicWidthTF();
        final JPopupMenu dummy = new JPopupMenu();
        dummy.addPopupMenuListener( new PopupMenuListener() {

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dummy.setVisible(false);
                    }
                });
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        res.putClientProperty("JTextField.variant", "search");
        res.putClientProperty("JTextField.Search.FindPopup", dummy);
        res.putClientProperty("JTextField.Search.FindAction", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                maybeShowPopup(null);
            }
        });

        return res;
    }

    @Override
    protected JComponent getInnerComponent() {
        return command;
    }

    private final class DynamicWidthTF extends JTextField {
        private Dimension prefWidth;

        @Override
        public Dimension getPreferredSize() {
            if (prefWidth == null) {
                Dimension orig = super.getPreferredSize();
                prefWidth = new Dimension(computePrefWidth(), orig.height);
            }
            return prefWidth;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

}
