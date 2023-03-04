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
package org.netbeans.modules.debugger.jpda.visual.actions;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices.RemoteListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Go to the component source.
 * 
 * @author Martin Entlicher
 */
public class GoToSourceAction extends NodeAction {
    
    static RequestProcessor RP = new RequestProcessor("Go to Source");

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                final String type = ci.getType();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        showSource(type);
                    }
                });
            }
            RemoteListener rl = n.getLookup().lookup(RemoteListener.class);
            if (rl != null) {
                final String clazz = rl.getListener().referenceType().name();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        showSource(clazz);
                    }
                });
            }
        }
    }
    
    private void showSource(final String type) {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            final String typePath = EditorContextBridge.getRelativePath (type);
            final String url = ((JPDADebuggerImpl) debugger).getEngineContext().getURL(typePath, true);
            final int lineNumber = findClassLine(url, type);
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    boolean success = EditorContextBridge.getContext().showSource(url, lineNumber, null);
                    if (!success) {
                        NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(GoToSourceAction.class, "MSG_NoSourceFile", typePath, type), NotifyDescriptor.WARNING_MESSAGE);
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

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;
        for (Node n : activatedNodes) {
            JavaComponentInfo ci = n.getLookup().lookup(JavaComponentInfo.class);
            if (ci != null) {
                enabled = true;
                break;
            }
            RemoteListener rl = n.getLookup().lookup(RemoteListener.class);
            if (rl != null) {
                enabled = true;
                break;
            }
        }
        return enabled;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToSourceAction.class, "CTL_GoToSource");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GoToSourceAction.class);
    }

    
}
