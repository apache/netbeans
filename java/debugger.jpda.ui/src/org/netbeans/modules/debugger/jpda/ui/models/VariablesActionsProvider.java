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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types={NodeActionsProvider.class},
                                 position=650),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types={NodeActionsProvider.class},
                                 position=650),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types={NodeActionsProvider.class},
                                 position=650),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types={NodeActionsProvider.class},
                                 position=650)
})
public class VariablesActionsProvider implements NodeActionsProvider {
    
    
    private final Action GO_TO_SOURCE_ACTION = Models.createAction (
        NbBundle.getMessage(VariablesActionsProvider.class, "CTL_GoToSource"), 
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return true;
            }
            @Override
            public void perform (final Object[] nodes) {
                lookupProvider.lookupFirst(null, RequestProcessor.class).post(new Runnable() {
                    @Override
                    public void run() {
                        goToSource ((Field) nodes [0]);
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    private final Action GO_TO_TYPE_SOURCE_ACTION = Models.createAction (
        NbBundle.getMessage(VariablesActionsProvider.class, "CTL_GoToTypeSource"), 
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return true;
            }
            @Override
            public void perform (final Object[] nodes) {
                lookupProvider.lookupFirst(null, RequestProcessor.class).post(new Runnable() {
                    @Override
                    public void run() {
                        showSource (((ObjectVariable) nodes [0]).getClassType());
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
        
    private ContextProvider lookupProvider;

    
    public VariablesActionsProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT)
            return new Action[] {
                WatchesActionsProvider.NEW_WATCH_ACTION
            };
        if (node instanceof Field) {
            if (node instanceof ObjectVariable) {
                return new Action [] {
                    GO_TO_SOURCE_ACTION,
                    GO_TO_TYPE_SOURCE_ACTION,
                };
            } else {
                return new Action [] {
                    GO_TO_SOURCE_ACTION,
                };
            }
        }
        if (node instanceof ObjectVariable)
            return new Action [] {
                GO_TO_TYPE_SOURCE_ACTION,
            };
        if (node instanceof Variable)
            return new Action [] {
            };
        if (node.toString().startsWith ("SubArray")) // NOI18N
            return new Action [] {
            };
        if (node.equals ("NoInfo")) // NOI18N
            return new Action [] {
            };
        throw new UnknownTypeException (node);
    }
    
    @Override
    public void performDefaultAction (final Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Field) {
            lookupProvider.lookupFirst(null, RequestProcessor.class).post(new Runnable() {
                @Override
                public void run() {
                    goToSource ((Field) node);
                }
            });
            return;
        }
        if (node.toString().startsWith ("SubArray")) // NOI18N
            return ;
        if (node.equals ("NoInfo")) // NOI18N
            return;
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }
    
    private void goToSource (Field variable) {
        SourcePath ectx = lookupProvider.lookupFirst(null, SourcePath.class);
        ectx.showSource (variable);
    }

    private boolean isSourceAvailable (Field v) {
        SourcePath ectx = lookupProvider.lookupFirst(null, SourcePath.class);
        return ectx.sourceAvailable (v);
    }
    
    private void showSource(final JPDAClassType type) {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            final String typePath = EditorContextBridge.getRelativePath (type.getName());
            final String url = ((JPDADebuggerImpl) debugger).getEngineContext().getURL(type, null);
            final int lineNumber = findClassLine(url, type.getName());
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    boolean success = EditorContextBridge.getContext().showSource(url, lineNumber, null);
                    if (!success) {
                        NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(VariablesActionsProvider.class, "MSG_NoSourceFile", typePath, type), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(d);
                    }
                }
            });
        }
    }
    
    private static int findClassLine(String url, String clazz) {
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException ex) {
            return 1;
        }
        // TODO: Add getClassLineNumber() into EditorContext
        String editorContextImplName = "org.netbeans.modules.debugger.jpda.projects.EditorContextSupport"; // NOI18N
        Class editorContextImpl;
        try {
            editorContextImpl = Thread.currentThread().getContextClassLoader().loadClass(editorContextImplName);
        } catch (ClassNotFoundException cnfex) {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            if (cl == null) {
                return 1;
            }
            try {
                editorContextImpl = cl.loadClass(editorContextImplName);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return 1;
            }
        }
        try {
            Method getClassLineNumber = editorContextImpl.getDeclaredMethod(
                    "getClassLineNumber", FileObject.class, String.class, String[].class);  // NOI18N
            getClassLineNumber.setAccessible(true);
            Future<Integer> lineNumberFuture = (Future<Integer>) getClassLineNumber.invoke(null, fo, clazz, new String[] {});
            if (lineNumberFuture == null) {
                return 1;
            }
            Integer line = lineNumberFuture.get();
            if (line == null) {
                return 1;
            } else {
                return line.intValue();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return 1;
        }
    }

}
