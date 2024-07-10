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

package org.netbeans.modules.java.hints.spiimpl.hints;

import com.sun.source.tree.Tree;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.Hacks;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.Trigger.Kinds;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.spiimpl.SPIAccessor;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch;
import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompiler;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.spi.java.hints.TriggerOptions;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class HintsInvoker {

    private final Map<String, Long> timeLog = new HashMap<>();

    private final HintsSettings settings;
    private final int caret;
    private final int from;
    private final int to;
    private final boolean bulkMode;
    private final AtomicBoolean cancel;

    public HintsInvoker(HintsSettings settings, AtomicBoolean cancel) {
        this(settings, false, cancel);
    }

    public HintsInvoker(HintsSettings settings, boolean bulkMode, AtomicBoolean cancel) {
        this(settings, -1, -1, -1, bulkMode, cancel);
    }

    public HintsInvoker(HintsSettings settings, int caret, AtomicBoolean cancel) {
        this(settings, caret, -1, -1, false, cancel);
    }

    public HintsInvoker(HintsSettings settings, int from, int to, AtomicBoolean cancel) {
        this(settings, -1, from, to, false, cancel);
    }

    private HintsInvoker(HintsSettings settings, int caret, int from, int to, boolean bulkMode, AtomicBoolean cancel) {
        this.settings = settings;
        this.caret = caret;
        this.from = from;
        this.to = to;
        this.bulkMode = bulkMode;
        this.cancel = cancel;
    }

    @CheckForNull
    public List<ErrorDescription> computeHints(CompilationInfo info) {
        return computeHints(info, new TreePath(info.getCompilationUnit()));
    }

    private List<ErrorDescription> computeHints(CompilationInfo info, TreePath startAt) {
        List<HintDescription> descs = new LinkedList<>();
        Map<HintMetadata, ? extends Collection<? extends HintDescription>> allHints = RulesManager.getInstance().readHints(info, null, cancel);

        if (allHints == null || cancel.get()) return null;
        SourceVersion sourceLevel = info.getSourceVersion();
        for (Entry<HintMetadata, ? extends Collection<? extends HintDescription>> e : allHints.entrySet()) {
            HintMetadata m = e.getKey();
            SourceVersion hintSourceLevel = m.sourceVersion;
            // hint requires a higher source level than the current compilation is configured for
            if (hintSourceLevel != null &&
                (sourceLevel.compareTo(hintSourceLevel) < 0)) {
                continue;
            }
            if (!settings.isEnabled(m)) {
                continue;
            }

            if (caret != -1) {
                if (m.kind == Hint.Kind.ACTION) {
                    descs.addAll(e.getValue());
                } else {
                    if (settings.getSeverity(m) == Severity.HINT) {
                        descs.addAll(e.getValue());
                    }
                }
            } else {
                if (m.kind == Hint.Kind.INSPECTION) {
                    if (settings.getSeverity(m) != Severity.HINT) {
                        descs.addAll(e.getValue());
                    }
                }
            }
        }

        List<ErrorDescription> errors = join(computeHints(info, startAt, descs, new ArrayList<>()));

        dumpTimeSpentInHints();
        
        return errors;
    }

    @CheckForNull
    public List<ErrorDescription> computeHints(CompilationInfo info,
                                               Iterable<? extends HintDescription> hints) {
        return computeHints(info, hints, new LinkedList<>());
    }

    @CheckForNull
    public List<ErrorDescription> computeHints(CompilationInfo info,
                                               Iterable<? extends HintDescription> hints,
                                               Collection<? super MessageImpl> problems) {
        return join(computeHints(info, new TreePath(info.getCompilationUnit()), hints, problems));
    }

    @CheckForNull
    public Map<HintDescription, List<ErrorDescription>> computeHints(CompilationInfo info,
                                        TreePath startAt,
                                        Iterable<? extends HintDescription> hints,
                                        Collection<? super MessageImpl> problems) {
        return computeHints(info, startAt, true, hints, problems);
    }
    
    @CheckForNull
    public Map<HintDescription, List<ErrorDescription>> computeHints(CompilationInfo info,
                                        TreePath startAt,
                                        boolean recursive,
                                        Iterable<? extends HintDescription> hints,
                                        Collection<? super MessageImpl> problems) {

        Map<Class<?>, List<HintDescription>> triggerKind2Hints = Map.of(
            Kinds.class, new ArrayList<>(),
            PatternDescription.class, new ArrayList<>()
        );

        SourceVersion srcVersion = info.getSourceVersion();
        for (HintDescription hd : hints) {
            SourceVersion hVersion = hd.getMetadata().sourceVersion;
            if (hVersion != null && srcVersion.compareTo(hVersion) < 0) {
                continue;
            }
            triggerKind2Hints.get(hd.getTrigger().getClass())
                             .add(hd);
        }

        if (caret != -1) {
            TreePath tp = info.getTreeUtilities().pathFor(caret);
            return computeSuggestions(info, tp, true, triggerKind2Hints, problems);
        } else if (from != -1 && to != -1) {
            return computeHintsInSpan(info, triggerKind2Hints, problems);
        } else if (!recursive) {
            return computeSuggestions(info, startAt, false, triggerKind2Hints, problems);
        } else {
            return computeHintsImpl(info, startAt, triggerKind2Hints, problems);
        }
    }

    private Map<HintDescription, List<ErrorDescription>> computeHintsImpl(CompilationInfo info,
                                        TreePath startAt,
                                        Map<Class<?>, List<HintDescription>> triggerKind2Hints,
                                        Collection<? super MessageImpl> problems) {
        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<>();
        List<HintDescription> kindBasedHints = triggerKind2Hints.get(Kinds.class);

        timeLog.put("[C] Kind Based Hints", (long) kindBasedHints.size());

        if (!kindBasedHints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            new ScannerImpl(info, cancel, sortByKinds(kindBasedHints)).scan(startAt, errors);

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Hints", kindEnd - kindStart);
        }

        if (cancel.get()) return null;
        
        List<HintDescription> patternBasedHints = triggerKind2Hints.get(PatternDescription.class);

        timeLog.put("[C] Pattern Based Hints", (long) patternBasedHints.size());

        long patternStart = System.currentTimeMillis();

        Map<PatternDescription, List<HintDescription>> patternHints = sortByPatterns(patternBasedHints);
        Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

        long bulkPatternStart = System.currentTimeMillis();

        BulkPattern bulkPattern = BulkSearch.getDefault().create(info, cancel, patternTests.keySet());

        if (bulkPattern == null || cancel.get()) return null;
        
        long bulkPatternEnd = System.currentTimeMillis();

        timeLog.put("Bulk Pattern preparation", bulkPatternEnd - bulkPatternStart);

        long bulkStart = System.currentTimeMillis();

        Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, cancel, startAt, bulkPattern, timeLog);
        
        if (occurringPatterns == null || cancel.get()) return null;

        long bulkEnd = System.currentTimeMillis();

        timeLog.put("Bulk Search", bulkEnd - bulkStart);
        
        Map<HintDescription, List<ErrorDescription>> computedHints = doComputeHints(info, occurringPatterns, patternTests, patternHints, problems);

        if (computedHints == null || cancel.get()) return null;
        
        mergeAll(errors, computedHints);

        long patternEnd = System.currentTimeMillis();

        timeLog.put("Pattern Based Hints", patternEnd - patternStart);

        return errors;
    }

    private Map<HintDescription, List<ErrorDescription>> computeHintsInSpan(CompilationInfo info,
                                        Map<Class<?>, List<HintDescription>> triggerKind2Hints,
                                        Collection<? super MessageImpl> problems) {

        TreePath path = info.getTreeUtilities().pathFor((from + to) / 2);

        while (path.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), path.getLeaf());
            int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());

            if (start <= from && end >= to) {
                break;
            }

            path = path.getParentPath();
        }

        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<>();
        List<HintDescription> kindBasedHints = triggerKind2Hints.get(Kinds.class);

        if (!kindBasedHints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            new ScannerImpl(info, cancel, sortByKinds(kindBasedHints)).scan(path, errors);

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Hints", kindEnd - kindStart);
        }

        List<HintDescription> patternBasedHints = triggerKind2Hints.get(PatternDescription.class);

        if (!patternBasedHints.isEmpty()) {
            long patternStart = System.currentTimeMillis();

            Map<PatternDescription, List<HintDescription>> patternHints = sortByPatterns(patternBasedHints);
            Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

            long bulkStart = System.currentTimeMillis();

            BulkPattern bulkPattern = BulkSearch.getDefault().create(info, cancel, patternTests.keySet());
            
            if (bulkPattern == null || cancel.get()) return null;
            
            Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, cancel, path, bulkPattern, timeLog);

            if (occurringPatterns == null || cancel.get()) return null;
        
            long bulkEnd = System.currentTimeMillis();

            timeLog.put("Bulk Search", bulkEnd - bulkStart);
            
            Map<HintDescription, List<ErrorDescription>> computedHints = doComputeHints(info, occurringPatterns, patternTests, patternHints, problems);

            if (computedHints == null || cancel.get()) return null;
            
            mergeAll(errors, computedHints);

            long patternEnd = System.currentTimeMillis();

            timeLog.put("Pattern Based Hints", patternEnd - patternStart);
        }

        if (path != null) {
            Map<HintDescription, List<ErrorDescription>> suggestions = computeSuggestions(info, path, true, triggerKind2Hints, problems);
            
            if (suggestions == null || cancel.get()) return null;
            
            mergeAll(errors, suggestions);
        }

        return errors;
    }

    private Map<HintDescription, List<ErrorDescription>> computeSuggestions(CompilationInfo info,
                                        TreePath workOn,
                                        boolean up,
                                        Map<Class<?>, List<HintDescription>> triggerKind2Hints,
                                        Collection<? super MessageImpl> problems) {
        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<>();
        List<HintDescription> kindBasedHints = triggerKind2Hints.get(Kinds.class);

        if (!kindBasedHints.isEmpty()) {
            long kindStart = System.currentTimeMillis();
            
            Map<Kind, List<HintDescription>> hints = sortByKinds(kindBasedHints);
            TreePath proc = workOn;

            while (proc != null) {
                new ScannerImpl(info, cancel, hints).scanDoNotGoDeeper(proc, errors);
                if (!up) break;
                proc = proc.getParentPath();
            }

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Suggestions", kindEnd - kindStart);
        }
        
        if (cancel.get()) return null;

        List<HintDescription> patternBasedHints = triggerKind2Hints.get(PatternDescription.class);

        if (!patternBasedHints.isEmpty()) {
            long patternStart = System.currentTimeMillis();

            Map<PatternDescription, List<HintDescription>> patternHints = sortByPatterns(patternBasedHints);
            Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

            //pretend that all the patterns occur on all treepaths from the current path
            //up (probably faster than using BulkSearch over whole file)
            //TODO: what about machint trees under the current path?
            Set<TreePath> paths = new HashSet<>();

            TreePath tp = workOn;

            while (tp != null) {
                paths.add(tp);
                if (!up) break;
                tp = tp.getParentPath();
            }

            Map<String, Collection<TreePath>> occurringPatterns = new HashMap<>();

            for (String p : patternTests.keySet()) {
                occurringPatterns.put(p, paths);
            }

//            long bulkStart = System.currentTimeMillis();
//
//            BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternTests.keySet());
//            Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, new TreePath(info.getCompilationUnit()), bulkPattern, timeLog);
//
//            long bulkEnd = System.currentTimeMillis();
//
//            Set<Tree> acceptedLeafs = new HashSet<Tree>();
//
//            TreePath tp = workOn;
//
//            while (tp != null) {
//                acceptedLeafs.add(tp.getLeaf());
//                tp = tp.getParentPath();
//            }
//
//            for (Entry<String, Collection<TreePath>> e : occurringPatterns.entrySet()) {
//                for (Iterator<TreePath> it = e.getValue().iterator(); it.hasNext(); ) {
//                    if (!acceptedLeafs.contains(it.next().getLeaf())) {
//                        it.remove();
//                    }
//                }
//            }
//
//            timeLog.put("Bulk Search", bulkEnd - bulkStart);

            Map<HintDescription, List<ErrorDescription>> computed = doComputeHints(info, occurringPatterns, patternTests, patternHints, problems);
            
            if (computed == null || cancel.get()) return null;
            
            mergeAll(errors, computed);

            long patternEnd = System.currentTimeMillis();

            timeLog.put("Pattern Based Hints", patternEnd - patternStart);
        }

        return errors;
    }

    public Map<HintDescription, List<ErrorDescription>> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, Map<PatternDescription, List<HintDescription>> patternHints) throws IllegalStateException {
        return doComputeHints(info, occurringPatterns, patterns, patternHints, new LinkedList<>());
    }

    private static Map<Kind, List<HintDescription>> sortByKinds(List<HintDescription> kindBasedHints) {
        Map<Kind, List<HintDescription>> result = new EnumMap<>(Kind.class);

        for (HintDescription hd : kindBasedHints) {
            for (Kind k : ((Kinds) hd.getTrigger()).getKinds()) {
                result.computeIfAbsent(k, l -> new LinkedList<>()).add(hd);
            }
        }

        return result;
    }

    private static Map<PatternDescription, List<HintDescription>> sortByPatterns(List<HintDescription> kindBasedHints) {
        Map<PatternDescription, List<HintDescription>> result = new HashMap<>();

        for (HintDescription hd : kindBasedHints) {
            result.computeIfAbsent((PatternDescription) hd.getTrigger(), k -> new LinkedList<>()).add(hd);
        }

        return result;
    }

    public static Map<String, List<PatternDescription>> computePatternTests(Map<PatternDescription, List<HintDescription>> patternHints) {
        Map<String, List<PatternDescription>> patternTests = new HashMap<>();
        for (Entry<PatternDescription, List<HintDescription>> e : patternHints.entrySet()) {
            String p = e.getKey().getPattern();
            patternTests.computeIfAbsent(p, k -> new LinkedList<>()).add(e.getKey());
        }
        return patternTests;
    }

    private Map<HintDescription, List<ErrorDescription>> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, Map<PatternDescription, List<HintDescription>> patternHints, Collection<? super MessageImpl> problems) throws IllegalStateException {
        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<>();

        for (Entry<String, Collection<TreePath>> occ : occurringPatterns.entrySet()) {
            PATTERN_LOOP: for (PatternDescription d : patterns.get(occ.getKey())) {
                if (cancel.get()) return null;
                
                Map<String, TypeMirror> constraints = new HashMap<>();

                for (Entry<String, String> e : d.getConstraints().entrySet()) {
                    TypeMirror designedType = Hacks.parseFQNType(info, e.getValue());

                    if (designedType == null || designedType.getKind() == TypeKind.ERROR) {
                        //will not bind to anything anyway (#190449), skip pattern:
                        continue PATTERN_LOOP;
                    }

                    constraints.put(e.getKey(), designedType);
                }

                Pattern pattern = PatternCompiler.compile(info, occ.getKey(), constraints, d.getImports());

                for (TreePath candidate : occ.getValue()) {
                    if (cancel.get()) return null;
                
                    Iterator<? extends Occurrence> verified = Matcher.create(info).setCancel(cancel).setSearchRoot(candidate).setTreeTopSearch().setKeepSyntheticTrees().match(pattern).iterator();

                    if (!verified.hasNext()) {
                        continue;
                    }

                    Set<String> suppressedWarnings = new HashSet<>(Utilities.findSuppressedWarnings(info, candidate));
                    Occurrence verifiedVariables = verified.next();
                    
                    boolean guarded = isInGuarded(info, candidate);

                    for (HintDescription hd : patternHints.get(d)) {
                        HintMetadata hm = hd.getMetadata();
                        // skip guarded sections
                        if (guarded && !hd.getTrigger().hasOption(TriggerOptions.PROCESS_GUARDED)) {
                            continue;
                        }
                        
                        HintContext c = SPIAccessor.getINSTANCE().createHintContext(info, settings, hm, candidate, verifiedVariables.getVariables(), verifiedVariables.getMultiVariables(), verifiedVariables.getVariables2Names(), constraints, problems, bulkMode, cancel, caret);

                        if (!Collections.disjoint(suppressedWarnings, hm.suppressWarnings))
                            continue;

                        Collection<? extends ErrorDescription> workerErrors = runHint(hd, c);

                        if (workerErrors != null) {
                            merge(errors, hd, workerErrors);
                        }
                    }
                }
            }
        }

        return errors;
    }

    public Map<String, Long> getTimeLog() {
        return timeLog;
    }

    private final class ScannerImpl extends CancellableTreePathScanner<Void, Map<HintDescription, List<ErrorDescription>>> {

        private final Deque<Set<String>> suppresWarnings = new ArrayDeque<>();
        private final CompilationInfo info;
        private final ProcessingEnvironment env;
        private final Map<Kind, List<HintDescription>> hints;
        
        public ScannerImpl(CompilationInfo info, AtomicBoolean cancel, Map<Kind, List<HintDescription>> hints) {
            super(cancel);
            this.info = info;
            this.env  = null;
            this.hints = hints;
        }

        private void runAndAdd(TreePath path, List<HintDescription> rules, Map<HintDescription, List<ErrorDescription>> d) {
            if (rules != null) {
                boolean guarded = isInGuarded(info, path);
                OUTER: for (HintDescription hd : rules) {
                    if (isCanceled()) {
                        return ;
                    }
                    if (guarded && !hd.getTrigger().hasOption(TriggerOptions.PROCESS_GUARDED)) {
                        continue;
                    }

                    HintMetadata hm = hd.getMetadata();

                    for (String wname : hm.suppressWarnings) {
                        if( !suppresWarnings.isEmpty() && suppresWarnings.peek().contains(wname)) {
                            continue OUTER;
                        }
                    }

                    HintContext c = SPIAccessor.getINSTANCE().createHintContext(info, settings, hm, path, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap(), Collections.<String, TypeMirror>emptyMap(), new ArrayList<>(), bulkMode, cancel, caret);
                    Collection<? extends ErrorDescription> errors = runHint(hd, c);

                    if (errors != null) {
                        merge(d, hd, errors);
                    }
                }
            }
        }

        @Override
        public Void scan(Tree tree, Map<HintDescription, List<ErrorDescription>> p) {
            if (tree == null)
                return null;

            TreePath tp = new TreePath(getCurrentPath(), tree);
            Kind k = tree.getKind();

            boolean b = pushSuppressWarrnings(tp);
            try {
                runAndAdd(tp, hints.get(k), p);

                if (isCanceled()) {
                    return null;
                }

                return super.scan(tree, p);
            } finally {
                if (b) {
                    suppresWarnings.pop();
                }
            }
        }

        @Override
        public Void scan(TreePath path, Map<HintDescription, List<ErrorDescription>> p) {
            Kind k = path.getLeaf().getKind();
            boolean b = pushSuppressWarrnings(path);
            try {
                runAndAdd(path, hints.get(k), p);

                if (isCanceled()) {
                    return null;
                }

                return super.scan(path, p);
            } finally {
                if (b) {
                    suppresWarnings.pop();
                }
            }
        }

        public void scanDoNotGoDeeper(TreePath path, Map<HintDescription, List<ErrorDescription>> p) {
            Kind k = path.getLeaf().getKind();
            runAndAdd(path, hints.get(k), p);
        }

        private boolean pushSuppressWarrnings(TreePath path) {
            switch (path.getLeaf().getKind()) {
                case ANNOTATION_TYPE, CLASS, ENUM, INTERFACE, METHOD, VARIABLE -> {
                    Set<String> current = suppresWarnings.isEmpty() ? null : suppresWarnings.peek();
                    Set<String> nju = current == null ? new HashSet<>() : new HashSet<>(current);

                    Element e = getTrees().getElement(path);

                    if (e != null) {
                        for (AnnotationMirror am : e.getAnnotationMirrors()) {
                            String name = ((TypeElement)am.getAnnotationType().asElement()).getQualifiedName().toString();
                            if ("java.lang.SuppressWarnings".equals(name)) { // NOI18N
                                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                                    if ("value".equals(entry.getKey().getSimpleName().toString()) && entry.getValue().getValue() instanceof List list) { // NOI18N
                                        for (Object obj : list) {
                                            if (obj instanceof AnnotationValue av && av.getValue() instanceof String str) {
                                                nju.add(str);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    suppresWarnings.push(nju);
                    return true;
                }
            }
            return false;
        }

        private Trees getTrees() {
            return info != null ? info.getTrees() : Trees.instance(env);
        }
    }

    static boolean isInGuarded(CompilationInfo info, TreePath tree) {
        if (info == null) {
            return false;
        }

        try {
            Document doc = info.getDocument();

            if (doc instanceof GuardedDocument gdoc) {
                int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree.getLeaf());
                int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree.getLeaf());
                boolean[] ret = { false };
                gdoc.render(() -> {
                    // MarkBlockChain should only be accessed under doc's readlock to guarantee a stability of the offsets.
                    MarkBlockChain guardedBlockChain = gdoc.getGuardedBlockChain();
                    if (guardedBlockChain.compareBlock(start, end) == MarkBlock.INNER) {
                        ret[0] = true;
                    }
                });
                return ret[0];
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    private Collection<? extends ErrorDescription> runHint(HintDescription hd, HintContext ctx) {
        long start = System.nanoTime();

        try {
            return hd.getWorker().createErrors(ctx);
        } finally {
            long end = System.nanoTime();
            reportSpentTime(hd.getMetadata().id, end - start);
        }
    }

    private static <K, V> void merge(Map<K, List<V>> to, K key, Collection<? extends V> value) {
        to.computeIfAbsent(key, k -> new LinkedList<>())
          .addAll(value);
    }

    private static <K, V> void mergeAll(Map<K, List<V>> to, Map<? extends K, ? extends Collection<? extends V>> what) {
        for (Entry<? extends K, ? extends Collection<? extends V>> e : what.entrySet()) {
            to.computeIfAbsent(e.getKey(), k -> new LinkedList<>())
              .addAll(e.getValue());
        }
    }

    private static List<ErrorDescription> join(Map<?, ? extends List<? extends ErrorDescription>> errors) {
        if (errors == null) return null;
        
        List<ErrorDescription> result = new LinkedList<>();

        for (Entry<?, ? extends Collection<? extends ErrorDescription>> e : errors.entrySet()) {
            result.addAll(e.getValue());
        }

        return result;
    }

    private static final boolean logTimeSpentInHints = Boolean.getBoolean("java.HintsInvoker.time.in.hints");
    private final Map<String, Long> hint2SpentTime = new HashMap<>();

    private void reportSpentTime(String id, long nanoTime) {
        if (!logTimeSpentInHints) return;
        
        Long prev = hint2SpentTime.get(id);

        if (prev == null) {
            prev = (long) 0;
        }

        hint2SpentTime.put(id, prev + nanoTime);
    }

    private void dumpTimeSpentInHints() {
        if (!logTimeSpentInHints) return;

        List<Entry<String, Long>> l = new ArrayList<>(hint2SpentTime.entrySet());

        l.sort((Entry<String, Long> o1, Entry<String, Long> o2) -> (int) Math.signum(o1.getValue() - o2.getValue()));

        for (Entry<String, Long> e : l) {
            System.err.println(e.getKey() + "=" + String.format("%3.2f", e.getValue() / 1000000.0));
        }
    }
}
