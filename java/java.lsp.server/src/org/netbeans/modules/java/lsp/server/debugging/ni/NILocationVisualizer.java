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
package org.netbeans.modules.java.lsp.server.debugging.ni;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.lsp.server.files.OpenedDocuments;
import org.netbeans.modules.java.lsp.server.progress.OperationContext;
import org.netbeans.modules.java.lsp.server.protocol.DecorationRenderOptions;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.SetTextEditorDecorationParams;
import org.netbeans.modules.nativeimage.api.Location;
import org.netbeans.modules.nativeimage.api.SourceInfo;
import org.netbeans.modules.nativeimage.api.Symbol;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Entlicher
 */
public final class NILocationVisualizer implements Consumer<String> {

    private final File niFileSources;
    private final NIDebugger niDebugger;
    private final NbCodeLanguageClient client;
    private final OpenedDocuments openedDocuments;
    private Set<String> decorationKeys = new HashSet<>(); 

    private NILocationVisualizer(File nativeImageFile, NIDebugger niDebugger, CompletableFuture<Void> finished, OpenedDocuments openedDocuments) {
        this.niFileSources = getNativeSources(nativeImageFile);
        this.niDebugger = niDebugger;
        OperationContext ctx = OperationContext.find(Lookup.getDefault());
        this.client = ctx.getClient();
        this.openedDocuments = openedDocuments;
        finished.thenRun(() -> {
            openedDocuments.removeOpenedConsumer(this);
            Set<String> keys;
            synchronized (this) {
                keys = decorationKeys;
                decorationKeys = null;
            }
            for (String key : keys) {
                client.disposeTextEditorDecoration(key);
            }
        });
    }

    private static File getNativeSources(File niFile) {
        File sources = new File(niFile.getParentFile(), "sources");
        if (sources.isDirectory()) {
            return sources;
        } else {
            return null;
        }
    }

    public static void handle(File nativeImageFile, NIDebugger niDebugger, CompletableFuture<Void> finished, OpenedDocuments openedDocuments) {
        openedDocuments.addOpenedConsumer(new NILocationVisualizer(nativeImageFile, niDebugger, finished, openedDocuments));
    }

    @Override
    public void accept(final String uri) {
        List<Location> locations = getLocations(uri);
        if (locations != null) {
            DecorationRenderOptions decorationOptions = new DecorationRenderOptions();
            decorationOptions.setColor(Either.forLeft("gray"));
            CompletableFuture<String> decorationFuture = client.createTextEditorDecoration(decorationOptions);
            decorationFuture.thenAccept(key -> {
                Intervals intervals = getCodeIntervals(uri);
                Range[] ranges = locationsToRanges(locations, intervals);
                client.setTextEditorDecoration(new SetTextEditorDecorationParams(key, uri, ranges));
                boolean disposed;
                synchronized (this) {
                    disposed = decorationKeys == null; // Disposed in the mean time
                    if (!disposed) {
                        decorationKeys.add(key);
                    }
                }
                if (disposed) {
                    client.disposeTextEditorDecoration(key);
                }
            });
        }
    }

    private final List<Location> getLocations(String uri) {
        List<Location> locations = niDebugger.listLocations(uri);
        if (locations == null && niFileSources != null) {
            String relPath = getRelativePath(uri);
            if (relPath != null) {
                File sourcesFile = new File(niFileSources, relPath);
                String filePath = sourcesFile.getAbsolutePath();
                locations = niDebugger.listLocations(filePath);
            }
        }
        return locations;
    }

