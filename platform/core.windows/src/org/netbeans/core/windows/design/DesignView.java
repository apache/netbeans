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
