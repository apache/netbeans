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

package org.netbeans.modules.java.hints.analyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.hints.analyzer.ui.AnalyzerTopComponent;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class Analyzer implements Runnable {
    public static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(Analyzer.class.getName(), 1, false, false);

    private final Lookup context;
    private final AtomicBoolean cancel;
    private final ProgressHandle handle;
    private final HintsSettings hintsSettings;

    public Analyzer(Lookup context, AtomicBoolean cancel, ProgressHandle handle, HintsSettings hintsSettings) {
        this.context = context;
        this.cancel = cancel;
        this.handle = handle;
        this.hintsSettings = hintsSettings;
    }

    public void run() {
        handle.start();

        try {
            List<FileObject> toProcess = new LinkedList<FileObject>();
            Queue<FileObject> q = new LinkedList<FileObject>();

            q.addAll(toAnalyze(context));

            while (!q.isEmpty()) {
                FileObject f = q.poll();

                if (f.isData() && JAVA_MIME_TYPE.equals(FileUtil.getMIMEType(f))) {
                    toProcess.add(f);
                }

                if (f.isFolder()) {
                    q.addAll(Arrays.asList(f.getChildren()));
                }
            }

            final List<ErrorDescription> eds = new LinkedList<ErrorDescription>();

            if (!toProcess.isEmpty()) {
                handle.switchToDeterminate(toProcess.size());

                ClasspathInfo cpInfo = ClasspathInfo.create(toProcess.get(0));
                JavaSource js = JavaSource.create(cpInfo, toProcess);
                final AtomicInteger f = new AtomicInteger();

                try {
                    js.runUserActionTask(new Task<CompilationController>() {

                        public void run(CompilationController cc) throws Exception {
                            if (cancel.get()) {
                                return;
                            }
                            
                            DataObject d = DataObject.find(cc.getFileObject());
                            EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
                            Document doc;

                            try {
                                doc = ec.openDocument();
                            } catch (UserQuestionException uqe) {
                                uqe.confirmed();
                                doc = ec.openDocument();
                            }

                            try {
                                handle.progress(FileUtil.getFileDisplayName(cc.getFileObject()));

                                if (cc.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                                    return;
                                }

                                handle.progress(f.incrementAndGet());

                                eds.addAll(new HintsInvoker(hintsSettings, new AtomicBoolean()).computeHints(cc));
                            } finally {
                            }
                        }
                    }, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (!cancel.get()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        AnalyzerTopComponent win = AnalyzerTopComponent.findInstance();
                        win.open();
                        win.requestActive();
                        win.setData(context, hintsSettings, eds);

                    }
                });
            }
        } finally {
            handle.finish();
        }
    }
    
    //@AWT
    public static void process(Lookup context, HintsSettings hintsSettings) {
        final AtomicBoolean abCancel = new AtomicBoolean();
        class Cancel implements Cancellable {
            public boolean cancel() {
                abCancel.set(true);
                return true;
            }
        }
        
        ProgressHandle h = ProgressHandleFactory.createHandle(NbBundle.getMessage(Analyzer.class, "LBL_AnalyzingJavadoc"), new Cancel()); // NOI18N

        RP.post(new Analyzer(context, abCancel, h, hintsSettings));
    }
    
    public static Lookup normalizeLookup(Lookup l) {
        if (!l.lookupAll(Project.class).isEmpty()) {
            return Lookups.fixed(l.lookupAll(Project.class).toArray(new Object[0]));
        }
        
        Collection<? extends FileObject> files = toAnalyze(l);
        
        if (!files.isEmpty()) {
            return Lookups.fixed(files.toArray(new Object[0]));
        }
        
        return null;
    }
    
    private static Collection<? extends FileObject> toAnalyze(Lookup l) {
        Set<FileObject> result = new LinkedHashSet<FileObject>();

        for (FileObject fo : l.lookupAll(FileObject.class)) {
            if (fo.getMIMEType().equals(JAVA_MIME_TYPE)) {
                result.add(fo);
            }
            if (fo.isFolder()) {
                if (containsJavaFiles(fo)) {
                    result.add(fo);
                }
            }
        }

        for (DataObject od : l.lookupAll(DataObject.class)) {
            FileObject primaryFile = od.getPrimaryFile();
            if (primaryFile.getMIMEType().equals(JAVA_MIME_TYPE)) {
                result.add(primaryFile);
            }
            if (primaryFile.isFolder()) {
                if (containsJavaFiles(primaryFile)) {
                    result.add(primaryFile);
                }
            }
        }
        
        for (Project p : l.lookupAll(Project.class)) {
            Sources s = ProjectUtils.getSources(p);
            
            for (SourceGroup sg : s.getSourceGroups("java")) { // NOI18N
                result.add(sg.getRootFolder());
            }
        }
        
        return result;
    }

    private static boolean containsJavaFiles(FileObject folder) {
        if (/* #159628 */Boolean.TRUE.equals(folder.getAttribute("isRemoteAndSlow"))) { // NOI18N
            return true;
        }
        FileObject[] children = folder.getChildren();
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];
            if (child.getMIMEType().equals(JAVA_MIME_TYPE)) {
                return true;
            } else {
                return containsJavaFiles(child);
            }
        }
        return false;
    }
}
