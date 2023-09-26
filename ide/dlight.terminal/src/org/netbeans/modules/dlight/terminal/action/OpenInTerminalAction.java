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
package org.netbeans.modules.dlight.terminal.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.api.terminal.TerminalSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

@ActionID(
        category = "Window", //NOI18N
        id = "org.netbeans.modules.dlight.terminal.action.OpenInTerminalAction" //NOI18N
)
@ActionRegistration(
        displayName = "#CTL_OpenInTerminalActionDescr",//NOI18N
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "UI/ToolActions/Files", position = 2050),
    @ActionReference(path = "Projects/Actions", position = 100),
    @ActionReference(path = "Shortcuts", name = "SO-K")
})
public class OpenInTerminalAction extends NodeAction {

    private OpenInTerminalAction() {
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            /* Do nothing for now, we enable this action only on a single node */
            return;
        }
        for (Node node : activatedNodes) {
            Lookup lookup = node.getLookup();
            FileObject fo = lookup.lookup(FileObject.class);

            final String path = (fo.isFolder()) ? fo.getPath() : fo.getParent().getPath();
            ExecutionEnvironment env = null;
            try {
                FileSystem fileSystem = fo.getFileSystem();
                Method declaredMethod = fileSystem.getClass().getDeclaredMethod("getExecutionEnvironment"); // NOI18N
                if (declaredMethod != null) {
                    declaredMethod.setAccessible(true);
                    Object invoke = declaredMethod.invoke(fileSystem);
                    if (invoke instanceof ExecutionEnvironment) {
                        env = (ExecutionEnvironment) invoke;
                    }
                }
            } catch (FileStateInvalidException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            }
            if (env == null) {
                env = ExecutionEnvironmentFactory.getLocal();
            }

            final ExecutionEnvironment envFinal = env;

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    /* Terminal title is meaningless but it will be changed later anyway*/
                    TerminalSupport.openTerminal(envFinal.getDisplayName(), envFinal, path);
                }
            });
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            Lookup lookup = activatedNodes[0].getLookup();
            if (lookup.lookup(FileObject.class) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenInTerminalAction.class, "CTL_OpenInTerminalActionDescr");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
