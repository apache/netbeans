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
import org.netbeans.modules.dlight.sendto.api.ConfigurationPanel;
import org.netbeans.modules.dlight.sendto.api.ConfigurationsRegistry;
import org.netbeans.modules.dlight.sendto.api.OutputMode;
import org.netbeans.modules.dlight.sendto.conifg.ui.OutputConfigurationPanel;
import org.netbeans.modules.dlight.sendto.conifg.ui.ScriptPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.TreeSet;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 *
 */
public final class DefaultConfigurationPanel extends ConfigurationPanel {

    private final ScriptPanel scriptPanel;
    private final ScriptPanel validationScriptPanel;
    private final OutputConfigurationPanel outputConfigPanel;

    public DefaultConfigurationPanel() {
        TreeSet<String> executors = new TreeSet<String>();

        for (Configuration configuration : ConfigurationsRegistry.getConfigurations()) {
            executors.add(configuration.get(DefaultScriptHandler.SCRIPT_EXECUTOR));
            executors.add(configuration.get(DefaultScriptHandler.VALIDATION_SCRIPT_EXECUTOR));
        }

        scriptPanel = new ScriptPanel(
                NbBundle.getMessage(DefaultConfigurationPanel.class,"DefaultConfigurationPanel.scriptPanel.label.text"), // NOI18N
                NbBundle.getMessage(DefaultConfigurationPanel.class, "DefaultConfigurationPanel.scriptPanel.scriptFld.toolTipText")); // NOI18N
        validationScriptPanel = new ScriptPanel(
                NbBundle.getMessage(DefaultConfigurationPanel.class, "DefaultConfigurationPanel.validationScriptPanel.label.text"), // NOI18N
                NbBundle.getMessage(DefaultConfigurationPanel.class, "DefaultConfigurationPanel.validationScriptPanel.scriptFld.toolTipText")); // NOI18N
        outputConfigPanel = new OutputConfigurationPanel();

        String[] executorsArray = executors.toArray(new String[executors.size()]);
        scriptPanel.setExecutors(executorsArray);
        validationScriptPanel.setExecutors(executorsArray);

        JPanel scriptsPanel = new JPanel(new GridLayout(2, 1, 0, 7));
        scriptsPanel.add(scriptPanel);
        scriptsPanel.add(validationScriptPanel);

        setLayout(new BorderLayout(0, 7));

        add(scriptsPanel, BorderLayout.CENTER);
        outputConfigPanel.setMinimumSize(new Dimension(100, 30));
        add(outputConfigPanel, BorderLayout.SOUTH);
    }

    @Override
    public void updatePanel(Configuration cfg) {
        scriptPanel.setScript(cfg.get(DefaultScriptHandler.SCRIPT));
        scriptPanel.setExecutor(cfg.get(DefaultScriptHandler.SCRIPT_EXECUTOR));
        validationScriptPanel.setScript(cfg.get(DefaultScriptHandler.VALIDATION_SCRIPT));
        validationScriptPanel.setExecutor(cfg.get(DefaultScriptHandler.VALIDATION_SCRIPT_EXECUTOR));
        OutputMode mode = OutputMode.parse(cfg.get(DefaultScriptHandler.OUTPUT_MODE));
        outputConfigPanel.setOutputMode(mode);
    }

    @Override
    public void updateConfig(Configuration cfg) {
        cfg.set(DefaultScriptHandler.SCRIPT, scriptPanel.getScript());
        cfg.set(DefaultScriptHandler.SCRIPT_EXECUTOR, scriptPanel.getExecutor());
        cfg.set(DefaultScriptHandler.VALIDATION_SCRIPT, validationScriptPanel.getScript());
        cfg.set(DefaultScriptHandler.VALIDATION_SCRIPT_EXECUTOR, validationScriptPanel.getExecutor());
        cfg.set(DefaultScriptHandler.OUTPUT_MODE, outputConfigPanel.getOutputMode().name());
    }
}
