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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.diff.builtin;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.options.AccessibleJFileChooser;
import org.netbeans.modules.diff.DiffModuleConfig;
import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.awt.StatusDisplayer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.modules.diff.Utils;

/**
 * Patch export facility.
 * 
 * @author Maros Sandor
 */
public class ExportPatch {

    private static final FileFilter unifiedFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ExportPatch.class, "FileFilter_Unified");
        }
    };

    private static final FileFilter normalFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ExportPatch.class, "FileFilter_Normal");
        }
    };
    
    /**
     * Prompts the user for the destination for the patch and the patch format.
     * 
     * @param base array of base files
     * @param modified array of modified files
     */
    public static void exportPatch(final StreamSource [] base, final StreamSource [] modified) {
        final JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportPatch.class, "ACSD_Export"));
        chooser.setDialogTitle(NbBundle.getMessage(ExportPatch.class, "CTL_Export_Title"));
        chooser.setMultiSelectionEnabled(false);
        FileFilter[] old = chooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            chooser.removeChoosableFileFilter(fileFilter);
        }
        chooser.setCurrentDirectory(new File(DiffModuleConfig.getDefault().getPreferences().get("ExportDiff.saveFolder", System.getProperty("user.home")))); // NOI18N
        chooser.addChoosableFileFilter(normalFilter);
        chooser.addChoosableFileFilter(unifiedFilter);
        
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);  // #71861
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(ExportPatch.class, "MNE_Export_ExportAction").charAt(0));
        chooser.setApproveButtonText(NbBundle.getMessage(ExportPatch.class, "CTL_Export_ExportAction"));
        DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(ExportPatch.class, "CTL_Export_Title"));
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        chooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String state = (String)e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    File destination = chooser.getSelectedFile();
                    String name = destination.getName();

                    boolean requiredExt = false;
                    final FileFilter selectedFileFilter = chooser.getFileFilter();
                    requiredExt |= name.endsWith(".diff");  // NOI18N
                    requiredExt |= name.endsWith(".patch"); // NOI18N
                    if (requiredExt == false) {
                        File parent = destination.getParentFile();
                        destination = new File(parent, name + ".patch"); // NOI18N
                    }

                    if (destination.exists()) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportPatch.class, "BK3005", destination.getAbsolutePath()));
                        nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                            return;
                        }
                    }

                    DiffModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", destination.getParent());

                    final File out = destination;
                    Utils.postParallel(new Runnable() {
                        @Override
                        public void run() {
                            exportDiff(base, modified, out, selectedFileFilter);
                        }
                    });
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private static void exportDiff(StreamSource[] base, StreamSource[] modified, File destination, FileFilter format) {
        boolean success = false;
        OutputStream out = null;
        int exportedFiles = 0;
        try {
            String sep = System.getProperty("line.separator"); // NOI18N
            out = new BufferedOutputStream(new FileOutputStream(destination));
            // Used by PatchAction as MAGIC to detect right encoding
            out.write(("# This patch file was generated by NetBeans IDE" + sep).getBytes("utf8"));  // NOI18N
            out.write(("# This patch can be applied using context Tools: Apply Diff Patch action on respective folder." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# It uses platform neutral UTF-8 encoding." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Above lines and this line are ignored by the patching process." + sep).getBytes("utf8"));  // NOI18N

            // TODO: sort files
            for (int i = 0; i < base.length; i++) {
                exportDiff(base[i], modified[i], out, format);
                exportedFiles++;
            }
            success = true;
        } catch (IOException ex) {
            ErrorManager.getDefault().annotate(ex, NbBundle.getMessage(ExportPatch.class, "BK3003"));
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);   // stack trace to log
            ErrorManager.getDefault().notify(ErrorManager.USER, ex);  // message to user
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException alreadyClsoed) {
                }
            }
            if (success) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportPatch.class, "BK3004", new Integer(exportedFiles)));
                if (exportedFiles == 0) {
                    destination.delete();
                } else {
                    openFile(destination);
                }
            } else {
                destination.delete();
            }

        }
    }

    private static void exportDiff(StreamSource base, StreamSource modified, OutputStream out, FileFilter format) throws IOException {
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);

        Reader r1 = null;
        Reader r2 = null;
        Difference[] differences;

        try {
            r1 = base.createReader();
            if (r1 == null) r1 = new StringReader("");  // NOI18N
            r2 = modified.createReader();
            if (r2 == null) r2 = new StringReader("");  // NOI18N
            differences = diff.computeDiff(r1, r2);
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }

        try {
            InputStream is;
            r1 = base.createReader();
            if (r1 == null) r1 = new StringReader(""); // NOI18N
            r2 = modified.createReader();
            if (r2 == null) r2 = new StringReader(""); // NOI18N
            TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
                base.getTitle(), // NOI18N
                modified.getTitle(),  // NOI18N
                null,
                null,
                r1,
                r2,
                differences
            );
            info.setContextMode(true, 3);
            String diffText;
            if (format == unifiedFilter) {
                diffText = TextDiffVisualizer.differenceToUnifiedDiffText(info);
            } else {
                diffText = TextDiffVisualizer.differenceToNormalDiffText(info);
            }
            is = new ByteArrayInputStream(diffText.getBytes("utf8"));  // NOI18N
            while(true) {
                int i = is.read();
                if (i == -1) break;
                out.write(i);
            }
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }
    }

    private static void openFile(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                OpenCookie oc = dao.getCookie(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                }
            } catch (DataObjectNotFoundException e) {
                // nonexistent DO, do nothing
            }
        }
    }
    
    private ExportPatch() {
    }
}
