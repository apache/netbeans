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
package org.netbeans.modules.python.editor.codecoverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * Code coverage for Python
 *
 * @todo Uhm... It looks like the hit count is ALWAYS 1 - so store and parse in a more
 *   compressed format!
 *
 */
public final class PythonCoverageProvider implements CoverageProvider {
    private static final int COUNT_INFERRED = -1;
    private static final int COUNT_NOT_COVERED = -2;
    private static final int COUNT_UNKNOWN = -3;
    private Map<String, String> hitCounts;
    private Map<String, String> fullNames;
    private long timestamp;
    private Project project;
    private Set<String> mimeTypes = Collections.singleton(PythonMIMEResolver.PYTHON_MIME_TYPE);
    private Boolean enabled;
    private Boolean aggregating;

    public PythonCoverageProvider(Project project) {
        this.project = project;
    }

    public static PythonCoverageProvider get(Project project) {
        return project.getLookup().lookup(PythonCoverageProvider.class);
    }

    private FileCoverageSummary createSummary(String fileName, List<Integer> linenos) {
        FileObject file;
        File f = new File(fileName);
        if (f.exists()) {
            file = FileUtil.toFileObject(f);
        } else {
            file = project.getProjectDirectory().getFileObject(fileName.replace('\\', '/'));
        }
        if (file != null) {
            Project p = FileOwnerQuery.getOwner(file);
            if (p != project) {
                return null;
            }
        }

        // Compute coverage:
        int lineCount = 0;
        int executed = 0;
        //int notExecuted = 0;
        //int inferred = 0;
        for (Integer lineno : linenos) {
            int line = lineno;
            if (line > lineCount) {
                lineCount = lineno;
            }
            // The lines explicitly listed are executed
            executed++;
        }

        // Attempt to make a more accurate percentage by using file details
        int inferredCount = 0;
        int partialCount = 0;
        if (file != null) {
            BaseDocument doc = GsfUtilities.getDocument(file, true);
            if (doc != null) {
                FileCoverageDetails details = getDetails(file, doc);
                if (details != null) {
                    lineCount = details.getLineCount();
                    int notExecuted = 0;
                    for (int line = 0; line < lineCount; line++) {
                        CoverageType type = details.getType(line);
                        if (type == CoverageType.NOT_COVERED) {
                            notExecuted++;
                        } else if (type == CoverageType.INFERRED) {
                            inferredCount++;
                        } else if (type == CoverageType.PARTIAL) {
                            partialCount++;
                        }
                    }
                    executed = lineCount-notExecuted;
                }
            }
        }

        if (file != null && FileUtil.isParentOf(project.getProjectDirectory(), file)) {
            fileName = FileUtil.getRelativePath(project.getProjectDirectory(), file);
        }

        FileCoverageSummary result = new FileCoverageSummary(file, fileName, lineCount, executed, inferredCount, partialCount);

        return result;
    }

    @Override
    public synchronized List<FileCoverageSummary> getResults() {
        List<FileCoverageSummary> results = new ArrayList<>();

        update();

        if (hitCounts == null) {
            return null;
        }

        for (Map.Entry<String, String> entry : hitCounts.entrySet()) {
            String fileName = entry.getKey();
            List<Integer> linenos = getLineCounts(entry.getValue());
            FileCoverageSummary summary = createSummary(fileName, linenos);
            if (summary != null) {
                results.add(summary);
            }
        }

        return results;
    }

    @Override
    public boolean supportsAggregation() {
        return true;
    }

    @Override
    public synchronized boolean isAggregating() {
        if (aggregating == null) {
            aggregating = CoverageProviderHelper.isAggregating(project);
        }
        return aggregating;
    }

    @Override
    public synchronized void setAggregating(boolean on) {
        if (aggregating != null && on == isAggregating()) {
            return;
        }

        aggregating = on;

        CoverageProviderHelper.setAggregating(project, on);
    }

    @Override
    public synchronized boolean isEnabled() {
        if (enabled == null) {
            enabled = CoverageProviderHelper.isEnabled(project);
        }
        return enabled;
    }

