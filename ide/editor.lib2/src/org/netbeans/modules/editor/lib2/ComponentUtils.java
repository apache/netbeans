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

package org.netbeans.modules.editor.lib2;

import java.awt.Component;
import java.awt.Frame;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;

/**
 * 
 * @author Vita Stejskal
 */
public final class ComponentUtils {

    private static final Logger LOG = Logger.getLogger(Logger.class.getName());
    private static final String STATUS_BAR_TEXT_PROPERTY = "statusBarText";
    
    public static boolean isGuardedException(BadLocationException exc) {
        return exc.getClass().getName().equals("org.netbeans.editor.GuardedException");
    }

    public static void returnFocus() {
         JTextComponent c = EditorRegistry.lastFocusedComponent();
         if (c != null) {
             requestFocus(c);
         }
    }

    public static void requestFocus(JTextComponent c) {
        if (c != null) {
            if (!EditorImplementation.getDefault().activateComponent(c)) {
                Frame f = getParentFrame(c);
                if (f != null) {
                    f.requestFocus();
                }
                c.requestFocus();
            }
        }
    }

    public static void setStatusText(JTextComponent c, String text) {
        // TODO: fix this, do not use reflection
        try {
            Object editorUI = getEditorUI(c);
            if (editorUI == null) {
                c.putClientProperty(STATUS_BAR_TEXT_PROPERTY, text);
                return;
            }
            Method getSbMethod = editorUI.getClass().getMethod("getStatusBar");
            Object statusBar = getSbMethod.invoke(editorUI);
            Method setTextMethod = statusBar.getClass().getMethod("setText", String.class, String.class);
            setTextMethod.invoke(statusBar, "main", text);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
//        StatusBar sb = getEditorUI(c).getStatusBar();
//        if (sb != null) {
//            sb.setText(StatusBar.CELL_MAIN, text);
//        }
    }

    public static void setStatusText(JTextComponent c, String text, int importance) {
        // TODO: fix this, do not use reflection
        try {
            Object editorUI = getEditorUI(c);
            if (editorUI == null) {
                c.putClientProperty(STATUS_BAR_TEXT_PROPERTY, text);
                return;
            }
            Method getSbMethod = editorUI.getClass().getMethod("getStatusBar");
            Object statusBar = getSbMethod.invoke(editorUI);
            Method setTextMethod = statusBar.getClass().getMethod("setText", String.class, int.class);
            setTextMethod.invoke(statusBar, text, importance);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
//        StatusBar sb = getEditorUI(c).getStatusBar();
//        if (sb != null) {
//            sb.setText(StatusBar.CELL_MAIN, text);
//        }
    }

//    public static void setStatusText(JTextComponent c, String text, Coloring extraColoring) {
//        TextUI textUI = c.getUI();
//        try {
//            Method getSbMethod = textUI.getClass().getMethod("getStatusBar");
//            Object statusBar = getSbMethod.invoke(textUI);
//            Method setTextMethod = statusBar.getClass().getMethod("setText", String.class, String.class);
//            setTextMethod.invoke(statusBar, "main", text);
//        } catch (Exception e) {
//            LOG.log(Level.WARNING, e.getMessage(), e);
//        }
// //        StatusBar sb = getEditorUI(c).getStatusBar();
// //        if (sb != null) {
// //            sb.setText(StatusBar.CELL_MAIN, text, extraColoring);
// //        }
//    }

    public static void setStatusBoldText(JTextComponent c, String text) {
        // TODO: fix this, do not use reflection
        try {
            Object editorUI = getEditorUI(c);
            if (editorUI == null) {
                c.putClientProperty(STATUS_BAR_TEXT_PROPERTY, text);
                return;
            }
            Method getSbMethod = editorUI.getClass().getMethod("getStatusBar"); //NOI18N
            Object statusBar = getSbMethod.invoke(editorUI);
            Method setTextMethod = statusBar.getClass().getMethod("setBoldText", String.class, String.class); //NOI18N
            setTextMethod.invoke(statusBar, "main", text); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
//        StatusBar sb = getEditorUI(c).getStatusBar();
//        if (sb != null) {
//            sb.setBoldText(StatusBar.CELL_MAIN, text);
//        }
    }

    public static String getStatusText(JTextComponent c) {
        // TODO: fix this, do not use reflection
        try {
            Object editorUI = getEditorUI(c);
            if (editorUI == null) {
                return "";
            }
            Method getSbMethod = editorUI.getClass().getMethod("getStatusBar"); //NOI18N
            Object statusBar = getSbMethod.invoke(editorUI);
            Method getTextMethod = statusBar.getClass().getMethod("getText", String.class); //NOI18N
            return (String) getTextMethod.invoke(statusBar, "main"); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return ""; //NOI18N
        }
//        StatusBar sb = getEditorUI(c).getStatusBar();
//        return (sb != null) ? sb.getText(StatusBar.CELL_MAIN) : null;
    }

    public static void clearStatusText(JTextComponent c) {
        setStatusText(c, ""); // NOI18N
    }
    
    
    private static Object getEditorUI(JTextComponent c) throws Exception {
        // TODO: fix this, do not use reflection
        TextUI textUI = c.getUI();
        Method getEuiMethod = null;
        try {
            getEuiMethod = textUI.getClass().getMethod("getEditorUI"); //NOI18N
        } catch (NoSuchMethodException nsme) {
            LOG.log(Level.INFO, nsme.getMessage(), nsme);
        }
        if (getEuiMethod != null) {
            return getEuiMethod.invoke(textUI);
        } else {
            return null;
        }
    }
    
    private static Frame getParentFrame(Component c) {
        do {
            c = c.getParent();
            if (c instanceof Frame) {
                return (Frame)c;
            }
        } while (c != null);
        return null;
    }
    
    /** Creates a new instance of DocUtils */
    private ComponentUtils() {
    }
}
