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
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Open the selected file(s) with the system default tool.
 * @author Jesse Glick
 */
public abstract sealed class AbstractSystemOpenAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor PROC = new RequestProcessor(AbstractSystemOpenAction.class);

    private final boolean openParent;
    protected final Set<File> files;

    protected AbstractSystemOpenAction(String name, Lookup context, boolean parent) {
        super(name);
        files = new HashSet<>();
        openParent = parent;
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        Lookup.Result<DataObject> result = context.lookupResult(DataObject.class);
        result.addLookupListener(WeakListeners.create(LookupListener.class, e -> updateFileSet(result), result));
        updateFileSet(result);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PROC.post(() -> { // #176879: asynch
            Desktop desktop = Desktop.getDesktop();
            for (File f : files) {
                try {
                    desktop.open(openParent ? f.getParentFile() : f);
                } catch (IOException x) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, null, x);
                    // XXX or perhaps notify user of problem; but not very useful on Unix at least (#6940853)
                }
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
    }

    private void updateFileSet(Lookup.Result<DataObject> result) {
        files.clear();
        for (DataObject d : result.allInstances()) {
            File f = FileUtil.toFile(d.getPrimaryFile());
            if (f != null && !isIgnoredFile(f)) {
                files.add(f);
            }
        }
    }

    private boolean isIgnoredFile(File f) {
        if (Utilities.isWindows() && f.isFile() && !f.getName().contains(".")) {
            /* #144575 */
            return true;
        }
        return f.getName().endsWith(".shadow");
    }

    @ActionID(id = "org.netbeans.core.ui.sysopen.SystemOpenAction", category = "Edit")
    @ActionRegistration(displayName = "#CTL_SystemOpenAction", lazy = false)
    @ActionReferences ({
        @ActionReference(path = "Editors/TabActions", position = 401),
        @ActionReference(path = "UI/ToolActions/Files", position = 2045),
        @ActionReference(path = "Projects/Actions", position = 101),
        @ActionReference(path = "Shortcuts", name = "SO-S")
    })
    public final static class SystemOpenAction extends AbstractSystemOpenAction {
        
        public SystemOpenAction() {
            this(Utilities.actionsGlobalContext());
        }
        
        public SystemOpenAction(Lookup context) {
            super(NbBundle.getMessage(AbstractSystemOpenAction.class, "CTL_SystemOpenAction"), context, false);
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new SystemOpenAction(context);
        }

        @Override
        public boolean isEnabled() {
            return !files.isEmpty() && super.isEnabled();
        }
    }
    
    @ActionID(id = "org.netbeans.core.ui.sysopen.SystemOpenParentAction", category = "Edit")
    @ActionRegistration(displayName = "#CTL_SystemOpenParentAction", lazy = false)
    @ActionReferences ({
        @ActionReference(path = "Editors/TabActions", position = 402),
        @ActionReference(path = "UI/ToolActions/Files", position = 2046),
        @ActionReference(path = "Projects/Actions", position = 102)
    })
    public final static class SystemOpenParentAction extends AbstractSystemOpenAction {
        
        public SystemOpenParentAction() {
            this(Utilities.actionsGlobalContext());
        }
        
        public SystemOpenParentAction(Lookup context) {
            super(NbBundle.getMessage(AbstractSystemOpenAction.class, "CTL_SystemOpenParentAction"), context, true);
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new SystemOpenParentAction(context);
        }

        @Override
        public boolean isEnabled() {
            return files.size() == 1 && files.iterator().next().getParent() != null && super.isEnabled();
        }
    }

}
