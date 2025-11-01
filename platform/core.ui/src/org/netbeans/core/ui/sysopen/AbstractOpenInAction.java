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
import java.net.MalformedURLException;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Open the selected file(s) or their parents with the system default tool or
 * as explorer tab.
 */
public abstract sealed class AbstractOpenInAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(AbstractOpenInAction.class);

    protected final Set<File> files;

    protected AbstractOpenInAction(String name, Lookup context) {
        super(name);
        files = new HashSet<>();
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        Lookup.Result<DataObject> result = context.lookupResult(DataObject.class);
        result.addLookupListener(WeakListeners.create(LookupListener.class, e -> updateFileSet(result), result));
        updateFileSet(result);
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

    protected void browse(FileObject fo) {
        if (fo != null) {
            try {
                Node node = DataObject.find(fo).getNodeDelegate();
                if (node != null) {
                    NodeOperation.getDefault().explore(node);
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    protected void openInSystem(Set<File> files) {
        RP.post(() -> {
            for (File f : files) {
                try {
                    Desktop.getDesktop().open(f);
                } catch (IOException x) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, null, x);
                }
            }
        });
    }

    protected boolean canOpenInSystem() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
    }

    @ActionID(id = "org.netbeans.core.ui.sysopen.OpenInSystemAction", category = "Edit")
    @ActionRegistration(displayName = "#CTL_OpenInSystemAction", lazy = false)
    @ActionReferences ({
        @ActionReference(path = "Editors/TabActions", position = 405),
        @ActionReference(path = "UI/ToolActions/Files", position = 2045),
        @ActionReference(path = "Projects/Actions", position = 101),
        @ActionReference(path = "Shortcuts", name = "SO-S")
    })
    public final static class OpenInSystemAction extends AbstractOpenInAction {
        
        public OpenInSystemAction() {
            this(Utilities.actionsGlobalContext());
        }
        
        public OpenInSystemAction(Lookup context) {
            super(NbBundle.getMessage(AbstractOpenInAction.class, "CTL_OpenInSystemAction"), context);
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new OpenInSystemAction(context);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openInSystem(files);
        }

        @Override
        public boolean isEnabled() {
            return !files.isEmpty() && canOpenInSystem();
        }
    }
    
    @ActionID(id = "org.netbeans.core.ui.sysopen.OpenParentInSystemAction", category = "Edit")
    @ActionRegistration(displayName = "#CTL_OpenParentInSystemAction", lazy = false)
    @ActionReferences ({
        @ActionReference(path = "Editors/TabActions", position = 410),
        @ActionReference(path = "UI/ToolActions/Files", position = 2046),
        @ActionReference(path = "Projects/Actions", position = 102)
    })
    public final static class OpenParentInSystemAction extends AbstractOpenInAction {
        
        public OpenParentInSystemAction() {
            this(Utilities.actionsGlobalContext());
        }
        
        public OpenParentInSystemAction(Lookup context) {
            super(NbBundle.getMessage(AbstractOpenInAction.class, "CTL_OpenParentInSystemAction"), context);
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new OpenParentInSystemAction(context);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openInSystem(Set.of(files.iterator().next().getParentFile()));
        }

        @Override
        public boolean isEnabled() {
            return files.size() == 1 && files.iterator().next().getParent() != null && canOpenInSystem();
        }
    }
    
    @ActionID(id = "org.netbeans.core.ui.sysopen.OpenInTabAction", category = "Edit")
    @ActionRegistration(displayName = "#CTL_OpenInTabAction", lazy = false)
    @ActionReferences ({
        @ActionReference(path = "Editors/TabActions", position = 415),
        @ActionReference(path = "UI/ToolActions/Files", position = 2047),
//        @ActionReference(path = "Projects/Actions", position = 103)
    })
    public final static class OpenInTabAction extends AbstractOpenInAction {
        
        public OpenInTabAction() {
            this(Utilities.actionsGlobalContext());
        }
        
        public OpenInTabAction(Lookup context) {
            super(NbBundle.getMessage(AbstractOpenInAction.class, "CTL_OpenInTabAction"), context);
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new OpenInTabAction(context);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            browse(FileUtil.toFileObject(files.iterator().next()));
        }

        @Override
        public boolean isEnabled() {
            if (files.size() == 1) {
                File file = files.iterator().next();
                try {
                    return file.isDirectory() || FileUtil.isArchiveFile(Utilities.toURI(file).toURL());
                } catch (MalformedURLException ignore) {}
            }
            return false;
        }
    }
    
    @ActionID(id = "org.netbeans.core.ui.sysopen.OpenParentInTabAction", category = "Edit")
    @ActionRegistration(displayName = "#CTL_OpenParentInTabAction", lazy = false)
    @ActionReferences ({
        @ActionReference(path = "Editors/TabActions", position = 420),
        @ActionReference(path = "UI/ToolActions/Files", position = 2048),
        @ActionReference(path = "Projects/Actions", position = 104)
    })
    public final static class OpenParentInTabAction extends AbstractOpenInAction {
        
        public OpenParentInTabAction() {
            this(Utilities.actionsGlobalContext());
        }
        
        public OpenParentInTabAction(Lookup context) {
            super(NbBundle.getMessage(AbstractOpenInAction.class, "CTL_OpenParentInTabAction"), context);
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new OpenParentInTabAction(context);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            browse(FileUtil.toFileObject(files.iterator().next().getParentFile()));
        }

        @Override
        public boolean isEnabled() {
            return files.size() == 1 && files.iterator().next().getParent() != null;
        }
    }

}
