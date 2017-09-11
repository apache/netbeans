/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.windows.design;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class DesignView implements Runnable, PropertyChangeListener {
    private static final DesignView INSTANCE = new DesignView();
    static int designModeCounter;
    
    private DesignView() {
    }

    public static void initialize() {
        INSTANCE.cleanToolbarsAndMenu();
        WindowManager.getDefault().invokeWhenUIReady(INSTANCE);
        TopComponent.getRegistry().addPropertyChangeListener(INSTANCE);
    }
    
    private void cleanToolbarsAndMenu() {
        FileObject tb = FileUtil.getConfigFile("Toolbars");

        if (tb != null) {
            for (FileObject fileObject : tb.getChildren()) {
                try {
                    fileObject.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            FileObject shadow;
            try {
                shadow = tb.createFolder("DesignView").createData("org-netbeans-core-windows-model-NewMode.shadow");
                shadow.setAttribute("originalFile", "Actions/System/org-netbeans-core-windows-model-NewMode.instance");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        FileObject mb = FileUtil.getConfigFile("Menu");

        if (mb != null) {
            for (FileObject fileObject : mb.getChildren()) {
                try {
                    fileObject.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        FileObject ws = FileUtil.getConfigFile("Windows2Local");

        if (ws != null) {
            try {
                ws.delete();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        WindowManager.getDefault().invokeWhenUIReady(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
            for (Mode m : WindowManager.getDefault().getModes()) {
                for (TopComponent topComponent : m.getTopComponents()) {
                    if (topComponent instanceof DesignViewComponent) {
                        continue;
                    }
                    topComponent.close();
                }
            }
        }
    }
    
    @Override
    public void run() {
        BIG: for (Mode m : WindowManager.getDefault().getModes()) {
            boolean found = false;
            for (TopComponent topComponent : m.getTopComponents()) {
                if (topComponent instanceof DesignViewComponent) {
                    found = true;
                    continue;
                }
                topComponent.close();
            }
            if (!found) {
                final DesignViewComponent mc = new DesignViewComponent();
                m.dockInto(mc);
                mc.open();
            }
        }
    }
    
    @ActionID(category = "System", id = "org.netbeans.core.windows.model.NewMode")
    @ActionRegistration(iconBase = "org/netbeans/core/windows/design/DesignView.png",
    displayName = "#CTL_NewMode")
    @Messages("CTL_NewMode=New Mode")
    public static ActionListener newModeAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DesignViewComponent dvc = new DesignViewComponent();
                /*
                WindowManagerImpl wmi = (WindowManagerImpl)WindowManager.getDefault();
                Mode m = wmi.createMode("mode_" + (++designModeCounter),
                    Constants.MODE_KIND_VIEW, Constants.MODE_KIND_VIEW, true,
                    new SplitConstraint[]{new SplitConstraint(Constants.HORIZONTAL, 1, 0.2)}
                );
                m.dockInto(dvc);
                 */
                dvc.open();
                dvc.requestAttention(true);
            }
        };
    }
}
