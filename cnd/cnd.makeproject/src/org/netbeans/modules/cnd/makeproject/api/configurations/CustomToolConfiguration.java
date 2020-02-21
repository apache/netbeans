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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.openide.util.NbBundle;

public class CustomToolConfiguration implements ConfigurationBase, Cloneable {
    // Custom tool
    private StringConfiguration commandLine;
    private StringConfiguration description;
    private StringConfiguration outputs;
    private StringConfiguration additionalDependencies;

    public CustomToolConfiguration() {
        // Custom Tool
        commandLine = new StringConfiguration(null, ""); // NOI18N
        description = new StringConfiguration(null, getString("PerformingStepTxt"));
        outputs = new StringConfiguration(null, ""); // NOI18N
        additionalDependencies = new StringConfiguration(null, ""); // NOI18N
    }

    @Override
    public boolean getModified() {
        return commandLine.getModified() || description.getModified() || outputs.getModified() || additionalDependencies.getModified();
    }

    public void setCommandLine(StringConfiguration commandLine) {
        this.commandLine = commandLine;
    }

    public StringConfiguration getCommandLine() {
        return commandLine;
    }

    public void setDescription(StringConfiguration description) {
        this.description = description;
    }

    public StringConfiguration getDescription() {
        return description;
    }

    public void setOutputs(StringConfiguration outputs) {
        this.outputs = outputs;
    }

    public StringConfiguration getOutputs() {
        return outputs;
    }

    public void setAdditionalDependencies(StringConfiguration additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    public StringConfiguration getAdditionalDependencies() {
        return additionalDependencies;
    }

    public void assign(CustomToolConfiguration conf) {
        getCommandLine().assign(conf.getCommandLine());
        getDescription().assign(conf.getDescription());
        getOutputs().assign(conf.getOutputs());
        getAdditionalDependencies().assign(conf.getAdditionalDependencies());
    }

    @Override
    public CustomToolConfiguration clone() {
        CustomToolConfiguration i = new CustomToolConfiguration();
        i.setCommandLine(getCommandLine().clone());
        i.setDescription(getDescription().clone());
        i.setOutputs(getOutputs().clone());
        i.setAdditionalDependencies(getAdditionalDependencies().clone());
        return i;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CustomToolConfiguration.class, s);
    }
}
