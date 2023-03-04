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
package org.netbeans.modules.profiler.snaptracer.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ExportSnapshotAction_ActionName=Export IDE snapshot...",
    "ExportSnapshotAction_ActionDescr=Export IDE snapshot...",
    "ExportSnapshotAction_ProgressMsg=Exporting snapshot...",
    "ExportSnapshotAction_CannotReplaceMsg=File {0} cannot be replaced.\nCheck file permissions.",
    "ExportSnapshotAction_ExportFailedMsg=Exporting snapshot failed:",
    "ExportSnapshotAction_FileChooserCaption=Select File or Directory",
    "ExportSnapshotAction_ExportButtonText=Export",
    "ExportSnapshotAction_NpssFileFilter=IDE Snapshots (*{0})",
    "ExportSnapshotAction_ExportToItselfMsg=Exporting the snapshot to itself.",
    "ExportSnapshotAction_OverwriteFileCaption=Overwrite Existing File",
    "ExportSnapshotAction_OverwriteFileMsg=File {0} already exists.\nDo you want to replace it?"
})
final class ExportSnapshotAction extends AbstractAction {
    
    private static final String NPSS_EXT = "."+ResultsManager.STACKTRACES_SNAPSHOT_EXTENSION; // NOI18N
    private static String LAST_DIRECTORY;
            
    private final FileObject snapshotFileObject;
    
    
    ExportSnapshotAction(FileObject snapshot) {
        snapshotFileObject = snapshot;
        
        putValue(Action.NAME, Bundle.ExportSnapshotAction_ActionName());
        putValue(Action.SHORT_DESCRIPTION, Bundle.ExportSnapshotAction_ActionDescr());
        putValue(Action.SMALL_ICON, Icons.getIcon(GeneralIcons.EXPORT));
        putValue("iconBase", Icons.getResource(GeneralIcons.EXPORT)); // NOI18N
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFileChooser chooser = createFileChooser();
                String filename = snapshotFileObject.getName();
                File lastDir = LAST_DIRECTORY != null ? new File(LAST_DIRECTORY) :
                                                        chooser.getCurrentDirectory();
                chooser.setSelectedFile(new File(lastDir, filename));
                Component parent = WindowManager.getDefault().getRegistry().getActivated();
                if (parent == null) parent = WindowManager.getDefault().getMainWindow();
                if (chooser.showDialog(parent, null) != JFileChooser.APPROVE_OPTION) return;
                File selected = chooser.getSelectedFile();
                if (selected.isDirectory()) {
                    LAST_DIRECTORY = selected.getAbsolutePath();
                    selected = new File(selected, filename);
                } else {
                    LAST_DIRECTORY = selected.getParent();
                }
                filename = selected.getName();
                if (!filename.toLowerCase().endsWith(NPSS_EXT)) {
                    filename+=NPSS_EXT;
                    selected = new File(selected.getParentFile(), filename);
                }
                if (!checkItselfOrOverwrite(snapshotFileObject, selected)) actionPerformed(e);
                else export(snapshotFileObject, selected);
            }
        });
    }
    
    // TODO: export also UI gestures file if available, preferably based on user option
    private static void export(final FileObject sourceFO, final File targetFile) {
        final ProgressHandle progress = ProgressHandle.createHandle(
                Bundle.ExportSnapshotAction_ProgressMsg());
        progress.setInitialDelay(500);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                progress.start();
                try {
                    if (targetFile.exists() && !targetFile.delete()) {
                        ProfilerDialogs.displayError(
                                Bundle.ExportSnapshotAction_CannotReplaceMsg(targetFile.getName()));
                    } else {
                        targetFile.toPath();
                        File targetParent = FileUtil.normalizeFile(targetFile.getParentFile());
                        FileObject targetFO = FileUtil.toFileObject(targetParent);
                        String targetName = targetFile.getName();
                        FileUtil.copyFile(sourceFO, targetFO, targetName, null);
                    }
                } catch (Throwable t) {
                    ProfilerLogger.log("Failed to export NPSS snapshot: " + t.getMessage()); // NOI18N
                    String msg = t.getLocalizedMessage().replace("<", "&lt;").replace(">", "&gt;"); // NOI18N
                    ProfilerDialogs.displayError("<html><b>" + Bundle.ExportSnapshotAction_ExportFailedMsg() + // NOI18N
                                                               "</b><br><br>" + msg + "</html>"); // NOI18N
                } finally {
                    progress.finish();
                }
            }
        });
    }
    
    private static JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle(Bundle.ExportSnapshotAction_FileChooserCaption());
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setApproveButtonText(Bundle.ExportSnapshotAction_ExportButtonText());
        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(NPSS_EXT);
            }
            public String getDescription() {
                return Bundle.ExportSnapshotAction_NpssFileFilter(NPSS_EXT);
            }
        });
        return fileChooser;
    }
    
    private static boolean checkItselfOrOverwrite(FileObject sourceFO, File target) {
        if (!target.exists()) {
            return true;
        }
        File source = FileUtil.toFile(sourceFO);
        if (source == null) {   // sourceFO is in memory
            return true;
        }
        if (source.equals(target)) {
            ProfilerDialogs.displayError(Bundle.ExportSnapshotAction_ExportToItselfMsg());
            return false;
        } else {
            return ProfilerDialogs.displayConfirmation(
                    Bundle.ExportSnapshotAction_OverwriteFileMsg(target.getName()),
                    Bundle.ExportSnapshotAction_OverwriteFileCaption());
        }
    }
    
}