    @Override
    public synchronized void setEnabled(boolean on) {
        if (enabled != null && on == isEnabled()) {
            return;
        }

        enabled = on;
        timestamp = 0;

        if (!on) {
            hitCounts = null;
            fullNames = null;
        }

        CoverageProviderHelper.setEnabled(project, on);
    }

    @Override
    public synchronized void clear() {
        File file = getPythonCoverageFile();
        if (file.exists()) {
            file.delete();
        }

        file = getNbCoverageFile();
        if (file.exists()) {
            file.delete();
        }

        hitCounts = null;
        fullNames = null;
        timestamp = 0;
    }

    @Override
    public synchronized FileCoverageDetails getDetails(FileObject fo, Document doc) {
        update();

        if (hitCounts == null) {
            return null;
        }
        String path = FileUtil.toFile(fo).getPath();
        if (path == null) {
            return null;
        }

        String lines = hitCounts.get(path);
        if (lines == null) {
            // Happens on some case insensitive file systems
            lines = hitCounts.get(path.toLowerCase());
        }
        if (lines == null) {
            String name = fo.getNameExt();
            String fullName = fullNames.get(name.toLowerCase());
            if (fullName != null && !fullName.equalsIgnoreCase(path)) {
                lines = hitCounts.get(fullName);
            }
        }

        if (lines != null) {
            List<Integer> linenos = getLineCounts(lines);
            int max = 0;
            for (Integer lineno : linenos) {
                if (lineno > max) {
                    max = lineno;
                }
            }

            int[] result = new int[max];
            for (int i = 0; i < max; i++) {
                result[i] = COUNT_UNKNOWN;
            }
            for (Integer lineno : linenos) {
                result[lineno - 1] = 1;
            }

            result = inferCounts(result, doc);

            return new PythonFileCoverageDetails(fo, result, path, linenos, timestamp);
        }

        return null;
    }

    private File getNbCoverageDir() {
        return new File(FileUtil.toFile(project.getProjectDirectory().getFileObject("nbproject")), "private" + File.separator + "coverage"); // NOI18N
    }

    private File getNbCoverageFile() {
        return new File(getNbCoverageDir(), ".nbcoverage"); // NOI18N
    }

    private File getPythonCoverageFile() {
        return new File(getNbCoverageDir(), ".coverage"); // NOI18N
    }

    private static List<Integer> getLineCounts(String lines) {
        int size = lines.length() / 6;
        List<Integer> lineCounts = new ArrayList<>(size);

        int start = 1;
        int i = start;
        int length = lines.length();
        while (i < length) {
            char c = lines.charAt(i);
            if (c == ',' || c == ']') {
                Integer line = Integer.valueOf(lines.substring(start, i));
                lineCounts.add(line);
                start = i + 1;
            } else if (c == ' ') {
                start = i + 1;
            }
            i++;
        }

        return lineCounts;
    }

    private boolean isExecutableToken(TokenId id) {
        return id != PythonTokenId.WHITESPACE && id != PythonTokenId.NEWLINE && id != PythonTokenId.COMMENT;
    }

    /**
     * Add inferred counts - look at execution lines and compare to document
     * contents to conclude that for example comments between two executed
     * lines should be inferred as executed
     */
    private int[] inferCounts(int[] result, Document document) {
        BaseDocument doc = (BaseDocument) document;
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, 0);
        if (ts == null) {
            return result;
        }

        int knownRange = result.length;

