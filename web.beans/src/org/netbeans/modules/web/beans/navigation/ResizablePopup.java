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
package org.netbeans.modules.web.beans.navigation;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;

import org.openide.windows.WindowManager;

/**
 * Copied from ResizablePopup at java.navigation. 
 *
 * @author ads
 */
public final class ResizablePopup {   
    static final String HELP_COOKIE = "help"; // NOI18N
    
    private static final WindowListener windowListener = new WindowAdapter() {
        
        public void windowClosing(WindowEvent windowEvent) {
            cleanup(windowEvent.getWindow());
        }

        private void cleanup(Window window) {
            window.setVisible(false);
            if (window instanceof RootPaneContainer) {
                ((RootPaneContainer) window).setContentPane(new JPanel());
            }
            window.removeWindowListener(this);
            window.dispose();
        }
        
        /*private boolean aboutToShowHelp(Window window) {
            if (window instanceof RootPaneContainer) {
                JComponent rootPane = ((RootPaneContainer) window).getRootPane();
                if (Boolean.TRUE.equals(rootPane.getClientProperty(HELP_COOKIE))) {
                    rootPane.putClientProperty(HELP_COOKIE, null);
                    return true;
                }
            }
            return false;
        }*/
    };

    public static JDialog getDialog() {
        JDialog dialog = new JDialog(WindowManager.getDefault().getMainWindow(), 
                "", false) 
        {
            private static final long serialVersionUID = -2488334519927160789L;

            public void setVisible(boolean visible) {
                boolean wasVisible = isVisible();
                if (wasVisible && !visible) {
                    WebBeansNavigationOptions.setLastBounds(getBounds());
                }
                super.setVisible(visible);
            }
        };
        //dialog.setUndecorated(true);
        dialog.setBounds(WebBeansNavigationOptions.getLastBounds());
        dialog.addWindowListener(windowListener);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        return dialog;
    }
}
