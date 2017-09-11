/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
////        StatusBar sb = getEditorUI(c).getStatusBar();
////        if (sb != null) {
////            sb.setText(StatusBar.CELL_MAIN, text, extraColoring);
////        }
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
