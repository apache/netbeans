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
package org.netbeans.spi.java.hints.support;

import com.sun.source.util.TreePath;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.hints.jackpot.spi.PatternConvertor;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.Trigger;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.spiimpl.batch.Scopes;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompiler;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.spi.editor.hints.*;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.Exceptions;

/**
 * Static utility classes for processing jackpot patterns.
 * <a href="https://github.com/apache/netbeans-jackpot30/tree/master/cmdline">Rules Language</a>
 * @author Jan Becicka
 * @since 1.1
 */
public final class TransformationSupport {

    private final String jackpotPattern;
    private final Transformer transformer;
    private AtomicBoolean cancel = new AtomicBoolean();

    private TransformationSupport(String jackpotPattern, Transformer transformer) {
        this.jackpotPattern = jackpotPattern;
        this.transformer = transformer;
    }
    
    /**
     * Creates new TransformationSupport representing given jackpotPattern.
     * @param jackpotPattern
     * @return
     */
    public static @NonNull TransformationSupport create(@NonNull String jackpotPattern) {
        return new TransformationSupport(jackpotPattern, null);
    }

    /**
     * Creates new TransformationSupport representing given jackpotPattern with custom Transformer.
     * @param inputJackpotPattern
     * @param t
     * @see Transformer
     * @return
     */
    public static @NonNull TransformationSupport create(@NonNull String inputJackpotPattern, @NonNull Transformer t) {
        return new TransformationSupport(inputJackpotPattern, t);
    }

    /**
     * Option to cancel query.
     * @param cancel
     * @return
     */
    public @NonNull TransformationSupport setCancel(@NonNull AtomicBoolean cancel) {
        this.cancel = cancel;
        return this;
    }


    /**
     * Run current transformation on all projects and collect results.
     * @return collection of {@link ModificationResult}
     */
    public @NonNull Collection<? extends ModificationResult> processAllProjects() {
        if (transformer!=null) {
            return performTransformation(jackpotPattern, transformer, cancel);
        } else {
            return performTransformation(jackpotPattern, cancel);
        }
    }

    
    /**
     * Process current transformation on given treePath and performs rewrite on
     * workingCopy.
     * @param workingCopy
     * @param treePath 
     */
    public void transformTreePath(@NonNull WorkingCopy workingCopy, @NonNull TreePath treePath) {
        if (transformer!=null) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else {
            performTransformation(workingCopy, treePath, jackpotPattern, cancel);
        }
    }


    /**
     * Transformer callback which is called for each occurrence during processing 
     * of {@link #performTransformation(java.lang.String, org.netbeans.spi.java.hints.support.JackpotSupport.Transformer, java.util.concurrent.atomic.AtomicBoolean) }
     */
    public interface Transformer {

        /**
         * Implement custom transformation of occurrence here.
         * @param copy
         * @param occurrence
         */
        public void transform(WorkingCopy copy, Occurrence occurrence);

    }
    
    
    
