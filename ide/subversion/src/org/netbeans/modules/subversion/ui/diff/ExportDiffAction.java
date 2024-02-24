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

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.SVNClientException;

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
public class ExportDiffAction extends ContextAction {
    
    private static final int enabledForStatus =
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    @Override
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_ExportDiff";  // NOI18N
    }

    /**
     * First look for DiffSetupSource name then for super (context name).
     */
    @Override
    public String getName() {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            String setupName = ((DiffSetupSource)activated).getSetupDisplayName();
            if (setupName != null) {
                return NbBundle.getMessage(this.getClass(), getBaseName(getActivatedNodes()) + "_Context",  // NOI18N
                                            setupName);
            }
        }
        return super.getName();
    }
    
    @Override
    public boolean enable(Node[] nodes) {
        if (super.enable(nodes)) {
            Context ctx = getCachedContext(nodes);
            if(!Subversion.getInstance().getStatusCache().containsFiles(ctx, enabledForStatus, true)) {
                return false;
            }  
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated instanceof DiffSetupSource) {
                return true;
            }
            return Lookup.getDefault().lookup(DiffProvider.class) != null;                
        } else {
            return false;
        }
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        performContextAction(nodes, false);
    }

    void performContextAction (final Node[] nodes, final boolean singleDiffSetup) {
        // reevaluate fast enablement logic guess

        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        boolean noop;
        Context context = getContext(nodes);
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            noop = ((DiffSetupSource) activated).getSetups().isEmpty();
        } else {
            noop = !Subversion.getInstance().getStatusCache().containsFiles(context, FileInformation.STATUS_LOCAL_CHANGE, true);
        }
        if (noop) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportDiffAction.class, "BK3001"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        }

        ExportDiffSupport exportDiffSupport = new ExportDiffSupport(context.getRootFiles(), SvnModuleConfig.getDefault().getPreferences()) {
            @Override
            public void writeDiffFile(final File toFile) {
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor();
                SvnProgressSupport ps = new SvnProgressSupport() {
                    @Override
                    protected void perform() {
                        async(this, nodes, toFile, singleDiffSetup);
                    }
                };
                ps.start(rp, null, getRunningName(nodes)).waitFinished();
            }
        };
        exportDiffSupport.export();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private void async(SvnProgressSupport progress, Node[] nodes, File destination, boolean singleDiffSetup) {
        // prepare setups and common parent - root

        File root;
        List<Setup> setups;

        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated instanceof DiffSetupSource) {
            if (!singleDiffSetup) {
                setups = new ArrayList<Setup>(((DiffSetupSource) activated).getSetups());
            } else {
                if (nodes.length > 0 && nodes[0] instanceof DiffNode) {
                    setups = new ArrayList<Setup>(Collections.singletonList(((DiffNode)nodes[0]).getSetup()));
                } else {
                    return;
                }
            }
            List<File> setupFiles = new ArrayList<File>(setups.size());
            for (Iterator i = setups.iterator(); i.hasNext();) {
                Setup setup = (Setup) i.next();
                setupFiles.add(setup.getBaseFile()); 
            }
            root = getCommonParent(setupFiles.toArray(new File[0]));
        } else {
            Context context = getContext(nodes);
            File [] files = SvnUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
            root = getCommonParent(context.getRootFiles());
            setups = new ArrayList<Setup>(files.length);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (!Subversion.getInstance().getStatusCache().getStatus(file).isDirectory()) {
                    Setup setup = new Setup(file, null, Setup.DIFFTYPE_LOCAL);
                    setups.add(setup);
                }
            }
        }
        exportDiff(setups, destination, root, progress);
    }

    public void exportDiff (List<Setup> setups, File destination, File root, SvnProgressSupport progress) {
        if (root == null) {
            NotifyDescriptor nd = new NotifyDescriptor(
                    NbBundle.getMessage(ExportDiffAction.class, "MSG_BadSelection_Prompt"), 
                    NbBundle.getMessage(ExportDiffAction.class, "MSG_BadSelection_Title"), 
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        boolean success = false;
        OutputStream out = null;
        int exportedFiles = 0;

        try {
            String sep = System.getProperty("line.separator"); // NOI18N
            out = new BufferedOutputStream(new FileOutputStream(destination));
            // Used by PatchAction as MAGIC to detect right encoding
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
                if (file.isDirectory()) continue;
                try {            
                    progress.setRepositoryRoot(SvnUtils.getRepositoryRootUrl(file));
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, true);
                    return;
                }                           
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
        } catch (IOException ex) {
            SvnClientExceptionHandler.notifyException(new Exception(NbBundle.getMessage(ExportDiffAction.class, "BK3003", //NOI18N
                    ex.getLocalizedMessage()), ex), true, false);
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException alreadyClsoed) {
                }
            }
            if (success) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportDiffAction.class, "BK3004", Integer.valueOf(exportedFiles)));
                if (exportedFiles == 0) {
                    destination.delete();
                } else {
                    Utils.openFile(FileUtil.normalizeFile(destination));
                }
            } else {
                destination.delete();
            }

        }
    }

    private static File getCommonParent(File [] files) {
        File root = files[0];
        if (!root.exists() || root.isFile()) root = root.getParentFile();
        for (int i = 1; i < files.length; i++) {
            root = Utils.getCommonParent(root, files[i]);
            if (root == null) return null;
        }
        return root;
    }

    /** Writes contextual diff into given stream.*/
    private void exportDiff(Setup setup, String relativePath, OutputStream out) throws IOException {
        setup.initSources();
        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);

        Reader r1 = null;
        Reader r2 = null;
        Difference[] differences;

        try {
            r1 = setup.getFirstSource().createReader();
            if (r1 == null) r1 = new StringReader("");  // NOI18N
            r2 = setup.getSecondSource().createReader();
            if (r2 == null) r2 = new StringReader("");  // NOI18N
            differences = diff.computeDiff(r1, r2);
        } finally {
            if (r1 != null) try { r1.close(); } catch (Exception e) {}
            if (r2 != null) try { r2.close(); } catch (Exception e) {}
        }

        File file = setup.getBaseFile();
        try {
            InputStream is;
            if (!SvnUtils.getMimeType(file).startsWith("text/") && differences.length == 0) {
                // assume the file is binary 
                is = new ByteArrayInputStream(exportBinaryFile(file).getBytes("utf8"));  // NOI18N
            } else {
                r1 = setup.getFirstSource().createReader();
                if (r1 == null) r1 = new StringReader(""); // NOI18N
                r2 = setup.getSecondSource().createReader();
                if (r2 == null) r2 = new StringReader(""); // NOI18N
                TextDiffVisualizer.TextDiffInfo info = new TextDiffVisualizer.TextDiffInfo(
                    relativePath + " " + setup.getFirstSource().getTitle(), // NOI18N
                    relativePath + " " + setup.getSecondSource().getTitle(),  // NOI18N
                    null,
                    null,
                    r1,
                    r2,
                    differences
                );
                info.setContextMode(true, 3);
                String diffText = TextDiffVisualizer.differenceToUnifiedDiffText(info);
                is = new ByteArrayInputStream(diffText.getBytes("utf8"));  // NOI18N
            }
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
        
    private String exportBinaryFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder((int) file.length());
        if (file.canRead()) {
            Utils.copyStreamsCloseAll(baos, new FileInputStream(file));
        }
        sb.append("MIME: application/octet-stream; encoding: Base64; length: ").append(file.canRead() ? file.length() : -1); // NOI18N
        sb.append(System.getProperty("line.separator")); // NOI18N
        sb.append(encodeToWrappedBase64(baos.toByteArray()));
        sb.append(System.getProperty("line.separator")); // NOI18N
        return sb.toString();
    }

    static String encodeToWrappedBase64(byte[] data) {
        return Base64.getMimeEncoder(
                60,
                System.getProperty("line.separator").getBytes(StandardCharsets.ISO_8859_1)
        ).encodeToString(data);
    }

}
