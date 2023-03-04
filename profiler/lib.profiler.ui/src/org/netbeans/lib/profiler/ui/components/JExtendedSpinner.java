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

package org.netbeans.lib.profiler.ui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.ui.UIUtils;


/**
 * JSpinner with some bugfixes
 *
 * - setModel doesn't break font
 * - setModel doesn't break accessible name and description
 * - can propagate accessible name and description to its editor (JFormattedTextField)
 * - doesn't consume ESC key
 *
 * @author Jiri Sedlacek
 */
public class JExtendedSpinner extends JSpinner {
    
    private static int defaultSpinnerHeight = -1;
    
    public static int getDefaultSpinnerHeight() {
        if (defaultSpinnerHeight == -1) {
            defaultSpinnerHeight = new JTextField().getPreferredSize().height;
        }

        return defaultSpinnerHeight;
    }
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public JExtendedSpinner() {
        super();
        ((JSpinner.DefaultEditor) getEditor()).getTextField().setFont(UIManager.getFont("Label.font")); // NOI18N
        ((JSpinner.DefaultEditor) getEditor()).getTextField().addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(final java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                        processKeyEvent(e);
                    }
                }
            });
        configureWheelListener();
    }

    public JExtendedSpinner(SpinnerModel model) {
        super(model);
        ((JSpinner.DefaultEditor) getEditor()).getTextField().setFont(UIManager.getFont("Label.font")); // NOI18N
        ((JSpinner.DefaultEditor) getEditor()).getTextField().addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(final java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                        processKeyEvent(e);
                    }
                }
            });
        configureWheelListener();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setModel(SpinnerModel model) {
        Font font = ((JSpinner.DefaultEditor) getEditor()).getTextField().getFont();
        String accessibleName = ((JSpinner.DefaultEditor) getEditor()).getTextField().getAccessibleContext().getAccessibleName();
        String accessibleDescription = ((JSpinner.DefaultEditor) getEditor()).getTextField().getAccessibleContext()
                                        .getAccessibleDescription();
        super.setModel(model);
        ((JSpinner.DefaultEditor) getEditor()).getTextField().setFont(font);
        ((JSpinner.DefaultEditor) getEditor()).getTextField().getAccessibleContext().setAccessibleName(accessibleName);
        ((JSpinner.DefaultEditor) getEditor()).getTextField().getAccessibleContext()
         .setAccessibleDescription(accessibleDescription);
    }

    public void fixAccessibility() {
        if (getAccessibleContext() != null) {
            ((JSpinner.DefaultEditor) getEditor()).getTextField().getAccessibleContext()
             .setAccessibleName(getAccessibleContext().getAccessibleName());
            ((JSpinner.DefaultEditor) getEditor()).getTextField().getAccessibleContext()
             .setAccessibleDescription(getAccessibleContext().getAccessibleDescription());
        }
    }
    
    
    public Dimension getPreferredSize() {
        if (UIUtils.isWindowsClassicLookAndFeel()) {
            return new Dimension(super.getPreferredSize().width, getDefaultSpinnerHeight());
        } else {
            return super.getPreferredSize();
        }
    }

    public Dimension getMinimumSize() {
        if (UIUtils.isWindowsClassicLookAndFeel()) {
            return getPreferredSize();
        } else {
            return super.getMinimumSize();
        }
    }
    
    
    private void configureWheelListener() {
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) return;
                Object newValue = (e.getWheelRotation() < 0 ?
                                   JExtendedSpinner.this.getNextValue() :
                                   JExtendedSpinner.this.getPreviousValue());
                if (newValue != null) JExtendedSpinner.this.setValue(newValue);
            }
        });
    }
}
