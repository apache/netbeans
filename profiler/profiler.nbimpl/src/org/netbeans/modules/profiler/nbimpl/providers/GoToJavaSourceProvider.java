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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.CommonUtils;
import org.netbeans.lib.profiler.ui.SwingWorker;
import org.netbeans.lib.profiler.utils.VMUtils;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProgressDisplayer;
import org.netbeans.modules.profiler.api.java.ProfilerTypeUtils;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.modules.profiler.nbimpl.javac.ParsingUtils;
import org.netbeans.modules.profiler.nbimpl.javac.ScanSensitiveTask;
import org.netbeans.modules.profiler.spi.java.GoToSourceProvider;
import org.netbeans.modules.profiler.ui.ProfilerProgressDisplayer;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service = GoToSourceProvider.class)
public final class GoToJavaSourceProvider extends GoToSourceProvider {
    private static final Logger LOG = Logger.getLogger(GoToJavaSourceProvider.class.getName());
    
    private static class GotoSourceRunnable implements Runnable {
        private Lookup.Provider project;
        private String className;
        private String methodName;
        private String signature;
        private int line;
        private final AtomicBoolean cancelled;
        private final AtomicBoolean result = new AtomicBoolean(true);
        
        public GotoSourceRunnable(Lookup.Provider project, String className, String methodName, String signature, int line, AtomicBoolean cancelled) {
            this.project = project;
            this.className = className;
            this.methodName = methodName;
            this.signature = signature;
            this.line = line;
            this.cancelled = cancelled;
        }
        
        @Override
        public void run() {
            SourceClassInfo ci = ProfilerTypeUtils.resolveClass(className, project);
            FileObject sourceFile = ci != null ? ci.getFile() : null;

            if (sourceFile == null) {
                return;
            }

            final JavaSource js = JavaSource.forFileObject(sourceFile);
            if (js != null) {
                ScanSensitiveTask<CompilationController> t = new ScanSensitiveTask<CompilationController>() {

                    public void run(CompilationController controller) throws Exception {
                        if (!controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).equals(JavaSource.Phase.ELEMENTS_RESOLVED)) {
                            return;
                        }
                        TypeElement parentClass = ElementUtilitiesEx.resolveClassByName(className, controller, true);

                        if (cancelled != null && cancelled.get()) return;
                        
                        if (ElementOpen.open(controller.getClasspathInfo(), parentClass)) {
                            Document doc = controller.getDocument();
                            if (doc instanceof StyledDocument) {
                                if (openAtLine(controller, doc, methodName, line)) {
                                    result.set(true);
                                    return;
                                }
                                if (methodName != null) {
                                    ExecutableElement methodElement = ElementUtilitiesEx.resolveMethodByName(controller, parentClass, methodName, signature);
                                    if (methodElement != null && ElementOpen.open(controller.getClasspathInfo(), methodElement)) {
                                        result.set(true);
                                        return;
                                    }
                                }
                            }
                            result.set(true);
                        }
                    }

                    @Override
                    public boolean shouldRetry() {
                        return !result.get();
                    }
                };
                
                ParsingUtils.invokeScanSensitiveTask(js, t);
            }
        }
        
        public boolean isSuccess() {
            return result.get();
        }
    }
    
    @Override
    @NbBundle.Messages({
        "MSG_ScanningInProgress=Please Wait...",
        "# {0} - full target name Class.Method(Signature)",
        "MSG_Opening=Opening {0}",
        "# {0} - full target name Class.Method(Signature)",
        "# {1} - line number",
        "MSG_OpeningOnLine=Opening {0}, line {1}",
        "MSG_CannotShowPrimitive=Cannot show source for primitive type"
    })
    public boolean openSource(final Lookup.Provider project, final String className, final String methodName, final String signature, final int line) {        
        
        String normalizedClassName = className.replace("[", "").replace("]", "").replace('/', '.'); // NOI18N // TODO: handle $?
        if (VMUtils.isPrimitiveType(normalizedClassName)) {
            ProfilerDialogs.displayWarning(Bundle.MSG_CannotShowPrimitive());
            return true;
        }
        
        final ProgressDisplayer pd = ProfilerProgressDisplayer.getDefault();
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        final ProgressDisplayer.ProgressController[] pc = new ProgressDisplayer.ProgressController[1];
        final GotoSourceRunnable r = new GotoSourceRunnable(project, normalizedClassName, methodName, signature, line, cancelled);
        
        new SwingWorker(false) {
            @Override
            protected void doInBackground() {
                r.run();
            }

            @Override
            protected void nonResponding() {
                // register a progress controller
                pc[0] = new ProgressDisplayer.ProgressController() {

                    @Override
                    public boolean cancel() {
                        if (cancelled.compareAndSet(false, true)) { // set as cancelled
                            pd.close(); // close the progress dialog if needed
                        }
                        return true;
                    }
                };
                // get the formmatted target name out of class/method/signature information
                String target = MethodNameFormatterFactory.getDefault().getFormatter().formatMethodName(className, methodName, signature).toFormatted();
                // open the progress dialog
                pd.showProgress(Bundle.MSG_ScanningInProgress(), (line == -1) ? Bundle.MSG_Opening(target) : Bundle.MSG_OpeningOnLine(target, line), pc[0]);
            }

            @Override
            protected void done() {
                // if not cancelled close the progress dialog
                if (!cancelled.get()) {
                    pd.close();
                }
            }            
        }.execute();
        return r.isSuccess();
    }

    @Override
    public boolean openFile(final FileObject srcFile, final int offset) {
        final boolean[] rslt = new boolean[] {false};
        Runnable action = new Runnable() {

            @Override
            public void run() {
                rslt[0] = UiUtils.open(srcFile, offset);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(action);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException e) {
                ProfilerLogger.log(e);
            }
        }
        return rslt[0];
    }

    private static boolean openAtLine(CompilationController controller, Document doc, String methodName, int line) {
        try {
            if (line > -1) {
                DataObject od = DataObject.find(controller.getFileObject());
                int offset = NbDocument.findLineOffset((StyledDocument) doc, line);
                ExecutableElement parentMethod = controller.getTreeUtilities().scopeFor(offset).getEnclosingMethod();
                if (parentMethod != null) {
                    String offsetMethodName = parentMethod.getSimpleName().toString();
                    if (methodName.equals(offsetMethodName)) {
                        LineCookie lc = od.getLookup().lookup(LineCookie.class);
                        if (lc != null) {
                            final Line l = lc.getLineSet().getCurrent(line - 1);

                            if (l != null) {
                                CommonUtils.runInEventDispatchThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                                    }
                                });
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException e) {
            LOG.log(Level.WARNING, "Error accessing dataobject", e);
        }
        return false;
    }
}
