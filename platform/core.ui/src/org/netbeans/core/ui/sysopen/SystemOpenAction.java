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
@ActionID(id = "org.netbeans.core.ui.sysopen.SystemOpenAction", category = "Edit")
@ActionRegistration(displayName = "#CTL_SystemOpenAction", lazy=false)
@ActionReferences ({
    @ActionReference(path = "UI/ToolActions/Files", position = 2045),
    @ActionReference(path = "Projects/Actions", position = 101),
    @ActionReference(path = "Shortcuts", name = "SO-S")
})
public final class SystemOpenAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor PROC = new RequestProcessor(SystemOpenAction.class);

    private final Lookup.Result<DataObject> result;
    private final LookupListener resultListener;
    private final Set<File> files;

    public SystemOpenAction() {
        this(Utilities.actionsGlobalContext());
    }

    private SystemOpenAction(Lookup context) {
        super(NbBundle.getMessage(SystemOpenAction.class, "CTL_SystemOpenAction"));
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        result = context.lookupResult(DataObject.class);
        resultListener = e -> updateFileSet();
        result.addLookupListener(WeakListeners.create(LookupListener.class, resultListener, result));
        files = new HashSet<>();
        updateFileSet();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PROC.post(() -> { // #176879: asynch
            Desktop desktop = Desktop.getDesktop();
            for (File f : files) {
                try {
                    desktop.open(f);
                } catch (IOException x) {
                    Logger.getLogger(SystemOpenAction.class.getName()).log(Level.INFO, null, x);
                    // XXX or perhaps notify user of problem; but not very useful on Unix at least (#6940853)
                }
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new SystemOpenAction(context);
    }

    @Override
    public boolean isEnabled() {
        return !files.isEmpty() && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
    }

    private void updateFileSet() {
        files.clear();
        for (DataObject d : result.allInstances()) {
            File f = FileUtil.toFile(d.getPrimaryFile());
            if (f == null || /* #144575 */ Utilities.isWindows() && f.isFile() && !f.getName().contains(".")) {
                files.clear();
                break;
            }
            files.add(f);
        }
    }

}
