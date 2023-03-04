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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.debugger.jpda.projects.EditorContextSupport;
import org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Martin Entlicher
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=NodeActionsProviderFilter.class)
public class BreakpointsActionsProvider implements NodeActionsProviderFilter {
    
    private static final Action GO_TO_SOURCE_ACTION = Models.createAction (
        loc("CTL_Breakpoint_GoToSource_Label"), // NOI18N
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return true;
            }
            @Override
            public void perform (Object[] nodes) {
                goToSource ((JPDABreakpoint) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    private static String loc(String key) {
        return NbBundle.getMessage(BreakpointsActionsProvider.class, key);
    }

    @Override
    public Action[] getActions (NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        if (!(node instanceof JPDABreakpoint) || node instanceof ThreadBreakpoint) {
            return original.getActions (node);
        }
        Action[] oas = original.getActions (node);
        Action[] as = new Action [oas.length + 2];
        as [0] = GO_TO_SOURCE_ACTION;
        as [1] = null;
        System.arraycopy (oas, 0, as, 2, oas.length);
        return as;
    }
    
    @Override
    public void performDefaultAction (NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof JPDABreakpoint && !(node instanceof ThreadBreakpoint)) {
            goToSource ((JPDABreakpoint) node);
        } else {
            original.performDefaultAction (node);
        }
    }

    private static void goToSource (JPDABreakpoint b) {
        String url;
        int lineNumber;
        Future futureLineNumber = null;
        if (b instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) b;
            url = lb.getURL();
            lineNumber = lb.getLineNumber();
            if (lineNumber < 1) {
                lineNumber = 1;
            }
        } else if (b instanceof FieldBreakpoint) {
            FieldBreakpoint fb = (FieldBreakpoint) b;
            String fieldName = fb.getFieldName();
            String className = fb.getClassName ();
            FileObject fo = getFileObject(getRelativePath (className));
            if (fo == null) {
                return ;
            }
            url = fo.toURL ().toString ();
            Future<Integer> fi = EditorContextSupport.getFieldLineNumber (
                fo,
                className,
                fieldName
            );
            if (fi.isDone()) {
                try {
                    lineNumber = fi.get();
                } catch (InterruptedException ex) {
                    lineNumber = 1;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                    lineNumber = 1;
                }
            } else {
                futureLineNumber = fi;
                lineNumber = -1;
            }
        } else if (b instanceof MethodBreakpoint) {
            MethodBreakpoint mb = (MethodBreakpoint) b;
            String methodName = mb.getMethodName();
            String[] classFilters = mb.getClassFilters();
            if (classFilters.length < 1) {
                return ;
            }
            String className = classFilters[0];
            FileObject fo = getFileObject(getRelativePath (className));
            if (fo == null) {
                return ;
            }
            url = fo.toURL ().toString ();
            Future<int[]> fi = EditorContextSupport.getMethodLineNumbers(
                fo,
                className,
                mb.getClassExclusionFilters(),
                methodName,
                mb.getMethodSignature()
            );
            if (fi.isDone()) {
                int[] lineNumbers;
                try {
                    lineNumbers = fi.get();
                    if (lineNumbers.length == 0) {
                        lineNumber = 1;
                    } else {
                        lineNumber = lineNumbers[0];
                    }
                } catch (InterruptedException ex) {
                    lineNumber = 1;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                    lineNumber = 1;
                }
            } else {
                futureLineNumber = fi;
                lineNumber = -1;
            }
        } else if (b instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint eb = (ExceptionBreakpoint) b;
            String className = eb.getExceptionClassName();
            FileObject fo = getFileObject(getRelativePath (className));
            if (fo == null) {
                return ;
            }
            url = fo.toURL ().toString ();
            // TODO: EditorContextImpl.getClassLineNumber(fo, className);
            lineNumber = 1;
        } else if (b instanceof ClassLoadUnloadBreakpoint) {
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) b;
            String[] classNames = cb.getClassFilters();
            if (classNames.length == 0) {
                return;
            }
            String className = classNames[0];
            FileObject fo = getFileObject(getRelativePath (className));
            if (fo == null) {
                return ;
            }
            url = fo.toURL ().toString ();
            // TODO: EditorContextImpl.getClassLineNumber(fo, className);
            lineNumber = 1;
        } else {
            return;
        }
        if (futureLineNumber != null) {
            final Future future = futureLineNumber;
            final String u = url;
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Object lineObj = future.get();
                        final int line;
                        if (lineObj instanceof Integer) {
                            line = (Integer) lineObj;
                        } else {
                            int[] lines = (int[]) lineObj;
                            if (lines.length == 0) {
                                line = 1;
                            } else {
                                line = lines[0];
                            }
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                EditorContextImpl.showSourceLine (
                                    u,
                                    line,
                                    null
                                );
                            }
                        });
                    } catch (InterruptedException ex) {
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            return ;
        }
        EditorContextImpl.showSourceLine (
            url,
            lineNumber,
            null
        );
    }
    
    private static FileObject getFileObject(String classRelPath) {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager ().getCurrentEngine();
        if (engine != null) {
            SourcePathProvider sp = engine.lookupFirst(null, SourcePathProvider.class);
            if (sp != null) {
                String url = sp.getURL(classRelPath, false);
                if (url == null) {
                    url = sp.getURL(classRelPath, true);
                }
                if (url != null) {
                    try {
                        FileObject fo = URLMapper.findFileObject(new URL(url));
                        if (fo != null) {
                            return fo;
                        }
                    } catch (MalformedURLException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }
        classRelPath = SourcePathProviderImpl.normalize(classRelPath);
        FileObject fo = GlobalPathRegistry.getDefault().findResource(classRelPath);
        return fo;
    }

    private static String getRelativePath (String className) {
        int i = className.indexOf ('$');
        if (i > 0) {
            className = className.substring (0, i);
        }
        String sourceName = className.replace 
            ('.', '/') + ".java";
        return sourceName;
    }
    
}
