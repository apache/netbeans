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

package org.netbeans.modules.cnd.utils.ui;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 *
 */
public class CndUIUtilities {

    private CndUIUtilities() {
    }


    // Utility to request focus for a component by using the
    // swing utilities to invoke it at a later
    public static void requestFocus(final Component c) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (c != null) {
                    if (c.getParent() != null) {
                        try {
                            c.requestFocus();
                        } catch (NullPointerException npe) {
                            // Throw away the npe. This is probably due to
                            // the parent of this component not existing
                            // before we're through processing the
                            // requestFocus() call. This can happen when
                            // quickly clicking through a wizard.
                        }
                    }
                }
            }
        });
    }

    // Utility to request focus for a component by using the
    // swing utilities to invoke it at a later
    public static void setDefaultButton(final JRootPane rootPane, final JButton button) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (button != null) {
                    if (button.getParent() != null && button.isVisible()) {
                        try {
                            rootPane.setDefaultButton(button);
                        } catch (NullPointerException npe) {
                            // Throw away the npe. This is probably due to
                            // the parent of this component not existing
                            // before we're through processing the
                            // requestFocus() call. This can happen when
                            // quickly clicking through a wizard.
                        }
                    }
                }
            }
        });
    }

}