    /**
     * The ranges are sub-intervals of <code>intervals</code> that do not contain <code>locations</code>.
     */
    private Range[] locationsToRanges(List<Location> locations, Intervals intervals) {
        locations.sort((l1, l2) -> l1.getLine() - l2.getLine());
        List<Range> ranges = new ArrayList<>();
        int lastLine = intervals.getFirst();
        int maxLine = intervals.getLast();
        for (Location l : locations) {
            int line = l.getLine();
            if (line == 0) {  // Unknown line location
                continue;
            }
            if (lastLine < line) {
                int start = lastLine;
                int end = line - 1;
                do {
                    while (!intervals.contains(start) && start < maxLine) {
                        start++;
                    }
                    if (start > end) {
                        break;
                    }
                    int rangeEnd = start;
                    while (rangeEnd < end && intervals.contains(rangeEnd)) {
                        rangeEnd++;
                    }
                    int startCol;
                    int endCol;
                    int[] extendedRange = intervals.extendRange(start, rangeEnd);
                    if (extendedRange != null) {
                        start = extendedRange[0];
                        startCol = extendedRange[1];
                        rangeEnd = extendedRange[2];
                        endCol = extendedRange[3];
                    } else {
                        startCol = intervals.getFirstColumn(start);
                        endCol = intervals.getLastColumn(rangeEnd);
                    }
                    int endLine = rangeEnd;
                    if (endCol == -1) { // end is the end of line
                        endLine++;
                        endCol = 1;
                    }
                    ranges.add(new Range(new Position(start-1, startCol-1), new Position(endLine-1, endCol-1))); // Position is 0-based
                    start = rangeEnd + 1;
                } while (start <= end);
            }
            lastLine = line + 1;
        }
        if (lastLine < maxLine) {
            ranges.add(new Range(new Position(lastLine, 0), new Position(maxLine, 0)));
        }
        for (String variable : intervals.variables.keySet()) {
            Map<SourceInfo, List<Symbol>> listVariables = niDebugger.listVariables(variable, true, -1);
            if (listVariables != null && listVariables.isEmpty()) {
                Interval interval = intervals.variables.get(variable);
                ranges.add(new Range(new Position(interval.l1-1, interval.c1-1), new Position(interval.l2-1, interval.c2-1))); // Position is 0-based
            }
        }
        return ranges.toArray(new Range[0]);
    }

    private static String r2s(List<Range> ranges) {
        StringBuilder sb = new StringBuilder("[");
        for (Range r : ranges) {
            sb.append(r.getStart().getLine() + " - " + r.getEnd().getLine());
            sb.append(", ");
        }
        if (sb.length() > 3) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(']');
        return sb.toString();
    }

    private static String getRelativePath(String url) {
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            return null;
        }
        if (fo == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath (fo, ClassPath.SOURCE);
        if (cp == null) {
            cp = ClassPath.getClassPath (fo, ClassPath.COMPILE);
        }
        if (cp == null) {
            return null;
        }
        return cp.getResourceName (fo, '/', true);
    }

