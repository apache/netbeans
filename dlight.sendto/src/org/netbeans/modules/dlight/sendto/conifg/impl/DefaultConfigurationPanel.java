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
