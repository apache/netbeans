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

package org.netbeans.core.ui.sysopen;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Open the selected file(s) with the system default tool.
 * @author Jesse Glick
 */
@ActionID(id = "org.netbeans.core.ui.sysopen.SystemOpenAction", category = "Edit")
@ActionRegistration(displayName = "#CTL_SystemOpenAction", lazy=false)
@ActionReference(path = "Loaders/content/unknown/Actions", position = 200)
public final class SystemOpenAction extends AbstractAction implements ContextAwareAction {
    
    public SystemOpenAction() {
        super(NbBundle.getMessage(SystemOpenAction.class, "CTL_SystemOpenAction"));
    }

    public @Override void actionPerformed(ActionEvent e) {
        new ContextAction(Utilities.actionsGlobalContext()).actionPerformed(e);
    }

    public @Override Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    
    private static final class ContextAction extends AbstractAction {
        
        private final Set<File> files;
        
        public ContextAction(Lookup context) {
            super(NbBundle.getMessage(SystemOpenAction.class, "CTL_SystemOpenAction"));
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            files = new HashSet<File>();
            for (DataObject d : context.lookupAll(DataObject.class)) {
                File f = FileUtil.toFile(d.getPrimaryFile());
                if (f == null || /* #144575 */Utilities.isWindows() && f.isFile() && !f.getName().contains(".")) {
                    files.clear();
                    break;
                }
                files.add(f);
            }
        }

        public @Override boolean isEnabled() {
            return !files.isEmpty() && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
        }

        public @Override void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() { // #176879: asynch
                public @Override void run() {
                    Desktop desktop = Desktop.getDesktop();
                    for (File f : files) {
                        try {
                            desktop.open(f);
                        } catch (IOException x) {
                            Logger.getLogger(SystemOpenAction.class.getName()).log(Level.INFO, null, x);
                            // XXX or perhaps notify user of problem; but not very useful on Unix at least (#6940853)
                        }
                    }
                }
            });
        }

    }
    
}

