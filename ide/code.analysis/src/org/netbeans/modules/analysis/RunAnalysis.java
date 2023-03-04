/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.analysis;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.analysis.RunAnalysisPanel.DialogState;
import org.netbeans.modules.analysis.RunAnalysisPanel.FutureWarnings;
import org.netbeans.modules.analysis.spi.AnalysisScopeProvider;
import org.netbeans.modules.analysis.spi.Analyzer;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.modules.analysis.spi.Analyzer.Context;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.netbeans.modules.analysis.spi.Analyzer.Result;
import org.netbeans.modules.analysis.ui.AnalysisProblemNode;
import org.netbeans.modules.analysis.ui.AnalysisResultTopComponent;
import org.netbeans.modules.analysis.ui.RequiredPluginsNode;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class RunAnalysis {

    private static final Logger LOG = Logger.getLogger(RunAnalysis.class.getName());
    private static final RequestProcessor WORKER = new RequestProcessor(RunAnalysisAction.class.getName(), 1, false, false);
    private static final int MAX_WORK = 1000;

    public static void showDialogAndRunAnalysis() {
        showDialogAndRunAnalysis(Lookups.fixed(Utilities.actionsGlobalContext().lookupAll(Object.class).toArray(new Object[0])), null);
    }
    
    @Messages({"BN_Inspect=Inspect",
               "BN_Cancel=Cancel",
               "TL_Inspect=Inspect"})
    public static void showDialogAndRunAnalysis(final Lookup context, DialogState startingState) {
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Analyzing...", null, null);
        final JButton runAnalysis = new JButton(Bundle.BN_Inspect());
        final RunAnalysisPanel rap = new RunAnalysisPanel(progress, context, runAnalysis, startingState);
        JButton cancel = new JButton(Bundle.BN_Cancel());
        HelpCtx helpCtx = new HelpCtx("org.netbeans.modules.analysis.RunAnalysis");
        DialogDescriptor dd = new DialogDescriptor(rap, Bundle.TL_Inspect(), true, new Object[] {runAnalysis, cancel}, runAnalysis, DialogDescriptor.DEFAULT_ALIGN, helpCtx, null);
        dd.setClosingOptions(new Object[0]);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        final AtomicBoolean doCancel = new AtomicBoolean();
        final AtomicReference<Analyzer> currentlyRunning = new AtomicReference<Analyzer>();

        runAnalysis.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                final long analysisStart = System.currentTimeMillis();
                
                runAnalysis.setEnabled(false);

                final AnalyzerFactory toRun = rap.getSelectedAnalyzer();
                final Configuration configuration = rap.getConfiguration();
                final String singleWarningId = rap.getSingleWarningId();
                final Collection<? extends AnalyzerFactory> analyzers = rap.getAnalyzers();
                final DialogState dialogState = rap.getDialogState();
                final FutureWarnings analyzerId2Description = rap.getAnalyzerId2Description();

                rap.started();
                progress.start();

                WORKER.post(new Runnable() {
                    @Override public void run() {
                        dialogState.save();
                        
                        Scope scope = rap.getSelectedScope(doCancel);

                        progress.switchToDeterminate(MAX_WORK);

                        final Map<AnalyzerFactory, List<ErrorDescription>> result = new HashMap<AnalyzerFactory, List<ErrorDescription>>();
                        final Map<ErrorDescription, Project> errorsToProjects = new IdentityHashMap<>();
                        Collection<MissingPlugin> missingPlugins = new ArrayList<MissingPlugin>();
                        Collection<AnalysisProblem> additionalProblems = new ArrayList<AnalysisProblem>();
                        
                        if (toRun == null) {
                            int doneSoFar = 0;
                            int bucketSize = MAX_WORK / analyzers.size();
                            for (AnalyzerFactory analyzer : analyzers) {
                                if (doCancel.get()) break;
                                doRunAnalyzer(analyzer, scope, progress, doneSoFar, bucketSize, result, errorsToProjects, missingPlugins, additionalProblems);
                                doneSoFar += bucketSize;
                            }
                        } else if (!doCancel.get()) {
                            doRunAnalyzer(toRun, scope, progress, 0, MAX_WORK, result, errorsToProjects, missingPlugins, additionalProblems);
                        }

                        final Collection<Node> extraNodes = new ArrayList<Node>();
                        
                        if (!missingPlugins.isEmpty()) {
                            extraNodes.add(new RequiredPluginsNode(missingPlugins));
                        }
                        
                        for (AnalysisProblem p : additionalProblems) {
                            extraNodes.add(new AnalysisProblemNode(p));
                        }
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                if (!doCancel.get()) {
                                    AnalysisResultTopComponent resultWindow = AnalysisResultTopComponent.findInstance();
                                    resultWindow.setData(context, dialogState, new AnalysisResult(result, errorsToProjects, analyzerId2Description, extraNodes));
                                    resultWindow.open();
                                    resultWindow.requestActive();
                                }

                                d.setVisible(false);
                                d.dispose();
                                
                                LOG.log(Level.FINE, "Total analysis time: {0}", System.currentTimeMillis() - analysisStart);
                            }
                        });
                    }

                    private void doRunAnalyzer(AnalyzerFactory analyzer, Scope scope, ProgressHandle handle, int bucketStart, int bucketSize, final Map<AnalyzerFactory, List<ErrorDescription>> result, final Map<ErrorDescription, Project> errorsToProjects, Collection<MissingPlugin> missingPlugins, Collection<AnalysisProblem> additionalProblems) {
                        List<ErrorDescription> current = new ArrayList<ErrorDescription>();                        
                        Preferences settings = configuration != null ? configuration.getPreferences().node(SPIAccessor.ACCESSOR.getAnalyzerId(analyzer)) : null;
                        Context context = SPIAccessor.ACCESSOR.createContext(scope, settings, singleWarningId, handle, bucketStart, bucketSize);
                        Result resCollector = SPIAccessor.ACCESSOR.createResult(current, errorsToProjects, additionalProblems);
                        Collection<? extends MissingPlugin> requiredPlugins = analyzer.requiredPlugins(context);
                        if (!requiredPlugins.isEmpty()) {
                            missingPlugins.addAll(requiredPlugins);
                            return ;
                        }
                        Analyzer a = analyzer.createAnalyzer(context, resCollector);                        
                        currentlyRunning.set(a);
                        if (doCancel.get()) return;
                        long s = System.currentTimeMillis();
                        for (ErrorDescription ed : a.analyze()) {
                            current.add(ed);
                        }
                        LOG.log(Level.FINE, "Analysis by {0} took {1}", new Object[] {SPIAccessor.ACCESSOR.getAnalyzerDisplayName(analyzer), System.currentTimeMillis() - s});
                        currentlyRunning.set(null);
                        if (!current.isEmpty())
                            result.put(analyzer, current);                        
                        additionalProblems.addAll(SPIAccessor.ACCESSOR.getAnalysisProblems(context));
                    }
                });
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                doCancel.set(true);
                Analyzer a = currentlyRunning.get();

                if (a != null) a.cancel();
                
                d.setVisible(false);
                d.dispose();
            }
        });

        d.setVisible(true);
    }

    
    static Map<Project, Map<FileObject, ClassPath>> projects2RegisteredContent(AtomicBoolean cancel) {
        Set<String> sourceIds = new HashSet<String>();

        for (PathRecognizer pr : Lookup.getDefault().lookupAll(PathRecognizer.class)) {
            Set<String> ids = pr.getSourcePathIds();

            if (ids == null) continue;

            sourceIds.addAll(ids);
        }

        Map<Project, Map<FileObject, ClassPath>> sourceRoots = new IdentityHashMap<Project, Map<FileObject, ClassPath>>();

        for (String id : sourceIds) {
            for (ClassPath sCP : GlobalPathRegistry.getDefault().getPaths(id)) {
                for (FileObject root : sCP.getRoots()) {
                    if (cancel.get()) return null;
                    Project owner = FileOwnerQuery.getOwner(root);

                    if (owner != null) {
                        Map<FileObject, ClassPath> projectSources = sourceRoots.get(owner);

                        if (projectSources == null) {
                            sourceRoots.put(owner, projectSources = new HashMap<FileObject, ClassPath>());
                        }

                        projectSources.put(root, sCP);
                    }
                }
            }
        }

        return sourceRoots;
    }

    static Scope addProjectToScope(Project p, Scope target, AtomicBoolean cancel, Map<Project, Map<FileObject, ClassPath>> projects2RegisteredContent) {
        Scope projectScope = p.getLookup().lookup(Scope.class);

        if (projectScope == null) {
            AnalysisScopeProvider scopeProvider = p.getLookup().lookup(AnalysisScopeProvider.class);
            
            projectScope = scopeProvider != null ? scopeProvider.getScope() : null;
        }
        
        if (projectScope != null) {
            return augment(target, projectScope.getSourceRoots(), projectScope.getFolders(), projectScope.getFiles());
        }

        Map<FileObject, ClassPath> roots = projects2RegisteredContent.get(p);

        if (roots != null) {
            for (Entry<FileObject, ClassPath> e : roots.entrySet()) {
                if (cancel.get()) return null;
                target = augment(target, Collections.singletonList(e.getKey()), null, null);
            }
        }

        return target;
    }

    private static @NonNull Scope augment(@NonNull Scope source, @NullAllowed Collection<FileObject> sourceRoots,
                                        @NullAllowed Collection<NonRecursiveFolder> folders,
                                        @NullAllowed Collection<FileObject> files) {
        Collection<FileObject> sourceRootsSet = new HashSet<FileObject>(source.getSourceRoots());
        if (sourceRoots != null) {
            sourceRootsSet.addAll(sourceRoots);
        }
        Collection<FileObject> filesSet = new HashSet<FileObject>(source.getFiles());
        if (files != null) {
            filesSet.addAll(files);
        }
        Collection<NonRecursiveFolder> foldersSet = new HashSet<NonRecursiveFolder>(source.getFolders());
        if (folders != null) {
            foldersSet.addAll(folders);
        }
        return Scope.create(sourceRootsSet, foldersSet, filesSet);
    }
}
