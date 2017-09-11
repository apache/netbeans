/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