    private static Intervals getCodeIntervals(String url) {
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            return null;
        }
        JavaSource source = JavaSource.forFileObject(fo);
        Intervals intervals = new Intervals();
        try {
            source.runWhenScanFinished(new Task<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    List<? extends TypeElement> topLevelElements = cc.getTopLevelElements();
                    TreeUtilities treeUtilities = cc.getTreeUtilities();
                    SourcePositions sourcePositions = cc.getTrees().getSourcePositions();
                    LineMap lineMap = cc.getCompilationUnit().getLineMap();
                    for (Element element : topLevelElements) {
                        Tree tree = cc.getTrees().getTree(element);
                        if (tree.getKind() ==  Tree.Kind.CLASS) {
                            List<? extends Tree> members = ((ClassTree) tree).getMembers();
                            for (Tree member : members) {
                                Tree t = null;
                                Tree enclosingTree = null;
                                if (member.getKind() == Tree.Kind.METHOD) {
                                    t = ((MethodTree) member).getBody();
                                    enclosingTree = member;
                                } else if (member.getKind() == Tree.Kind.BLOCK) {
                                    t = member;
                                }
                                if (t != null) {
                                    Interval interval = createInterval(cc.getCompilationUnit(), sourcePositions, lineMap, t, enclosingTree);
                                    if (interval != null) {
                                        intervals.add(interval);
                                    }
                                } else if (member.getKind() == Tree.Kind.VARIABLE) {
                                    VariableTree variable = (VariableTree) member;
                                    boolean isStatic = variable.getModifiers().getFlags().contains(Modifier.STATIC);
                                    if (isStatic) {
                                        String name = variable.getName().toString();
                                        name = cc.getElementUtilities().getElementName(element, true) + "::" + name;
                                        Interval interval = createInterval(cc.getCompilationUnit(), sourcePositions, lineMap, member, enclosingTree);
                                        if (interval != null) {
                                            intervals.addVariable(name, interval);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
        }
        return intervals;
    }

    private static Interval createInterval(CompilationUnitTree cut, SourcePositions sourcePositions, LineMap lineMap, Tree tree, Tree enclosingTree) {
        long start = sourcePositions.getStartPosition(cut, tree);
        long end = sourcePositions.getEndPosition(cut, tree);
        if (start != Diagnostic.NOPOS && end != Diagnostic.NOPOS) {
            int line1 = (int) lineMap.getLineNumber(start);
            int col1 = (int) lineMap.getColumnNumber(start);
            int line2 = (int) lineMap.getLineNumber(end);
            int col2 = (int) lineMap.getColumnNumber(end);
            Interval enclosingInterval = null;
            if (enclosingTree != null) {
                enclosingInterval = createInterval(cut, sourcePositions, lineMap, enclosingTree, null);
            }
            return new Interval(line1, col1, line2, col2, enclosingInterval);
        } else {
            return null;
        }
    }

    private static final class Intervals {

        private final List<Interval> intervals = new ArrayList<>();
        private final Map<String, Interval> variables = new HashMap<>();

        void add(Interval i) {
            int index = intervals.size();
            for (int idx = 0; idx < intervals.size(); idx++) {
                Interval ii = intervals.get(idx);
                if (i.l1 < ii.l1) {
                    index = idx;
                    break;
                }
            }
            intervals.add(index, i);
        }

        void addVariable(String name, Interval i) {
            variables.put(name, i);
        }

        boolean contains(int n) {
            for (Interval i : intervals) {
                if (i.contains(n)) {
                    return true;
                }
            }
            return false;
        }

        int getFirst() {
            if (intervals.size() > 0) {
                return intervals.get(0).l1;
            } else {
                return 0;
            }
        }

        int getLast() {
            int s = intervals.size();
            if (s > 0) {
                return intervals.get(s - 1).l2;
            } else {
                return -1;
            }
        }

        private int getFirstColumn(int line) {
            for (Interval i : intervals) {
                if (i.contains(line)) {
                    return i.firstColumnOn(line);
                }
            }
            return 1;
        }

        private int getLastColumn(int line) {
            for (Interval i : intervals) {
                if (i.contains(line)) {
                    return i.lastColumnOn(line);
                }
            }
            return -1;
        }

        // If a whole interval is covered, include its enclosing interval
        private int[] extendRange(int start, int end) {
            int xStartL = -1;
            int xStartC = -1;
            int xEndL = -1;
            int xEndC = -1;
            for (Interval i : intervals) {
                if (start <= i.l1 && i.l2 <= end) {
                    Interval ie = i.enclosing;
                    if (ie != null) {
                        if (xStartL < 0) {
                            xStartL = ie.l1;
                            xStartC = ie.c1;
                        }
                        xEndL = ie.l2;
                        xEndC = ie.c2;
                    }
                }
            }
            if (xStartL != -1) {
                return new int[] { xStartL, xStartC, xEndL, xEndC};
            } else {
                return null;
            }
        }
    }

    private static final class Interval {

        private final int l1;
        private final int c1;
        private final int l2;
        private final int c2;
        private final Interval enclosing;

        Interval(int l1, int c1, int l2, int c2, Interval enclosing) {
            assert l1 <= l2;
            this.l1 = l1;
            this.c1 = c1;
            this.l2 = l2;
            this.c2 = c2;
            if (enclosing != null) {
                assert enclosing.l1 <= l1;
                assert enclosing.l2 >= l2;
            }
            this.enclosing = enclosing;
        }

        private boolean contains(int l) {
            return l1 <= l && l <= l2;
        }

        private int firstColumnOn(int l) {
            if (l == l1) {
                return c1;
            } else {
                return 1;
            }
        }

        private int lastColumnOn(int l) {
            if (l == l2) {
                return c2;
            } else {
                return -1;
            }
        }

        @Override
        public String toString() {
            return "Interval<" + l1 + ", " + l2 + '>';
        }

    }
}