    /**
     * Performs transformation described by jackpotPattern on given workingCopy.
     * @param workingCopy
     * @param jackpotPattern
     * @param cancel
     */
    private static void performTransformation(WorkingCopy workingCopy, TreePath on, String jackpotPattern, AtomicBoolean cancel) {
        Iterable<? extends HintDescription> hints = PatternConvertor.create(jackpotPattern);
        HintsInvoker inv = new HintsInvoker(HintsSettings.getSettingsFor(workingCopy.getFileObject()), cancel);
        Map<HintDescription, List<ErrorDescription>> computeHints = inv.computeHints(workingCopy, on, false, hints, new ArrayList<>());
        
        if (computeHints == null || cancel.get()) return ;
        
        List<ErrorDescription> errs = new ArrayList<>();
        for (Entry<HintDescription, List<ErrorDescription>> entry: computeHints.entrySet()) {
            errs.addAll(entry.getValue());
        }
        List<MessageImpl> problems = new LinkedList<>();

        try {
            if (BatchUtilities.applyFixes(workingCopy, Collections.<Project, Set<String>>emptyMap(), errs, null, new ArrayList<>(), problems)) {
                throw new IllegalStateException();
            }
        } catch (IllegalStateException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        if (!problems.isEmpty()) {
            throw new IllegalStateException(problems.get(0).text);
        }
    }
    
    /**
     * Performs jackpotPattern transformation in all open projects.
     * @param jackpotPattern
     * @param cancel
     * @return
     */
    private static  Collection<? extends ModificationResult> performTransformation(String jackpotPattern, AtomicBoolean cancel) {
        Collection<MessageImpl> problems = new LinkedList<>();
        BatchResult batchResult = BatchSearch.findOccurrences(PatternConvertor.create(jackpotPattern), Scopes.allOpenedProjectsScope());
        return BatchUtilities.applyFixes(batchResult, new ProgressHandleWrapper(1, 1), cancel, problems);
    }
    
    /**
     * Performs transformation defined by transformer on all occurrences, which matches inputJackpotPattern.
     * @param inputJackpotPattern
     * @param transformer
     * @return collection of ModificationResults.
     */
    private static Collection<? extends ModificationResult> performTransformation(final String inputJackpotPattern, final Transformer transformer, AtomicBoolean cancel) {
        List<HintDescription> descriptions = new ArrayList<>();

        for (HintDescription hd : PatternConvertor.create(inputJackpotPattern)) {
            final String triggerPattern = ((Trigger.PatternDescription) hd.getTrigger()).getPattern();
            descriptions.add(
                HintDescriptionFactory.create()
                        .setTrigger(hd.getTrigger())
                        .setTriggerOptions(hd.getTrigger()
                        .getOptions())
                        .setWorker((HintContext ctx) -> {
                    final Map<String, TypeMirrorHandle<?>> constraintsHandles = new HashMap<>();

                    for (Map.Entry<String, TypeMirror> c : ctx.getConstraints().entrySet()) {
                        constraintsHandles.put(c.getKey(), TypeMirrorHandle.create(c.getValue()));
                    }

                    Fix fix = new JavaFix(ctx.getInfo(), ctx.getPath()) {
                        @Override protected String getText() {
                            return "";
                        }
                        @Override protected void performRewrite(JavaFix.TransformationContext ctx) {
                            WorkingCopy wc = ctx.getWorkingCopy();
                            Map<String, TypeMirror> constraints = new HashMap<>();

                            for (Map.Entry<String, TypeMirrorHandle<?>> c : constraintsHandles.entrySet()) {
                                constraints.put(c.getKey(), c.getValue().resolve(wc));
                            }

                            Pattern pattern = PatternCompiler.compile(wc, triggerPattern, constraints, Collections.<String>emptyList());
                            Collection<? extends Occurrence> occurrence = Matcher.create(wc).setTreeTopSearch().setSearchRoot(ctx.getPath()).match(pattern);

                            assert occurrence.size() == 1;

                            transformer.transform(wc, occurrence.iterator().next());
                        }
                    }.toEditorFix();
                    
                    return List.of(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "", List.of(fix), ctx.getInfo().getFileObject(), 0, 0));
                }).produce()
            );

        }
        
        BatchSearch.BatchResult batchResult = BatchSearch.findOccurrences(descriptions, Scopes.allOpenedProjectsScope());
        return BatchUtilities.applyFixes(batchResult, new ProgressHandleWrapper(1, 1), cancel, new ArrayList<>());
    }
    
    /**
     * Performs transformation described by transformationJackpotPattern on all occurences described by inputJackpotPattern.
     * @param inputJackpotPattern
     * @param transformationJackpotPattern
     * @param cancel 
     * @return
     */
    private static Collection<? extends ModificationResult> performTransformation(String inputJackpotPattern, final String transformationJackpotPattern, AtomicBoolean cancel) {
        return performTransformation(inputJackpotPattern, (WorkingCopy copy, Occurrence occurrence) -> {
            try {
                Fix toFix = TransformationSupport.rewriteFix(copy, "whatever", occurrence.getOccurrenceRoot(), transformationJackpotPattern, occurrence.getVariables(), occurrence.getMultiVariables(), occurrence.getVariables2Names(), Collections.<String, TypeMirror>emptyMap(), Collections.<String, String>emptyMap());
                TransformationSupport.process(((JavaFixImpl) toFix).jf, copy, false, null, new ArrayList<>());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }, cancel);
    }

    private static ChangeInfo process(JavaFix jf, WorkingCopy wc, boolean canShowUI, Map<org.openide.filesystems.FileObject, byte[]> resourceContent, Collection<? super RefactoringElementImplementation> fileChanges) throws Exception {
        return JavaFixImpl.Accessor.INSTANCE.process(jf, wc, canShowUI, resourceContent, fileChanges);
    }

    /**
     * 
     * @param info
     * @param displayName
     * @param what
     * @param to
     * @param parameters
     * @param parametersMulti
     * @param parameterNames
     * @param constraints
     * @param options
     * @param imports
     * @return
     */
    private static Fix rewriteFix(CompilationInfo info, String displayName, TreePath what, final String to, Map<String, TreePath> parameters, Map<String, Collection<? extends TreePath>> parametersMulti, final Map<String, String> parameterNames, Map<String, TypeMirror> constraints, Map<String, String> options, String... imports) {
        return JavaFixImpl.Accessor.INSTANCE.rewriteFix(info, displayName, what, to, parameters, parametersMulti, parameterNames, constraints, options, imports);
    }
    
}
