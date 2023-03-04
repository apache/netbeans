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

package org.netbeans.modules.xml.lib;

import java.awt.event.FocusListener;

/**
 * Holds common A11Y hacks.
 *
 * @author  Petr Kuzel
 */
public final class A11YUtil {

    /** Creates a new instance of A11YUtil */
    private A11YUtil() {
    }

    private static FocusListener flis;

    /**
     * Get universal Focus listener suitable for decsription JTextFields only.
     * It provides screen reades support for read only enabled descriptions.
     */
    public static synchronized FocusListener getA11YJTextFieldSupport() {

        if (flis == null) {
            flis = new java.awt.event.FocusListener() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (e.getComponent() instanceof javax.swing.JTextField) {
                        ((javax.swing.JTextField)e.getComponent()).selectAll();
                    }
                }
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (e.getComponent() instanceof javax.swing.JTextField) {
                        ((javax.swing.JTextField)e.getComponent()).select(1,1);
                    }
                }
            };
        }
        return flis;
    }
    
}
