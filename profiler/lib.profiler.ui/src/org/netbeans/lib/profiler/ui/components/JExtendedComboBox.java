/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.ui.components;

import java.awt.Component;
import java.awt.event.ItemEvent;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;


/**
 * JComboBox which supports JSeparator inside its popup menu.
 *
 * @author Jiri Sedlacek
 */
public class JExtendedComboBox extends JComboBox {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class ExtendedComboListRenderer extends DefaultListCellRenderer {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (value instanceof JSeparator) {
                return (JSeparator) value;
            } else {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private DefaultComboBoxModel model = new DefaultComboBoxModel();
    private boolean closingWithSeparator = false;
    private int lastSelectedIndex = 0;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public JExtendedComboBox() {
        setModel(model);
        setRenderer(new ExtendedComboListRenderer());
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setModel(ComboBoxModel aModel) {
        if (!(aModel instanceof DefaultComboBoxModel)) {
            throw new RuntimeException("Only DefaultComboBoxModel is supported for this component"); //NOI18N
        }

        model = (DefaultComboBoxModel) aModel;
        super.setModel(model);
    }

    public void firePopupMenuWillBecomeInvisible() {
        if (getSelectedItem() instanceof JSeparator) {
            closingWithSeparator = true;
        }

        super.firePopupMenuWillBecomeInvisible();
    }

    protected void fireItemStateChanged(ItemEvent e) {
        switch (e.getStateChange()) {
            case ItemEvent.SELECTED:

                if (e.getItem() instanceof JSeparator) {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                selectNextItem();
                            }
                        });

                }

                break;
            case ItemEvent.DESELECTED:

                if (!(e.getItem() instanceof JSeparator)) {
                    lastSelectedIndex = model.getIndexOf(e.getItem());
                }

                break;
        }

        super.fireItemStateChanged(e);
    }

    private void selectNextItem() {
        int currentSelectedIndex = getSelectedIndex();

        if (closingWithSeparator) {
            setSelectedIndex(lastSelectedIndex);
            closingWithSeparator = false;
        } else if (currentSelectedIndex > lastSelectedIndex) {
            setSelectedIndex(currentSelectedIndex + 1);
        } else {
            setSelectedIndex(currentSelectedIndex - 1);
        }
    }
}
