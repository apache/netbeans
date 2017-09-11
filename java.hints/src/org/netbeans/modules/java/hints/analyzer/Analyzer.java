/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
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
