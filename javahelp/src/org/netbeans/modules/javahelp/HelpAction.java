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

package org.netbeans.modules.javahelp;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import org.netbeans.api.javahelp.Help;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Shows help for the currently focused component
 * @author Jesse Glick
 */
@ActionID(category="Help", id="org.netbeans.modules.javahelp.HelpAction")
@ActionRegistration(displayName="#LBL_HelpAction", iconBase="org/netbeans/modules/javahelp/resources/show-help.gif",
        /* OpenIDE-Transmodal-Action must be present from the start (#206089) */ lazy=false)
@ActionReference(path="Shortcuts", name="F1")
public class HelpAction extends AbstractAction {

    public HelpAction() {
        Installer.log.fine("HelpAction.initialize");
        // Cf. org.netbeans.core.windows.frames.ShortcutAndMenuKeyEventProcessor
        putValue("OpenIDE-Transmodal-Action", true); // NOI18N
    }

    static class WindowActivatedDetector implements AWTEventListener {
        private static java.lang.ref.WeakReference<Window> currentWindowRef;
        private static WindowActivatedDetector detector = null;

        static synchronized void install() {
            if (detector == null && !GraphicsEnvironment.isHeadless()) {
                detector = new WindowActivatedDetector();
                Toolkit.getDefaultToolkit ().addAWTEventListener(detector, AWTEvent.WINDOW_EVENT_MASK);
            }
        }
        
        static synchronized void uninstall() {
            if (detector != null) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(detector);
                detector = null;
            }
        }
        
        static synchronized Window getCurrentActivatedWindow() {
            if (currentWindowRef != null) {
                return currentWindowRef.get();
            }
            else {
                return null;
            }
        }

        private static synchronized void setCurrentActivatedWindow(Window w) {
            currentWindowRef = new java.lang.ref.WeakReference<Window>(w);
        }

        public void eventDispatched (AWTEvent ev) {
            if (ev.getID() != WindowEvent.WINDOW_ACTIVATED)
                return;
            setCurrentActivatedWindow(((WindowEvent) ev).getWindow());
        }
    }
    
    private static HelpCtx findHelpCtx() {

        final TopComponent activeTC = TopComponent.getRegistry().getActivated();
        final Window win = WindowActivatedDetector.getCurrentActivatedWindow();
        final Container cont;
        if (activeTC != null && win != null && win.isAncestorOf(activeTC)) {
            cont = activeTC;
        } else {
            cont = win;
        }
        if (cont == null) {
            return HelpCtx.DEFAULT_HELP;
        } else {
            Component focused = SwingUtilities.findFocusOwner(cont);
            HelpCtx help = HelpCtx.findHelp(focused == null ? cont : focused);
            Installer.log.log(Level.FINE, "HelpCtx {0} from {1}",
                    new Object[]{help, focused});
            return help;
        }
    }
    
    @Override public void actionPerformed(ActionEvent ev) {
        Help h = (Help)Lookup.getDefault().lookup(Help.class);
        if (h == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        HelpCtx help;
        
        final MenuElement[] path =
            MenuSelectionManager.defaultManager().getSelectedPath();

        if (path != null
            && path.length > 0
            && !(path[0].getComponent() instanceof javax.swing.plaf.basic.ComboPopup)
            ) {
            help = HelpCtx.findHelp(path[path.length - 1].getComponent());
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MenuElement[] newPath =
                        MenuSelectionManager.defaultManager().getSelectedPath();

                    if (newPath.length != path.length)
                        return;
                    for (int i = 0; i < newPath.length; i++) {
                        if (newPath[i] != path[i])
                            return;
                    }
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                }
            });
        }
        else {
            help = findHelpCtx();
        }
        
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(HelpAction.class, "CTL_OpeningHelp"));
        h.showHelp (help);
    }
}
