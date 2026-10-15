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

package org.netbeans.modules.git.ui.conflicts;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.spi.diff.*;

import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.filesystems.*;

import org.netbeans.api.diff.*;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitConflictDescriptor;
import org.netbeans.libs.git.GitConflictDescriptor.Type;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class ResolveConflictsExecutor extends GitProgressSupport {
    
    private static final String TMP_PREFIX = "merge-"; // NOI18N
    
    static final String CHANGE_LEFT = "<<<<<<< "; // NOI18N
    static final String CHANGE_RIGHT = ">>>>>>> "; // NOI18N
    static final String CHANGE_DELIMETER = "======="; // NOI18N
    static final String CHANGE_BASE_DELIMETER = "|||||||"; // NOI18N
    
    private String leftFileRevision = null;
    private String rightFileRevision = null;

    private static final Logger LOG = Logger.getLogger(ResolveConflictsExecutor.class.getName());

    private final File[] files;
    private final Set<File> toResolve;
    private String newLineString;
    private static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");

    public ResolveConflictsExecutor(File[] files) {
        super();
        this.files = files;
        this.toResolve = new HashSet<File>();
    }

    private void exec (MergeVisualizer merge, File file) {
        try {
            FileObject fo = FileUtil.toFileObject(file);
            if(fo == null) {
                LOG.warning("can't resolve conflicts for null fileobject : " + file + ", exists: " + file.exists());
                return;
            }
            FileLock lock = fo.lock();
            boolean mergeViewerDisplayed = false;
            try {
                mergeViewerDisplayed = handleMergeFor(file, fo, lock, merge);
            } finally {
                if (!mergeViewerDisplayed) {
                    lock.releaseLock();
                }
            }
        } catch (FileAlreadyLockedException e) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Set components = TopComponent.getRegistry().getOpened();
                    for (Iterator i = components.iterator(); i.hasNext();) {
                        TopComponent tc = (TopComponent) i.next();
                        if (tc.getClientProperty(ResolveConflictsExecutor.class.getName()) != null) {
                            tc.requestActive();
                        }
                    }
                }
            });
        } catch (Exception ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        }
    }
    
    private boolean handleMergeFor(final File file, FileObject fo, FileLock lock, final MergeVisualizer merge) throws IOException {
        String mimeType = (fo == null) ? "text/plain" : fo.getMIMEType(); // NOI18N
        File folder = Utils.getTempFolder();
        File f1 = new File(folder, TMP_PREFIX + "ours-" + file.getName()); //NOI18N
        f1.createNewFile();
        f1.deleteOnExit();
        File f2 = new File(folder, TMP_PREFIX + "theirs-" + file.getName()); //NOI18N
        f1.createNewFile();
        f2.deleteOnExit();
        
        File f3 = new File(folder, TMP_PREFIX + "result-" + file.getName()); //NOI18N
        f3.deleteOnExit();
        
        newLineString = Utils.getLineEnding(fo, lock);

        Charset encoding = FileEncodingQuery.getEncoding(fo);
        final Difference[] diffs = copyParts(true, file, f1, true, encoding);
        if (diffs.length == 0) {
            toResolve.add(file);
            return false;
        }

        copyParts(false, file, f2, false, encoding);

        String originalLeftFileRevision = leftFileRevision;
        String originalRightFileRevision = rightFileRevision;
        if (leftFileRevision != null) leftFileRevision = leftFileRevision.trim();
        if (rightFileRevision != null) rightFileRevision = rightFileRevision.trim();
        if (leftFileRevision == null || leftFileRevision.isEmpty()) {
            leftFileRevision = org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Diff.titleWorkingFile"); // NOI18N
        } else {
            leftFileRevision = formatRevision(leftFileRevision);
        }
        if (rightFileRevision == null || rightFileRevision.isEmpty()) {
            rightFileRevision = org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Diff.titleWorkingFile"); // NOI18N
        } else {
            rightFileRevision = formatRevision(rightFileRevision);
        }
        
        final StreamSource s1;
        final StreamSource s2;
        Utils.associateEncoding(file, f1);
        Utils.associateEncoding(file, f2);
        s1 = StreamSource.createSource(file.getName(), leftFileRevision, mimeType, f1);
        s2 = StreamSource.createSource(file.getName(), rightFileRevision, mimeType, f2);
        final StreamSource result = new MergeResultWriterInfo(f1, f2, f3, file, mimeType,
                                                              originalLeftFileRevision,
                                                              originalRightFileRevision,
                                                              fo, lock, encoding, newLineString);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Component c = merge.createView(diffs, s1, s2, result);
                    if (c instanceof TopComponent) {
                        ((TopComponent) c).putClientProperty(ResolveConflictsExecutor.class.getName(), Boolean.TRUE);
                    }
                } catch (IOException ioex) {
                    GitClientExceptionHandler.notifyException(ioex, true);
                }
            }
        });
        return true;
    }

    private static final int MAX_LEN = 40;
    private String formatRevision (String commit) {
    try {
            GitClient client = getClient();
            GitRevisionInfo info = client.log(commit, GitUtils.NULL_PROGRESS_MONITOR);
            StringBuilder sb = new StringBuilder(100);
            sb.append(commit);
            if (!info.getRevision().startsWith(commit)) {
                String commitId = info.getRevision();
                if (commitId.length() > 7) {
                    commitId = commitId.substring(0, 7);
                }
                sb.append(" (").append(commitId).append(")");
            }
            sb.append(" - ").append(info.getShortMessage().replace('\n', ' '));
            if (sb.length() > MAX_LEN) {
                sb.delete(MAX_LEN, sb.length());
            }
            return sb.toString();
        } catch (GitException ex) {
            LOG.log(Level.FINE, null, ex);
            return commit;
        }
    }

    /**
     * Copy the file and conflict parts into another file.
     */
    private Difference[] copyParts(boolean generateDiffs, File source,
                                   File dest, boolean leftPart, Charset charset) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(source), charset));
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest), charset));
        ArrayList<Difference> diffList = null;
        if (generateDiffs) {
            diffList = new ArrayList<Difference>();
        }
        try {
            String line;
            boolean isChangeLeft = false;
            boolean isChangeRight = false;
            boolean isChangeBase = false;
            int f1l1 = 0, f1l2 = 0, f2l1 = 0, f2l2 = 0;
            StringBuilder text1 = new StringBuilder();
            StringBuilder text2 = new StringBuilder();
            int i = 1, j = 1;
            while ((line = r.readLine()) != null) {
                // As the Graphical Merge Visualizer does not support 3 way diff,
                // remove the base diff itself.
                // Only show the diffs of the two heads against the base
                if (line.startsWith(CHANGE_BASE_DELIMETER)) {
                    isChangeBase = true;
                    continue;
                }
                if (isChangeBase && line.startsWith(CHANGE_DELIMETER)) {
                    isChangeBase = false;
                } else if (isChangeBase) {
                    continue;
                }
                if (line.startsWith(CHANGE_LEFT)) {
                    if (generateDiffs) {
                        if (leftFileRevision == null) {
                            leftFileRevision = line.substring(CHANGE_LEFT.length());
                        }
                        if (isChangeLeft) {
                            f1l2 = i - 1;
                            diffList.add((f1l1 > f1l2) ? new Difference(Difference.ADD,
                                                                        f1l1 - 1, 0, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()) :
                                         (f2l1 > f2l2) ? new Difference(Difference.DELETE,
                                                                        f1l1, f1l2, f2l1 - 1, 0,
                                                                        text1.toString(),
                                                                        text2.toString())
                                                       : new Difference(Difference.CHANGE,
                                                                        f1l1, f1l2, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()));
                            f1l1 = f1l2 = f2l1 = f2l2 = 0;
                            text1.delete(0, text1.length());
                            text2.delete(0, text2.length());
                        } else {
                            f1l1 = i;
                        }
                    }
                    isChangeLeft = !isChangeLeft;
                    continue;
                } else if (line.startsWith(CHANGE_RIGHT)) {
                    if (generateDiffs) {
                        if (rightFileRevision == null) {
                            rightFileRevision = line.substring(CHANGE_RIGHT.length());
                        }
                        if (isChangeRight) {
                            f2l2 = j - 1;
                            diffList.add((f1l1 > f1l2) ? new Difference(Difference.ADD,
                                                                        f1l1 - 1, 0, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()) :
                                         (f2l1 > f2l2) ? new Difference(Difference.DELETE,
                                                                        f1l1, f1l2, f2l1 - 1, 0,
                                                                        text1.toString(),
                                                                        text2.toString())
                                                       : new Difference(Difference.CHANGE,
                                                                        f1l1, f1l2, f2l1, f2l2,
                                                                        text1.toString(),
                                                                        text2.toString()));
                            f1l1 = f1l2 = f2l1 = f2l2 = 0;
                            text1.delete(0, text1.length());
                            text2.delete(0, text2.length());
                        } else {
                            f2l1 = j;
                        }
                    }
                    isChangeRight = !isChangeRight;
                    continue;
                } else if (isChangeRight && line.indexOf(CHANGE_RIGHT) != -1) {
                    String lineText = line.substring(0, line.lastIndexOf(CHANGE_RIGHT));
                    if (generateDiffs) {
                        if (rightFileRevision == null) {
                            rightFileRevision = line.substring(line.lastIndexOf(CHANGE_RIGHT) + CHANGE_RIGHT.length());
                        }
                        text2.append(lineText);
                        f2l2 = j;
                        diffList.add((f1l1 > f1l2) ? new Difference(Difference.ADD,
                                                                    f1l1 - 1, 0, f2l1, f2l2,
                                                                    text1.toString(),
                                                                    text2.toString()) :
                                     (f2l1 > f2l2) ? new Difference(Difference.DELETE,
                                                                    f1l1, f1l2, f2l1 - 1, 0,
                                                                    text1.toString(),
                                                                    text2.toString())
                                                   : new Difference(Difference.CHANGE,
                                                                    f1l1, f1l2, f2l1, f2l2,
                                                                    text1.toString(),
                                                                    text2.toString()));
                        f1l1 = f1l2 = f2l1 = f2l2 = 0;
                        text1.delete(0, text1.length());
                        text2.delete(0, text2.length());
                    }
                    if (!leftPart) {
                        w.write(lineText);
                        w.write(newLineString);
                    }
                    isChangeRight = !isChangeRight;
                    continue;
                } else if (line.equals(CHANGE_DELIMETER)) {
                    if (isChangeLeft) {
                        isChangeLeft = false;
                        isChangeRight = true;
                        f1l2 = i - 1;
                        f2l1 = j;
                        continue;
                    } else if (isChangeRight) {
                        isChangeRight = false;
                        isChangeLeft = true;
                        f2l2 = j - 1;
                        f1l1 = i;
                        continue;
                    }
                } else if (line.endsWith(CHANGE_DELIMETER) && !line.endsWith(CHANGE_DELIMETER + CHANGE_DELIMETER.charAt(0))) {
                    String lineText = line.substring(0, line.length() - CHANGE_DELIMETER.length()) + newLineString; // NOI18N
                    if (isChangeLeft) {
                        text1.append(lineText);
                        if (leftPart) {
                            w.write(lineText);
                            w.write(newLineString);
                        }
                        isChangeLeft = false;
                        isChangeRight = true;
                        f1l2 = i;
                        f2l1 = j;
                    } else if (isChangeRight) {
                        text2.append(lineText);
                        if (!leftPart) {
                            w.write(lineText);
                            w.write(newLineString);
                        }
                        isChangeRight = false;
                        isChangeLeft = true;
                        f2l2 = j;
                        f1l1 = i;
                    }
                    continue;
                }
                if (!isChangeLeft && !isChangeRight || leftPart == isChangeLeft) {
                    w.write(line);
                    w.write(newLineString);
                }
                if (isChangeLeft) text1.append(line).append(newLineString); // NOI18N
                if (isChangeRight) text2.append(line).append(newLineString); // NOI18N
                if (generateDiffs) {
                    if (isChangeLeft) i++;
                    else if (isChangeRight) j++;
                    else {
                        i++;
                        j++;
                    }
                }
            }
        } finally {
            try {
                r.close();
            } finally {
                w.close();
            }
        }
        if (generateDiffs) {
            return diffList.toArray(new Difference[0]);
        } else {
            return null;
        }
    }

    @Override
    public void perform () {
        MergeVisualizer merge = (MergeVisualizer) Lookup.getDefault().lookup(MergeVisualizer.class);
        if (merge == null) {
            throw new IllegalStateException("No Merge engine found."); // NOI18N
        }

        try {
            GitClient client = getClient();
            Map<File, GitStatus> statuses = client.getStatus(files, getProgressMonitor());
            for (Map.Entry<File, GitStatus> e : statuses.entrySet()) {
                if (isCanceled()) {
                    break;
                }
                GitConflictDescriptor desc = e.getValue().getConflictDescriptor();
                if (desc != null) {
                    boolean remove = desc.getType() == Type.BOTH_DELETED;
                    if (desc.getType() == Type.DELETED_BY_THEM || desc.getType() == Type.DELETED_BY_US) {
                        ResolveTreeConflictPanel panel = new ResolveTreeConflictPanel(e.getKey(), e.getValue().getRelativePath(), desc.getType());
                        final JButton resolveButton = new JButton();
                        Mnemonics.setLocalizedText(resolveButton, NbBundle.getMessage(ResolveConflictsExecutor.class, "LBL_TreeConflict_ResolveButton.title")); //NOI18N
                        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ResolveConflictsExecutor.class, "LBL_TreeConflict.title"), //NOI18N
                                true, new Object[] { resolveButton, DialogDescriptor.CANCEL_OPTION }, resolveButton, DialogDescriptor.DEFAULT_ALIGN,
                                new HelpCtx(ResolveTreeConflictPanel.class), null);
                        resolveButton.setEnabled(false);
                        ActionListener list = new ActionListener() {
                            @Override
                            public void actionPerformed (ActionEvent e) {
                                resolveButton.setEnabled(true);
                            }
                        };
                        panel.rbAdd.addActionListener(list);
                        panel.rbRemove.addActionListener(list);
                        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                        dialog.setVisible(true);
                        if (dd.getValue() == resolveButton) {
                            remove = panel.rbRemove.isSelected();
                        } else {
                            // do nothing
                            continue;
                        }
                    }
                    if (remove) {
                        e.getKey().delete();
                    }
                    if (desc.getType() != Type.BOTH_ADDED && desc.getType() != Type.BOTH_MODIFIED) {
                        // it has no contents conflict, it's a tree conflict
                        toResolve.add(e.getKey());
                        continue;
                    }
                }
                exec(merge, e.getKey());
            }
            if (!isCanceled()) {
                List<Node> nodes = new ArrayList<Node>(toResolve.size());
                for (File f : toResolve) {
                    nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(f)));
                }
                SystemAction.get(MarkResolvedAction.class).performAction(VCSContext.forNodes(nodes.toArray(new Node[0])));
            }
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        }
    }

    private static class MergeResultWriterInfo extends StreamSource {
        
        private File tempf1, tempf2, tempf3, outputFile;
        private File fileToRepairEntriesOf;
        private String mimeType;
        private String leftFileRevision;
        private String rightFileRevision;
        private FileObject fo;
        private FileLock lock;
        private Charset encoding;
        private final String newLineString;
        
        public MergeResultWriterInfo(File tempf1, File tempf2, File tempf3,
                                     File outputFile, String mimeType,
                                     String leftFileRevision, String rightFileRevision,
                                     FileObject fo, FileLock lock, Charset encoding,
                                     String newLineString) {
            this.tempf1 = tempf1;
            this.tempf2 = tempf2;
            this.tempf3 = tempf3;
            this.outputFile = outputFile;
            this.mimeType = mimeType;
            this.leftFileRevision = leftFileRevision;
            this.rightFileRevision = rightFileRevision;
            this.fo = fo;
            this.lock = lock;
            this.newLineString = newLineString;
            if (encoding == null) {
                encoding = FileEncodingQuery.getEncoding(FileUtil.toFileObject(tempf1));
            }
            this.encoding = encoding;
        }
        
        @Override
        public String getName() {
            return outputFile.getName();
        }
        
        @Override
        public String getTitle() {
            return org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Merge.titleResult"); // NOI18N
        }
        
        @Override
        public String getMIMEType() {
            return mimeType;
        }
        
        @Override
        public Reader createReader() throws IOException {
            throw new IOException("No reader of merge result"); // NOI18N
        }
        
        /**
         * Create a writer, that writes to the source.
         * @param conflicts The list of conflicts remaining in the source.
         *                  Can be <code>null</code> if there are no conflicts.
         * @return The writer or <code>null</code>, when no writer can be created.
         */
        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            Writer w;
            if (fo != null) {
                w = new LineEndingFilterWriter(new OutputStreamWriter(fo.getOutputStream(lock), encoding), newLineString);
            } else {
                w = new LineEndingFilterWriter(new OutputStreamWriter(new FileOutputStream(outputFile), encoding), newLineString);
            }
            if (conflicts == null || conflicts.length == 0) {
                fileToRepairEntriesOf = outputFile;
                return w;
            } else {
                return new MergeConflictFileWriter(w, fo, conflicts,
                                                   leftFileRevision, rightFileRevision, newLineString);
            }
        }
        
        /**
         * This method is called when the visual merging process is finished.
         * All possible writting processes are finished before this method is called.
         */
        @Override
        public void close() {
            tempf1.delete();
            tempf2.delete();
            tempf3.delete();
            if (lock != null) {
                lock.releaseLock();
                lock = null;
            }
            fo = null;
            if (fileToRepairEntriesOf != null) {
                repairEntries(fileToRepairEntriesOf);
                fileToRepairEntriesOf = null;
            }
        }

        private void repairEntries (File file) {
            SystemAction.get(MarkResolvedAction.class).performAction(GitUtils.getContextForFile(file));
        }
    }
    
    private static class MergeConflictFileWriter extends FilterWriter {
        
        private Difference[] conflicts;
        private int lineNumber;
        private int currentConflict;
        private String leftName;
        private String rightName;
        private FileObject fo;
        private final String newLineString;
        
        public MergeConflictFileWriter(Writer delegate, FileObject fo,
                                       Difference[] conflicts, String leftName,
                                       String rightName, String newLineString) throws IOException {
            super(delegate);
            this.conflicts = conflicts;
            this.leftName = leftName;
            this.rightName = rightName;
            this.newLineString = newLineString;
            this.lineNumber = 1;
            this.currentConflict = 0;
            if (lineNumber == conflicts[currentConflict].getFirstStart()) {
                writeConflict(conflicts[currentConflict]);
                currentConflict++;
            }
            this.fo = fo;
        }
        
        @Override
        public void write(String str) throws IOException {
            if (!SYSTEM_LINE_SEPARATOR.equals(newLineString)) {
                str = str.replaceAll(SYSTEM_LINE_SEPARATOR, newLineString);
            }
            super.write(str);
            lineNumber += numChars(newLineString, str);
            if (currentConflict < conflicts.length && lineNumber >= conflicts[currentConflict].getFirstStart()) {
                writeConflict(conflicts[currentConflict]);
                currentConflict++;
            }
        }
        
        private void writeConflict(Difference conflict) throws IOException {
            super.write(CHANGE_LEFT + leftName + newLineString); // NOI18N
            super.write(conflict.getFirstText());
            super.write(CHANGE_DELIMETER + newLineString); // NOI18N
            super.write(conflict.getSecondText());
            super.write(CHANGE_RIGHT + rightName + newLineString); // NOI18N
        }
        
        private static int numChars(String s, String str) {
            int n = 0;
            for (int pos = str.indexOf(s); pos >= 0 && pos < str.length(); pos = str.indexOf(s, pos + 1)) {
                n++;
            }
            return n;
        }
        
        public void close() throws IOException {
            super.close();
            if (fo != null) fo.refresh(true);
        }
    }

    private static class LineEndingFilterWriter extends FilterWriter {
        private final String lineEnding;

        public LineEndingFilterWriter (OutputStreamWriter outputStreamWriter, String lineEnding) {
            super(outputStreamWriter);
            this.lineEnding = lineEnding;
        }
        
        @Override
        public void write(String str) throws IOException {
            if (!SYSTEM_LINE_SEPARATOR.equals(lineEnding)) {
                str = str.replaceAll(SYSTEM_LINE_SEPARATOR, lineEnding);
            }
            super.write(str);
        }
    }
}

