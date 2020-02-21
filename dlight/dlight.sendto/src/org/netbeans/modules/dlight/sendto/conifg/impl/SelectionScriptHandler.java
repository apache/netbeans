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
