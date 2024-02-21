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

package org.netbeans.modules.mercurial.ui.update;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.logging.Level;
import javax.swing.*;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.spi.diff.*;

import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.filesystems.*;

import org.netbeans.api.diff.*;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Shows basic conflict resolver UI.
 *
 * This class is copy&pasted from javacvs
 *
 * @author  Martin Entlicher
 */
public class ResolveConflictsExecutor extends HgProgressSupport {
    
    private static final String TMP_PREFIX = "merge"; // NOI18N
    private static final String ORIG_SUFFIX = ".orig."; // NOI18N
    private static final String LOCAL = "local";                        //NOI18N
    
    static final String CHANGE_LEFT = "<<<<<<< "; // NOI18N
    static final String CHANGE_RIGHT = ">>>>>>> "; // NOI18N
    static final String CHANGE_DELIMETER = "======="; // NOI18N
    static final String CHANGE_BASE_DELIMETER = "|||||||"; // NOI18N
    
    private String leftFileRevision = null;
    private String rightFileRevision = null;

    private final File file;
    private String newLineString;
    private static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");

    public ResolveConflictsExecutor(File file) {
        super();
        this.file = file;
    }

    public void exec() {
        MergeVisualizer merge = (MergeVisualizer) Lookup.getDefault().lookup(MergeVisualizer.class);
        if (merge == null) {
            throw new IllegalStateException("No Merge engine found."); // NOI18N
        }
        
        try {
            FileObject fo = FileUtil.toFileObject(file);
            if(fo == null) {
                Mercurial.LOG.warning("can't resolve conflicts for null fileobject : " + file + ", exists: " + file.exists());
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
            SwingUtilities.invokeLater(new Runnable() {
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
        } catch (IOException ioex) {
            org.openide.ErrorManager.getDefault().notify(ioex);
        }
    }
    
    private boolean handleMergeFor(final File file, FileObject fo, FileLock lock,
                                final MergeVisualizer merge) throws IOException {
        String mimeType = (fo == null) ? "text/plain" : fo.getMIMEType(); // NOI18N
        String ext = (fo == null) ? "" : "." + fo.getExt();             //NOI18N
        File f1 = FileUtil.normalizeFile(Files.createTempFile(TMP_PREFIX, ext).toFile());
        File f2 = FileUtil.normalizeFile(Files.createTempFile(TMP_PREFIX, ext).toFile());
        File f3 = FileUtil.normalizeFile(Files.createTempFile(TMP_PREFIX, ext).toFile());
        f1.deleteOnExit();
        f2.deleteOnExit();
        f3.deleteOnExit();
        
        newLineString = Utils.getLineEnding(fo, lock);

        Charset encoding = FileEncodingQuery.getEncoding(fo);
        final Difference[] diffs = copyParts(true, file, f1, true, encoding);
        if (diffs.length == 0) {
            ConflictResolvedAction.resolved(file);  // remove conflict status
            return false;
        }

        copyParts(false, file, f2, false, encoding);
        //GraphicalMergeVisualizer merge = new GraphicalMergeVisualizer();
        String originalLeftFileRevision = leftFileRevision;
        String originalRightFileRevision = rightFileRevision;
        if (leftFileRevision != null) leftFileRevision = leftFileRevision.trim();
        if (rightFileRevision != null) rightFileRevision = rightFileRevision.trim();
        List<HgLogMessage> parentRevisions = null;
        if (leftFileRevision.equals(LOCAL)) {
            try {
                parentRevisions = HgCommand.getParents(Mercurial.getInstance().getRepositoryRoot(file), file, null);
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
            }
            if (parentRevisions != null && parentRevisions.size() > 1) {
                leftFileRevision = formatRevision(parentRevisions.get(0));
                rightFileRevision = formatRevision(parentRevisions.get(1));
            }
        }
        if (leftFileRevision == null || leftFileRevision.equals(file.getAbsolutePath() + ORIG_SUFFIX)
                || leftFileRevision.equals(LOCAL)){
            leftFileRevision = org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Diff.titleWorkingFile"); // NOI18N
        }
        if (rightFileRevision == null || rightFileRevision.equals(file.getAbsolutePath() + ORIG_SUFFIX)) {
            rightFileRevision = org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Diff.titleWorkingFile"); // NOI18N
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
            public void run() {
                try {
                    Component c = merge.createView(diffs, s1, s2, result);
                    if (c instanceof TopComponent) {
                        ((TopComponent) c).putClientProperty(ResolveConflictsExecutor.class.getName(), Boolean.TRUE);
                    }
                } catch (IOException ioex) {
                    org.openide.ErrorManager.getDefault().notify(ioex);
                }
            }
        });
        return true;
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
            StringBuffer text1 = new StringBuffer();
            StringBuffer text2 = new StringBuffer();
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
                                                       /*
                            diffList.add(new Difference((f1l1 > f1l2) ? Difference.ADD :
                                                        (f2l1 > f2l2) ? Difference.DELETE :
                                                                        Difference.CHANGE,
                                                        f1l1, f1l2, f2l1, f2l2));
                                                        */
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
                if (isChangeLeft) text1.append(line + newLineString); // NOI18N
                if (isChangeRight) text2.append(line + newLineString); // NOI18N
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

    public void perform() {
        exec();
    }

    public void run() {
        throw new RuntimeException("Not implemented"); // NOI18N
    }

    private static final int MAX_LEN = 40;
    private static String formatRevision (HgLogMessage revision) {
        StringBuilder sb = new StringBuilder(100);
        String branch = revision.getBranches().length == 0 ? HgBranch.DEFAULT_NAME : revision.getBranches()[0];
        sb.append(revision.getRevisionNumber()).append(" (").append(branch).append(") ");
        sb.append(revision.getShortMessage());
        if (sb.length() > MAX_LEN) {
            sb.delete(MAX_LEN, sb.length());
        }
        return sb.toString();
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
        
        public String getName() {
            return outputFile.getName();
        }
        
        public String getTitle() {
            return org.openide.util.NbBundle.getMessage(ResolveConflictsExecutor.class, "Merge.titleResult"); // NOI18N
        }
        
        public String getMIMEType() {
            return mimeType;
        }
        
        public Reader createReader() throws IOException {
            throw new IOException("No reader of merge result"); // NOI18N
        }
        
        /**
         * Create a writer, that writes to the source.
         * @param conflicts The list of conflicts remaining in the source.
         *                  Can be <code>null</code> if there are no conflicts.
         * @return The writer or <code>null</code>, when no writer can be created.
         */
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

        private void repairEntries(File file) {
            ConflictResolvedAction.resolved(file);  // remove conflict status
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

