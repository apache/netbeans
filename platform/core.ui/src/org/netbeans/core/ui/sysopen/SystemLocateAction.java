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
import java.util.Collection;
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
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Open the selected file(s) with the system default tool.
 *
 * @author Jesse Glick
 */
@ActionID(id = "org.netbeans.core.ui.sysopen.SystemLocateAction", category = "Edit")
@ActionRegistration(displayName = "#CTL_SystemLocateAction", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/TabActions", position = 402),
    @ActionReference(path = "UI/ToolActions/Files", position = 2046),
    @ActionReference(path = "Projects/Actions", position = 102),
    @ActionReference(path = "Shortcuts", name = "DO-S")
})
public final class SystemLocateAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor PROC = new RequestProcessor(SystemLocateAction.class);

    private final Lookup.Result<DataObject> result;
    private final LookupListener resultListener;

    private FileObject file;

    public SystemLocateAction() {
        this(Utilities.actionsGlobalContext());
    }

    private SystemLocateAction(Lookup context) {
        super(NbBundle.getMessage(SystemLocateAction.class, "CTL_SystemLocateAction"));
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        result = context.lookupResult(DataObject.class);
        resultListener = e -> updateFile();
        result.addLookupListener(WeakListeners.create(LookupListener.class, resultListener, result));
        updateFile();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileObject fo = file;
        if (fo == null) {
            return;
        }
        PROC.post(() -> { // #176879: asynch
            Desktop desktop = Desktop.getDesktop();
            try {
                if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                    desktop.browseFileDirectory(FileUtil.toFile(fo));
                } else {
                    desktop.open(FileUtil.toFile(fo.getParent()));
                }
            } catch (Exception x) {
                Logger.getLogger(SystemLocateAction.class.getName()).log(Level.INFO, null, x);
                // XXX or perhaps notify user of problem; but not very useful on Unix at least (#6940853)
            }
        });
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new SystemLocateAction(context);
    }

    @Override
    public boolean isEnabled() {
        return file != null && Desktop.isDesktopSupported()
                && (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)
                || Desktop.getDesktop().isSupported(Desktop.Action.OPEN));
    }

    private void updateFile() {
        Collection<? extends DataObject> dobs = result.allInstances();
        if (dobs.size() == 1) {
            FileObject fo = dobs.iterator().next().getPrimaryFile();
            if (fo.isData()) {
                this.file = fo;
            } else {
                this.file = null;
            }
        } else {
            file = null;
        }
    }

}
