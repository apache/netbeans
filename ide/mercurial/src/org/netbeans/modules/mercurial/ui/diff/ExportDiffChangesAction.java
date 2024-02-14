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

package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.openide.nodes.Node;

/**
 * Exports diff to file:
 *
 * <ul>
 * <li>for components that implements {@link DiffSetupSource} interface
 * exports actually displayed diff.
 *
 * <li>for DataNodes <b>local</b> differencies between the current
 * working copy and BASE repository version.
 * </ul>
 *  
 * @author Petr Kuzel
 */
@NbBundle.Messages({
    "CTL_MenuItem_ExportDiffChanges=Export &Uncommitted Changes...",
    "CTL_PopupMenuItem_ExportDiffChanges=Export Uncommitted Changes..."
})
public class ExportDiffChangesAction extends ContextAction {

    private static final Logger LOG = Logger.getLogger(ExportDiffChangesAction.class.getName());
    static final String PATCH_FILE_HEADER = "NetBeans IDE HG Patch"; //NOI18N

    @Override
    protected boolean enable(Node[] nodes) {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            return true;
        }
        VCSContext context = HgUtils.getCurrentContext(nodes);
        if(!HgUtils.isFromHgRepository(context)) {
            return false;
        }
        return Lookup.getDefault().lookup(DiffProvider.class) != null;
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ExportDiffChanges"; // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        performContextAction(nodes, false);
    }

    void performContextAction (Node[] nodes, final boolean singleDiffSetup) {
        boolean noop;
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        TopComponent activated = TopComponent.getRegistry().getActivated();
        Collection<Setup> setups = null;
        if (activated instanceof DiffSetupSource) {
            noop = (setups = ((DiffSetupSource) activated).getSetups()).isEmpty();
        } else {
            File [] files = HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE, false);
            noop = files.length == 0;
        }
        if (noop) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportDiffChangesAction.class, "BK3001"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        File[] roots = setups == null ? HgUtils.getActionRoots(context) : getRoots(setups);
        if (roots == null || roots.length == 0) {
            LOG.log(Level.INFO, "Null roots for {0}", context.getRootFiles()); //NOI18N
            return;
        }
        File contextFile = roots[0];
        final File root = Mercurial.getInstance().getRepositoryRoot(contextFile);
        ExportDiffSupport exportDiffSupport = new ExportDiffSupport(new File[] {contextFile}, HgModuleConfig.getDefault().getPreferences()) {
            @Override
            public void writeDiffFile(final File toFile) {
                ExportDiffAction.saveFolderToPrefs(toFile);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                HgProgressSupport ps = new HgProgressSupport() {
                    @Override
                    protected void perform() {
                        async(this, root, context, toFile, singleDiffSetup);
                    }
                };
                ps.start(rp, root, org.openide.util.NbBundle.getMessage(ExportDiffChangesAction.class, "LBL_ExportChanges_Progress")).waitFinished();
            }
        };
        exportDiffSupport.export();

    }
    
    private void async(HgProgressSupport progress, File root, VCSContext context, File destination, boolean singleDiffSetup) {
        List<Setup> setups;
        Mercurial hg = Mercurial.getInstance();

        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            if (!singleDiffSetup) {
                setups = new ArrayList<Setup>(((DiffSetupSource) activated).getSetups());
            } else {
                DiffNode node = context.getElements().lookup(DiffNode.class);
                if (node != null) {
                    setups = new ArrayList<Setup>(Collections.singletonList(node.getSetup()));
                } else {
                    LOG.log(Level.INFO, "No DiffNode in the context: {0}", new Object[]{context.getElements().lookup(Object.class)}); //NOI18N
                    return;
                }
            }
            for (Iterator i = setups.iterator(); i.hasNext();) {
                Setup setup = (Setup) i.next();
                File file = setup.getBaseFile();
                // remove files from other repositories
                if (!root.equals(hg.getRepositoryRoot(file))) {
                    i.remove();
                }
            }
        } else {
            File [] files = HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE, false);
            setups = new ArrayList<Setup>(files.length);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (root.equals(hg.getRepositoryRoot(file)))  {
                    Mercurial.LOG.log(Level.FINE, "preparing setup {0}", file); //NOI18N
                    Setup setup = new Setup(file, null, Setup.DIFFTYPE_LOCAL);
                    Mercurial.LOG.log(Level.FINE, "setup prepared {0}", setup.getBaseFile()); //NOI18N
                    setups.add(setup);
                }
            }
        }
        exportDiff(setups, destination, root, progress);
    }
    
    /**
     * 
     * @param setups
     * @param destination patch file to save changes to. If export fails for some reason, the file will not be created and will be deleted if existed before.
     * @param root
     * @param progress 
     */
    public void exportDiff (List<Setup> setups, File destination, File root, HgProgressSupport progress) {
        boolean success = false;
        OutputStream out = null;
        int exportedFiles = 0;
        OutputLogger logger = progress.getLogger();
        try {
            if (root == null) {
                NotifyDescriptor nd = new NotifyDescriptor(
                        NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_BadSelection_Prompt"), 
                        NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_BadSelection_Title"), 
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }

            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffChangesAction.class,
                    "MSG_EXPORT_CHANGES_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(ExportDiffChangesAction.class,
                    "MSG_EXPORT_CHANGES_TITLE_SEP")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_EXPORT_CHANGES", destination)); // NOI18N

            String sep = System.getProperty("line.separator"); // NOI18N
            ensureParentExists(destination);
            out = new BufferedOutputStream(new FileOutputStream(destination));
            // Used by PatchAction as MAGIC to detect right encoding
            out.write(("# " + PATCH_FILE_HEADER + sep).getBytes("utf8"));  // NOI18N
            out.write(("# This patch file was generated by NetBeans IDE" + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Following Index: paths are relative to: " + root.getAbsolutePath() + sep).getBytes("utf8"));  // NOI18N
            out.write(("# This patch can be applied using context Tools: Patch action on respective folder." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# It uses platform neutral UTF-8 encoding and \\n newlines." + sep).getBytes("utf8"));  // NOI18N
            out.write(("# Above lines and this line are ignored by the patching process." + sep).getBytes("utf8"));  // NOI18N


            setups.sort(new Comparator<Setup>() {
                @Override
                public int compare(Setup o1, Setup o2) {
                    return o1.getBaseFile().compareTo(o2.getBaseFile());
                }
            });
            Iterator<Setup> it = setups.iterator();
            int i = 0;
            while (it.hasNext()) {
                Setup setup = it.next();
                File file = setup.getBaseFile();                
                Mercurial.LOG.log(Level.FINE, "exporting setup {0}", file.getName()); //NOI18N
                logger.output(NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_Export_Changes_Exporting", file.getName())); //NOI18N
                if (file.isDirectory()) continue;
                progress.setDisplayName(file.getName());

                String index = "Index: ";   // NOI18N
                String rootPath = root.getAbsolutePath();
                String filePath = file.getAbsolutePath();
                String relativePath = filePath;
                if (filePath.startsWith(rootPath)) {
                    relativePath = filePath.substring(rootPath.length() + 1).replace(File.separatorChar, '/');
                    index += relativePath + sep;
                    out.write(index.getBytes("utf8")); // NOI18N
                }
                exportDiff(setup, relativePath, out);
                i++;
            }

            exportedFiles = i;
            success = true;
            logger.outputInRed(NbBundle.getMessage(ExportDiffChangesAction.class, "MSG_EXPORT_CHANGES_DONE")); // NOI18N
        } catch (IOException ex) {
            logger.outputInRed(NbBundle.getMessage(ExportDiffChangesAction.class, "BK3003")); //NOI18N
            Mercurial.LOG.log(Level.INFO, NbBundle.getMessage(ExportDiffChangesAction.class, "BK3003"), ex);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException alreadyClosed) {
                }
            }
            if (success) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportDiffChangesAction.class, "BK3004", Integer.valueOf(exportedFiles)));
                if (exportedFiles == 0) {
                    destination.delete();
                } else {
                    Utils.openFile(destination);
                }
            } else {
                destination.delete();
            }
            logger.output(""); // NOI18N
        }
    }

    /** Writes contextual diff into given stream.*/
    private void exportDiff(Setup setup, String relativePath, OutputStream out) throws IOException {
        setup.initSources();
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);

        Reader r1 = null;
        Reader r2 = null;
        Difference[] differences;
        FileStatus fileStatus = setup.getInfo().getStatus(null);

        try {
            if (fileStatus == null || !fileStatus.isCopied()) {
                r1 = setup.getFirstSource().createReader();
            }
            if (r1 == null) r1 = new StringReader("");  // NOI18N
            r2 = setup.getSecondSource().createReader();
            if (r2 == null) r2 = new StringReader("");  // NOI18N
            differences = diff.computeDiff(r1, r2);
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }

        try {
            InputStream is;
            r1 = null;
            if (fileStatus == null || !fileStatus.isCopied()) {
                r1 = setup.getFirstSource().createReader();
            }
            if (r1 == null) r1 = new StringReader(""); // NOI18N
            r2 = setup.getSecondSource().createReader();
            if (r2 == null) r2 = new StringReader(""); // NOI18N
            TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
                relativePath, // NOI18N
                relativePath,  // NOI18N
                null,
                null,
                r1,
                r2,
                differences
            );
            info.setContextMode(true, 3);
            String diffText = TextDiffVisualizer.differenceToUnifiedDiffText(info);
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

    private void ensureParentExists(File destination) {
        File parent = destination.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
    }

    private File[] getRoots (Collection<Setup> setups) {
        HashSet<File> roots = new HashSet<File>(setups.size());
        for (Setup setup : setups) {
            File f = setup.getBaseFile();
            if (f != null) {
                roots.add(f);
            }
        }
        return roots.toArray(new File[0]);
    }
}
