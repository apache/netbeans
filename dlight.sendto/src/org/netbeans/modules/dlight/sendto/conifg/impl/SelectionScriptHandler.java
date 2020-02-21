/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.sendto.conifg.impl;

import org.netbeans.modules.dlight.sendto.api.Configuration;
import org.netbeans.modules.dlight.sendto.action.FutureAction;
import org.netbeans.modules.dlight.sendto.api.OutputMode;
import org.netbeans.modules.dlight.sendto.api.ScriptsRegistry;
import org.netbeans.modules.dlight.sendto.spi.Handler;
import org.netbeans.modules.dlight.sendto.util.ScriptExecutor;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.dlight.sendto.util.Utils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = Handler.class)
public class SelectionScriptHandler extends Handler<SelectionConfigurationPanel> {

    public SelectionScriptHandler() {
        super("SelectionScriptHandler"); // NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(DefaultScriptHandler.class, "SelectionScriptHandler.description.text"); // NOI18N
    }

    @Override
    protected SelectionConfigurationPanel createConfigurationPanel() {
        return new SelectionConfigurationPanel();
    }

    @Override
    public FutureAction createActionFor(final Lookup actionContext, final Configuration cfg) {
        DataEditorSupport des = actionContext.lookup(DataEditorSupport.class);
        if (des == null) {
            return null;
        }
        
        StyledDocument doc = des.getDocument();
        if (doc == null) {
            return null;
        }
        
        JTextComponent focusedComponent = EditorRegistry.findComponent(doc);
        if (focusedComponent == null) {
            return null;
        }

        final String selection = focusedComponent.getSelectedText();

        if (selection == null || selection.isEmpty()) {
            return null;
        }

        return new FutureAction(new Callable<Action>() {

            @Override
            public Action call() throws Exception {
                return new AbstractAction(cfg.getName()) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startScript(cfg, selection);
                    }
                };
            }
        });
    }

    private void startScript(final Configuration cfg, final String selection) {
        String script = cfg.get(DefaultScriptHandler.SCRIPT);

        if (script.trim().isEmpty()) {
            return;
        }
        final ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();

        String scriptFile = ScriptsRegistry.getScriptFile(cfg, env, script);

        if (scriptFile == null) {
            return;
        }

        String scriptExecutor = Utils.substituteShell(cfg.get(DefaultScriptHandler.SCRIPT_EXECUTOR).trim(), env);

        List<String> cmd = new ArrayList<String>(2);
        cmd.add(scriptExecutor);
        cmd.add(scriptFile);

        ScriptExecutor executor = new ScriptExecutor(cmd);
        executor.writeToProcessOnRun(selection);
        executor.execute(cfg.getName(), OutputMode.parse(cfg.get(DefaultScriptHandler.OUTPUT_MODE)));
    }

    @Override
    public void applyChanges(Configuration cfg) {
        if (cfg.isModified()) {
            ScriptsRegistry.invalidate(cfg);
        }
    }
}
