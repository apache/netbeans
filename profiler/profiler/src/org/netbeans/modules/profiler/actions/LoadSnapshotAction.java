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

package org.netbeans.modules.profiler.actions;

import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.ResultsManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.windows.WindowManager;


/**
 * An action to prompt to select file and load/open snapshot from it.
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "LoadSnapshotAction_ActionName=&Load Snapshot...",
    "LoadSnapshotAction_ActionDescr=Load profiling results snapshot from disk",
    "LoadSnapshotAction_OpenSnapshotDialogCaption=Open Snapshot File",
    "LoadSnapshotAction_ProfilerSnapshotFileFilter=Profiler Snapshot Files (*.{0})",
    "LoadSnapshotAction_OpenSnapshotHeapdumpDialogCaption=Open Snapshot or Heap Dump",
    "LoadSnapshotAction_ProfilerSnapshotHeapdumpFileFilter=Profiler Snapshot or Heap Dump Files (*.{0} | *.{1})",
    "LoadSnapshotAction_No_Snapshot_Selected=Not a .nps snapshot file"
})
@ActionID(id = "org.netbeans.modules.profiler.actions.LoadSnapshotAction", category = "Profile")
//@ActionRegistration(iconInMenu = true, displayName = "#LoadSnapshotAction_ActionName", iconBase = "org/netbeans/modules/profiler/impl/icons/openSnapshot.png")
//@ActionReference(path = "Menu/Profile", position = 1400)
public final class LoadSnapshotAction extends AbstractAction {
    //~ Static fields/initializers ----------------------------------------------------------------------------------------------- 
    private static File importDir;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public LoadSnapshotAction() {
        putValue(Action.SHORT_DESCRIPTION, Bundle.LoadSnapshotAction_ActionDescr());
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent e) {
        loadSnapshot(false);
    }

    public void loadSnapshotOrHeapdump() {
        loadSnapshot(true);
    }

    // NOTE: supports also loading HeapDumps to simplify implementation of Load button in Control Panel
    private void loadSnapshot(final boolean handleHeapdumps) {
        JFileChooser chooser = new JFileChooser();

        if (importDir != null) {
            chooser.setCurrentDirectory(importDir);
        }

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle(handleHeapdumps ? 
            Bundle.LoadSnapshotAction_OpenSnapshotHeapdumpDialogCaption() : 
            Bundle.LoadSnapshotAction_OpenSnapshotDialogCaption());
        chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    String fname = f.getName();
                    if (fname.endsWith("." + ResultsManager.SNAPSHOT_EXTENSION)) return true; // NOI18N
                    if (fname.endsWith("." + ResultsManager.STACKTRACES_SNAPSHOT_EXTENSION)) return true; // NOI18N
                    if (handleHeapdumps && fname.endsWith("." + ResultsManager.HEAPDUMP_EXTENSION)) return true; // NOI18N
                    return false;
                }

                public String getDescription() {
                    return handleHeapdumps
                           ? Bundle.LoadSnapshotAction_ProfilerSnapshotHeapdumpFileFilter(
                                ResultsManager.SNAPSHOT_EXTENSION, 
                                ResultsManager.HEAPDUMP_EXTENSION)
                           : Bundle.LoadSnapshotAction_ProfilerSnapshotFileFilter(
                                ResultsManager.SNAPSHOT_EXTENSION);
                }
            });

        if (chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            final File[] files = chooser.getSelectedFiles();
            final ArrayList<FileObject> snapshotsFOArr = new ArrayList<>();
            final ArrayList<File> heapdumpsFArr = new ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fname = file.getName();

                if (fname.endsWith("." + ResultsManager.SNAPSHOT_EXTENSION) || fname.endsWith("." + ResultsManager.STACKTRACES_SNAPSHOT_EXTENSION)) { // NOI18N
                    snapshotsFOArr.add(FileUtil.toFileObject(FileUtil.normalizeFile(file)));
                } else if (fname.endsWith("." + ResultsManager.HEAPDUMP_EXTENSION)) { // NOI18N
                    heapdumpsFArr.add(file);
                }
            }

            if (!snapshotsFOArr.isEmpty()) {
                processor().post(new Runnable() {
                    public void run() {
                        final LoadedSnapshot[] imported = ResultsManager.getDefault().loadSnapshots(
                                snapshotsFOArr.toArray(new FileObject[0]));
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ResultsManager.getDefault().openSnapshots(imported);
                            }
                        });
                    }
                });
            } else if (!handleHeapdumps) {
                ProfilerDialogs.displayError(Bundle.LoadSnapshotAction_No_Snapshot_Selected());

            }

            if (!heapdumpsFArr.isEmpty()) {
                processor().post(new Runnable() {
                    public void run() {
                        for (File heapDump : heapdumpsFArr) {
                            ResultsManager.getDefault().openSnapshot(heapDump);
                        }
                    }
                });
            }

            importDir = chooser.getCurrentDirectory();
        }
    }
    
    private static Reference<RequestProcessor> PROCESSOR_REF;
    private static synchronized RequestProcessor processor() {
        RequestProcessor processor = PROCESSOR_REF == null ? null : PROCESSOR_REF.get();
        if (processor == null) {
            processor = new RequestProcessor("Profiler Snapshot Loader", 3); // NOI18N
            PROCESSOR_REF = new WeakReference(processor);
        }
        return processor;
    }
}
