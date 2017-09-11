/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
