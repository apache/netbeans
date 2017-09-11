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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.diff;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.diff.builtin.ContextualPatch;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Patch Action. It asks for a patch file and applies it to the selected file.
 *
 * @author  Martin Entlicher
 * @author Maros Sandor
 */
public class PatchAction extends NodeAction {
    
    private static final String PREF_RECENT_PATCH_PATH = "patch.recentPatchDir";
    // for tests
    private static boolean skipReport = false;

    /** Creates a new instance of PatchAction */
    public PatchAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PatchAction.class, "CTL_PatchActionName");
    }

    @Override
    public boolean enable(Node[] nodes) {
        if (nodes.length == 1) {
            FileObject fo = DiffAction.getFileFromNode(nodes[0]);
            if (fo != null) {
                try {
                    // #63460
                    return fo.getURL().getProtocol().equals("file");  // NOI18N
                } catch (FileStateInvalidException fsiex) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * @return false to run in AWT thread.
     */
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public void performAction(Node[] nodes) {
        final FileObject fo = DiffAction.getFileFromNode(nodes[0]);
        if (fo != null) {
            final File patch = getPatchFor(fo);
            if (patch == null) return ;
            Utils.postParallel(new Runnable () {
                @Override
                public void run() {
                    performPatch(patch, FileUtil.toFile(fo));
                }
            });
        }
    }

    public static boolean performPatch(File patch, File file) throws MissingResourceException {
        ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(PatchAction.class, "MSG_AplyingPatch", new Object[] {patch.getName()}));
        List<ContextualPatch.PatchReport> report = null;
        try {
            ph.start();
            ContextualPatch cp = ContextualPatch.create(patch, file);
            try {
                report = cp.patch(false, ph);
            } catch (Exception ioex) {
                ErrorManager.getDefault().annotate(ioex, NbBundle.getMessage(PatchAction.class, "EXC_PatchParsingFailed", ioex.getLocalizedMessage()));
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioex);
                ErrorManager.getDefault().notify(ErrorManager.USER, ioex);
                return false;
            }
        } finally {
            ph.finish();
        }
        return displayPatchReport(report, patch);
    }


    private static boolean displayPatchReport(List<ContextualPatch.PatchReport> report, final File patchFile) {

        List<ContextualPatch.PatchReport> successful = new ArrayList<ContextualPatch.PatchReport>(); 
        List<ContextualPatch.PatchReport> failed = new ArrayList<ContextualPatch.PatchReport>();
            
        for (ContextualPatch.PatchReport patchReport : report) {
            switch (patchReport.getStatus()) {
            case Patched:
                successful.add(patchReport);
                break;
            case Failure:
            case Missing:
                failed.add(patchReport);
                break;
            }
        }

        InputOutput log = IOProvider.getDefault().getIO("Patch Report", false);        
        OutputWriter ow = log.getOut();
        if (log.isClosed()) {
            try {
                ow.reset();
            } catch (IOException ex) {
            }
            log.select();
        }

        try {
            ow.print(DateFormat.getDateTimeInstance().format(new Date()));
            ow.println("  ===========================================================================");
            ow.print(NbBundle.getMessage(PatchAction.class, "MSG_PatchAction.output.patchFile")); //NOI18N
            try {
                ow.println(patchFile.getAbsolutePath(), new OutputListener() {
                    @Override
                    public void outputLineSelected (OutputEvent ev) {
                    }

                    @Override
                    public void outputLineAction (OutputEvent ev) {
                        Utils.openFile(patchFile);
                    }

                    @Override
                    public void outputLineCleared (OutputEvent ev) {
                    }
                });
            } catch (IOException ex) {
                ow.println(patchFile.getAbsolutePath());
            }
            ow.println("--- Successfully Patched ---");
            if (successful.size() > 0) {
                for (ContextualPatch.PatchReport patchReport : successful) {
                    ow.println(patchReport.getFile().getAbsolutePath());
                }
            } else {
                ow.println("<none>");
            }

            ow.println("--- Failed ---");
            if (failed.size() > 0) {
                for (ContextualPatch.PatchReport patchReport : failed) {
                    ow.print(patchReport.getFile().getAbsolutePath());
                    ow.print(" (");
                    ow.print(patchReport.getFailure().getLocalizedMessage());
                    ow.println(" )");
                }
            } else {
                ow.println("<none>");
            }
        } finally {
            ow.close();
        }
        
        if (successful.size() > 0) {
            List<FileObject> binaries = new ArrayList<FileObject>();
            List<FileObject> appliedFiles = new ArrayList<FileObject>();
            Map<FileObject, FileObject> backups = new HashMap<FileObject, FileObject>();
            for (ContextualPatch.PatchReport patchReport : successful) {
                FileObject fo = FileUtil.toFileObject(patchReport.getFile());
                FileObject backup = FileUtil.toFileObject(patchReport.getOriginalBackupFile());
                if (patchReport.isBinary()) {
                    binaries.add(fo);
                }
                appliedFiles.add(fo);
                backups.put(fo, backup);
            }
            
            if (skipReport) {
                return failed.isEmpty();
            }
            
            String message = failed.size() > 0 ? NbBundle.getMessage(PatchAction.class, "MSG_PatchAppliedPartially") : NbBundle.getMessage(PatchAction.class, "MSG_PatchAppliedSuccessfully");
            Object notifyResult = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    message,
                    NotifyDescriptor.YES_NO_OPTION));
            if (NotifyDescriptor.YES_OPTION.equals(notifyResult)) {
                showDiffs(appliedFiles, binaries, backups);
                removeBackups(appliedFiles, backups, true);
            } else {
                removeBackups(appliedFiles, backups, false);
            }
            return failed.isEmpty();
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(PatchAction.class, "MSG_WrongPatch")));
            return false;
        }
    }

    private File getPatchFor(FileObject fo) {
        JFileChooser chooser = new JFileChooser();
        String patchDirPath = DiffModuleConfig.getDefault().getPreferences().get(PREF_RECENT_PATCH_PATH, System.getProperty("user.home"));
        File patchDir = new File(patchDirPath);
        while (!patchDir.isDirectory()) {
            patchDir = patchDir.getParentFile();
            if (patchDir == null) {
                patchDir = new File(System.getProperty("user.home"));
                break;
            }
        }
        FileUtil.preventFileChooserSymlinkTraversal(chooser, patchDir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String title = NbBundle.getMessage(PatchAction.class,
            (fo.isData()) ? "TITLE_SelectPatchForFile"
                          : "TITLE_SelectPatchForFolder", fo.getNameExt());
        chooser.setDialogTitle(title);

        // setup filters, default one filters patch files
        FileFilter patchFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(PatchAction.class, "CTL_PatchDialog_FileFilter");
            }
        };
        chooser.addChoosableFileFilter(patchFilter);
        chooser.setFileFilter(patchFilter);

        chooser.setApproveButtonText(NbBundle.getMessage(PatchAction.class, "BTN_Patch"));
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(PatchAction.class, "BTN_Patch_mnc").charAt(0));
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(PatchAction.class, "BTN_Patch_tooltip"));
        HelpCtx ctx = new HelpCtx(PatchAction.class.getName());
        DialogDescriptor descriptor = new DialogDescriptor( chooser, title, true, new Object[0], null, 0, ctx, null );
        final Dialog dialog = DialogDisplayer.getDefault().createDialog( descriptor );
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PatchAction.class, "ACSD_PatchDialog"));

        ChooserListener listener = new PatchAction.ChooserListener(dialog,chooser);
	chooser.addActionListener(listener);
        dialog.setVisible(true);

        File selectedFile = listener.getFile();
        if (selectedFile != null) {
            DiffModuleConfig.getDefault().getPreferences().put(PREF_RECENT_PATCH_PATH, selectedFile.getParentFile().getAbsolutePath());
        }
        return selectedFile;
    }

    private static void showDiffs(List<FileObject> files, List<FileObject> binaries, Map<FileObject, FileObject> backups) {
        for (int i = 0; i < files.size(); i++) {
            FileObject file = files.get(i);
            FileObject backup = backups.get(file);
            if (binaries.contains(file)) continue;
            if (backup == null) {
                try {
                    backup = FileUtil.toFileObject(FileUtil.normalizeFile(File.createTempFile("diff-empty-backup", "")));
                } catch (IOException e) {
                    // ignore
                }
            }
            DiffAction.performAction(backup, file, file);
        }
    }

    /** Removes the backup copies of files upon the successful application 
     * of a patch (.orig files).
     * @param files a list of files, to which the patch was successfully applied
     * @param backups a map of a form original file -> backup file
     */
    private static void removeBackups(List<FileObject> files, Map<FileObject, FileObject> backups, boolean onExit) {
        StringBuffer filenames=new StringBuffer(), 
                     exceptions=new StringBuffer();
        for (int i = 0; i < files.size(); i++) {
            FileObject targetFileObject = files.get(i);
            FileObject backup= backups.get(targetFileObject);

            // delete files that become empty and they have a backup file
            if (targetFileObject != null && targetFileObject.getSize() == 0) {
                if (backup != null && backup.isValid() && backup.getSize() > 0) {
                    if (onExit) {
                        deleteOnExit(targetFileObject);
                    } else {
                        try {
                            targetFileObject.delete();
                        } catch (IOException e) {
                            ErrorManager err = ErrorManager.getDefault();
                            err.annotate(e, "Patch can not delete file, skipping...");
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                }
            }

            if (backup != null && backup.isValid()) {
                if (onExit) {
                    deleteOnExit(backup);
                } else {
                    try {
                        backup.delete();
                    }
                    catch (IOException ex) {
                        filenames.append(FileUtil.getFileDisplayName(backup));
                        filenames.append('\n');
                        exceptions.append(ex.getLocalizedMessage());
                        exceptions.append('\n');
                    }
                }
            }
        }
        if (filenames.length()>0)
            ErrorManager.getDefault().notify(
                ErrorManager.getDefault().annotate(new IOException(),
                    NbBundle.getMessage(PatchAction.class, 
                        "EXC_CannotRemoveBackup", filenames, exceptions)));
    }
    
    private static void deleteOnExit(FileObject fo) {
        File file = FileUtil.toFile(fo);
        if (file != null) {
            file.deleteOnExit();
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PatchAction.class);
    }

    class ChooserListener implements ActionListener{
        private Dialog dialog;
        private JFileChooser chooser;
        private File file = null;

        public ChooserListener(Dialog dialog,JFileChooser chooser){
            super();
            this.dialog = dialog;
            this.chooser = chooser;
        }

        @Override
        public void actionPerformed(ActionEvent e){
            String command  = e.getActionCommand();
            if(command == JFileChooser.APPROVE_SELECTION){
                if(dialog != null) {
                    file = chooser.getSelectedFile();
                    dialog.setVisible(false);

                }
            }else{
                if(dialog != null){
                    file = null;
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        }
        public File getFile(){
            return file;
        }
    }

}
