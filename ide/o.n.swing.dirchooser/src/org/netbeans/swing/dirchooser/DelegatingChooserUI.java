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
package org.netbeans.swing.dirchooser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalFileChooserUI;
import org.openide.util.Utilities;

/** Placeholder ComponentUI that just delegates to other FileChooserUIs
 * based on what selection mode is set in JFileChooser.
 *
 * @author Dafe Simonek
 */
public class DelegatingChooserUI extends ComponentUI {
    
    static final String USE_SHELL_FOLDER = "FileChooser.useShellFolder";
    static final String NB_USE_SHELL_FOLDER = "nb.FileChooser.useShellFolder";
    static final String START_TIME = "start.time";
    
    private static boolean firstTime = true;

    public static ComponentUI createUI(JComponent c) {
        JFileChooser fc = (JFileChooser)c;

        // #109703 - don't use shell folder on JDK versions interval <1.6.0_02, 1.6.0_10>,
        // it's terribly slow on Windows due to JDK bug
        if (Utilities.isWindows()) {
            if (System.getProperty(NB_USE_SHELL_FOLDER) != null) {
                fc.putClientProperty(USE_SHELL_FOLDER, Boolean.getBoolean(NB_USE_SHELL_FOLDER));
            } else {
                String jv = System.getProperty("java.version");
                jv = jv.split("-", 2)[0];
                if ("1.6.0_02".compareToIgnoreCase(jv) <= 0 &&
                        "1.6.0_10".compareToIgnoreCase(jv) >= 0) {
                    if (!Boolean.TRUE.equals(fc.getClientProperty(USE_SHELL_FOLDER))) {
                        fc.putClientProperty(USE_SHELL_FOLDER, Boolean.FALSE);
                    }
                }
            }
        }

        // mark start time, just once during init (code can be run multiple times
        // because of property listenign below)
        if (fc.getClientProperty(START_TIME) == null) {
            fc.putClientProperty(START_TIME, Long.valueOf(System.currentTimeMillis()));
        }
        
        Class<?> chooser = getCurChooser(fc);
        ComponentUI compUI;
        try {
            Method createUIMethod = chooser.getMethod("createUI", JComponent.class);
            compUI = (ComponentUI) createUIMethod.invoke(null, fc);
        } catch (Exception exc) {
            Logger.getLogger(DelegatingChooserUI.class.getName()).log(Level.FINE,
                    "Could not instantiate custom chooser, fallbacking to Metal", exc);
            compUI = MetalFileChooserUI.createUI(c);
        }
        
        // listen to sel mode changes and select correct chooser by invoking
        // filechooser.updateUI() which triggers this createUI again 
        if (firstTime) {
            fc.addPropertyChangeListener(
                    JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY,
                    new PropertyChangeListener () {
                        public @Override void propertyChange(PropertyChangeEvent evt) {
                            JFileChooser fileChooser = (JFileChooser)evt.getSource();
                            fileChooser.updateUI();
                        }
                    }
            );
        }
        
        return compUI;
    }

    /** Returns dirchooser for DIRECTORIES_ONLY, default filechooser for other
     * selection modes.
     */
    private static Class<?> getCurChooser (JFileChooser fc) {
        if (fc.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
            return DirectoryChooserUI.class;
        }
        return Module.getOrigChooser();
    }

}