        // Make a larger line count array which includes unknown data for the
        // tail end of the file
        try {
            int lineCount = Utilities.getLineOffset(doc, doc.getLength());
            if (lineCount > result.length) {
                int[] r = new int[lineCount];
                System.arraycopy(result, 0, r, 0, result.length);
                for (int i = result.length; i < r.length; i++) {
                    r[i] = COUNT_UNKNOWN;
                }
                result = r;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        boolean[] continued = new boolean[result.length];
        PythonTokenId[] lineFirstTokens = new PythonTokenId[result.length];
        computeLineDetails(result, document, lineFirstTokens, continued);

        // coverage.py only records a hit on the LAST line of a multiline statement.
        // For example, you may have
        //    outstr = (outstr
        //              + string.hexdigits[(o >> 4) & 0xF]
        //              + string.hexdigits[o & 0xF])
        // ...and only the LAST line here is marked executable. Go and fix up that
        for (int lineno = result.length - 1; lineno >= 0; lineno--) {
            if (result[lineno] >= 0 && continued[lineno]) {
                for (lineno--; lineno >= 0; lineno--) {
                    if (result[lineno] == COUNT_UNKNOWN) {
                        result[lineno] = COUNT_INFERRED;
                        if (!continued[lineno]) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        // (1) If I have comment lines immediately before an executed line,
        //   mark those as executed as well.
        for (int lineno = 0; lineno < result.length; lineno++) {
            TokenId id = lineFirstTokens[lineno];
            if (id == PythonTokenId.DEF || id == PythonTokenId.CLASS) {
                for (int prev = lineno - 1; prev >= 0; prev--) {
                    if (lineFirstTokens[prev] == PythonTokenId.COMMENT) {
                        if (result[prev] == COUNT_UNKNOWN) {
                            result[prev] = COUNT_INFERRED;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        // (2) If I can find non-executable lines between executed lines
        //   mark all those as inferred.
        // ... unless that next executed line is a "class" or "def",
        // since these lines are probably the dividers between unrelated
        // functions.
        // (3) If I can find executable lines that are NOT continued from
        //  an executed line, then mark all such lines as a block until
        //  I get to an executed line.
        for (int lineno = 0; lineno < result.length; lineno++) {
            int count = result[lineno];
            if (count == COUNT_UNKNOWN) {
                PythonTokenId id = lineFirstTokens[lineno];
                if (isExecutableToken(id)) {

                    // (4) If I have String literals immediately before executed code, those are executable
                    // as well (docstrings)
                    if (id == PythonTokenId.STRING_BEGIN || id == PythonTokenId.STRING_LITERAL) {
                        // Peek ahead
                        boolean beforeExecutable = false;
                        int j = lineno+1;
                        for (; j < result.length; j++) {
                            PythonTokenId lft = lineFirstTokens[j];
                            if (lft != PythonTokenId.STRING_LITERAL && lft != PythonTokenId.STRING_END && isExecutableToken(lft)) {
                                if (result[j] >= 0) {
                                    beforeExecutable = true;
                                }
                                break;
                            }
                        }
                        if (beforeExecutable) {
                            for (; lineno < j; lineno++) {
                                result[lineno] = COUNT_INFERRED;
                            }
                            continue;
                        }
                    }


                    // There's code here.
                    // If this line is not continued, mark it, and all lines
                    // up to the next known or inferred line, as not covered
                    if (!continued[lineno]) {
                        for (; lineno < result.length; lineno++) {
                            if (result[lineno] == COUNT_UNKNOWN) {
                                result[lineno] = COUNT_NOT_COVERED;
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    // Look ahead to the next known count, and iff it is
                    // executable (and not a def/class) mark all these lines
                    // as inferred.
                    boolean markInferred = true;
                    int i = lineno + 1;
                    for (; i < result.length; i++) {
                        int nextCount = result[i];
                        if (nextCount >= 0 || nextCount == COUNT_INFERRED) {
                            if (lineFirstTokens[i] == PythonTokenId.DEF || lineFirstTokens[i] == PythonTokenId.CLASS) {
                                markInferred = false;
                            }
                            break;
                        } else if (nextCount == COUNT_NOT_COVERED) {
                            markInferred = false;
                            break;
                        }
                    }
                    if (markInferred) {
                        for (int line = lineno; line < i; line++) {
                            result[line] = COUNT_INFERRED;
                        }
                        lineno = i;
                        continue;
                    }
                }
            }
        }
        
        // Mark the end of the file (after known coverage data) based on what we see earlier.
        // If it contains only whitespace or comments, then it's either inferred executed
        // or not executed  based on the status of the last line, until the first executable
        // line, and at that point it's all not executed from then on.
        if (knownRange > 0) {
            int last = result[knownRange-1];
            boolean foundExecutable = false;
            for (int lineno = knownRange; lineno < result.length; lineno++) {
                if (foundExecutable) {
                    result[lineno] = COUNT_NOT_COVERED;
                } else {
                    int count = result[lineno];
                    if (count == COUNT_UNKNOWN) {
                        if (isExecutableToken(lineFirstTokens[lineno])) {
                            foundExecutable = true;
                            result[lineno] = COUNT_NOT_COVERED;
                        } else {
                            if (last == COUNT_INFERRED || last >= 0) {
                                result[lineno] = COUNT_INFERRED;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private void computeLineDetails(int[] result, Document document, TokenId[] lineFirstTokens, boolean[] continued) {
        BaseDocument doc = (BaseDocument) document;
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, 0);
        if (ts == null) {
            return;
        }

        try {
            // Look for gaps in the lines and see if lines in between are executable
            int balance = 0;
            int currentOffset = 0;
            boolean prevWasContinue = false;
            for (int lineno = 0; lineno < result.length; lineno++) {
                // Update the line balance
                if (currentOffset > doc.getLength()) {
                    break;
                }
                int begin = Utilities.getRowStart(doc, currentOffset);
                int end = Utilities.getRowEnd(doc, currentOffset);
                int nonWhiteOffset = Utilities.getRowFirstNonWhite(doc, begin);
                if (nonWhiteOffset != -1) {
                    begin = nonWhiteOffset;
                }

                ts.move(begin);

                if (ts.moveNext()) {
                    Token<? extends PythonTokenId> token = ts.token();
                    TokenId id = token.id();
                    lineFirstTokens[lineno] = id;
                    continued[lineno] = prevWasContinue || balance > 0;

                    do {
                        token = ts.token();
                        id = token.id();

                        if (id == PythonTokenId.LPAREN || id == PythonTokenId.LBRACE || id == PythonTokenId.LBRACKET) {
                            balance++;
                        } else if (id == PythonTokenId.RPAREN || id == PythonTokenId.RBRACE || id == PythonTokenId.RBRACKET) {
                            balance--;
                        } else if (id == PythonTokenId.NONUNARY_OP || id == PythonTokenId.ESC) {
                            prevWasContinue = true;
                        } else if (id != PythonTokenId.WHITESPACE && id != PythonTokenId.NEWLINE && id != PythonTokenId.COMMENT) {
                            prevWasContinue = false;
                        }
                    } while (ts.moveNext() && (ts.offset() <= end));
                } else {
                    lineFirstTokens[lineno] = PythonTokenId.WHITESPACE;
                    if (lineno > 0) {
                        continued[lineno] = continued[lineno - 1];
                    } else {
                        continued[lineno] = false;
                    }
                }

                currentOffset = Utilities.getRowEnd(doc, currentOffset) + 1;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private synchronized void update() {
        File pythonCoverage = getPythonCoverageFile();
        if (!pythonCoverage.exists()) {
            // No recorded data! Done.
            return;
        }

        File nbCoverage = getNbCoverageFile();

        // Read & Parse the corresponding data structure into memory
        if (nbCoverage.exists() && timestamp < nbCoverage.lastModified()) {
            timestamp = nbCoverage.lastModified();
            hitCounts = new HashMap<>();
            fullNames = new HashMap<>();

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(nbCoverage));
                while (true) {
                    try {
                        String file = br.readLine();
                        String lines = br.readLine();

                        if (file == null || lines == null) {
                            break;
                        }

                        int last = Math.max(file.lastIndexOf('\\'), file.lastIndexOf('/'));
                        String base = file;
                        if (last != COUNT_INFERRED) {
                            base = file.substring(last + 1);
                        }

                        fullNames.put(base.toLowerCase(), file);

                        assert lines.startsWith("[");
                        assert lines.endsWith("]");

                        hitCounts.put(file, lines);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    public PythonExecution wrapWithCoverage(final PythonExecution original) {
        InstalledFileLocator locator = InstalledFileLocator.getDefault();
        // Set COVERAGE_FILE to ${getPythonCoverageFile()}
        // Run with "-x"
        File coverageScript = locator.locate("coverage/coverage.py", "org-netbeans-modules-python-codecoverage.jar", false);
        assert coverageScript != null;

        File wrapper = locator.locate("coverage/coverage_wrapper.py", "org-netbeans-modules-python-codecoverage.jar", false);
        assert wrapper != null;

        PythonExecution execution = new PythonExecution(original);

        List<String> wrapperArgs = new ArrayList<>();
        // TODO - path munging on Windows?
        File pythonCoverage = getPythonCoverageFile();
        File nbCoverage = getNbCoverageFile();
        File dir = getNbCoverageDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        wrapperArgs.add(pythonCoverage.getPath());
        wrapperArgs.add(nbCoverage.getPath());
        wrapperArgs.add(coverageScript.getPath());

        if (!CoverageManager.INSTANCE.isAggregating(project)) {
            wrapperArgs.add("-e"); // NOI18N
        }
        wrapperArgs.add("-x"); // NOI18N

        execution.setWrapperCommand(wrapper.getPath(),
                wrapperArgs.toArray(new String[wrapperArgs.size()]),
                new String[]{"COVERAGE_FILE=" + pythonCoverage.getPath()}); // NOI18N
        execution.addOutConvertor(new HideCoverageFramesConvertor());
        execution.addErrConvertor(new HideCoverageFramesConvertor());

        execution.setPostExecutionHook(new Runnable() {
            @Override
            public void run() {
                // Process the data immediately since it's available when we need it...
                PythonCoverageProvider.this.update();
                CoverageManager.INSTANCE.resultsUpdated(project, PythonCoverageProvider.this);

                if (original.getPostExecutionHook() != null) {
                    original.getPostExecutionHook().run();
                }
            }
        });

        return execution;
    }

    @Override
    public String getTestAllAction() {
        return null;
    }

    // Remove stacktrace lines that refer to frames in the wrapper scripts
    private static class HideCoverageFramesConvertor implements LineConvertor {
        boolean lastWasCulled = false;

        @Override
        public List<ConvertedLine> convert(String line) {
            // What about Windows? Do \\ instead?
            if (line.contains("/python/coverage/coverage")) { // NOI18N
                lastWasCulled = true;
                return Collections.emptyList();
            } else if (lastWasCulled) {
                // Filter out the first line AFTER a code coverage line as well - these are the method names
                lastWasCulled = false;
                return Collections.emptyList();
            }

            return null;
        }
    }

    public synchronized void notifyProjectOpened() {
        CoverageManager.INSTANCE.setEnabled(project, true);
    }

    @Override
    public boolean supportsHitCounts() {
        return false;
    }

    @Override
    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    private class PythonFileCoverageDetails implements FileCoverageDetails {
        private int[] hitCounts;
        private final String fileName;
        private final List<Integer> lineCounts;
        private final long lastUpdated;
        private final FileObject fileObject;

        public PythonFileCoverageDetails(FileObject fileObject, int[] hitCounts, String fileName, List<Integer> lineCounts, long lastUpdated) {
            this.fileObject = fileObject;
            this.hitCounts = hitCounts;
            this.fileName = fileName;
            this.lineCounts = lineCounts;
            this.lastUpdated = lastUpdated;
        }

        @Override
        public int getLineCount() {
            return hitCounts.length;
        }

        @Override
        public boolean hasHitCounts() {
            return false;
        }

        @Override
        public FileCoverageSummary getSummary() {
            return createSummary(fileName, lineCounts);
        }

        @Override
        public CoverageType getType(int lineNo) {
            int count = hitCounts[lineNo];
            switch (count) {
                case COUNT_UNKNOWN:
                    return CoverageType.UNKNOWN;
                case COUNT_NOT_COVERED:
                    return CoverageType.NOT_COVERED;
                case COUNT_INFERRED:
                    return CoverageType.INFERRED;
                default:
                    return CoverageType.COVERED;
            }
        }

        @Override
        public int getHitCount(int lineNo) {
            return hitCounts[lineNo];
        }

        @Override
        public long lastUpdated() {
            return lastUpdated;
        }

        @Override
        public FileObject getFile() {
            return fileObject;
        }
    }
}
