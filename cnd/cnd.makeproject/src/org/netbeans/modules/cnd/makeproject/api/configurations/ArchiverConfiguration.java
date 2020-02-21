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

import java.util.Locale;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

public class ArchiverConfiguration implements AllOptionsProvider, Cloneable {

    private MakeConfiguration makeConfiguration;
    private StringConfiguration output;
    private NamedBooleanConfiguration runRanlib;
    private BooleanConfiguration replaceOption;
    private BooleanConfiguration verboseOption;
    private BooleanConfiguration supressOption;
    private OptionsConfiguration commandLineConfiguration;
    private OptionsConfiguration additionalDependencies;
    private StringConfiguration tool;
    private StringConfiguration ranlibTool;

    // Constructors
    public ArchiverConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        output = new StringConfiguration(null, ""); // NOI18N
        runRanlib = new NamedBooleanConfiguration(true, "", "$(RANLIB)"); // NOI18N
        replaceOption = new BooleanConfiguration(true);
        verboseOption = new BooleanConfiguration(true);
        supressOption = new BooleanConfiguration(false);
        commandLineConfiguration = new OptionsConfiguration();
        additionalDependencies = new OptionsConfiguration();
        tool = new StringConfiguration(null, "ar"); // NOI18N
        ranlibTool = new StringConfiguration(null, "ranlib"); // NOI18N
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    // Output
    public void setOutput(StringConfiguration output) {
        this.output = output;
    }

    public StringConfiguration getOutput() {
        return output;
    }

    // RunRanlib
    public void setRunRanlib(NamedBooleanConfiguration runRanlib) {
        this.runRanlib = runRanlib;
    }

    public NamedBooleanConfiguration getRunRanlib() {
        return runRanlib;
    }

    // Replace
    public void setReplaceOption(BooleanConfiguration replaceOption) {
        this.replaceOption = replaceOption;
    }

    public BooleanConfiguration getReplaceOption() {
        return replaceOption;
    }

    // Verbose
    public void setVerboseOption(BooleanConfiguration verboseOption) {
        this.verboseOption = verboseOption;
    }

    public BooleanConfiguration getVerboseOption() {
        return verboseOption;
    }

    // Supress
    public void setSupressOption(BooleanConfiguration supressOption) {
        this.supressOption = supressOption;
    }

    public BooleanConfiguration getSupressOption() {
        return supressOption;
    }

    // CommandLine
    public OptionsConfiguration getCommandLineConfiguration() {
        return commandLineConfiguration;
    }

    public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
        this.commandLineConfiguration = commandLineConfiguration;
    }

    // Additional Dependencies
    public OptionsConfiguration getAdditionalDependencies() {
        return additionalDependencies;
    }

    public void setAdditionalDependencies(OptionsConfiguration additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    // Tool
    public void setTool(StringConfiguration tool) {
        this.tool = tool;
    }

    public StringConfiguration getTool() {
        return tool;
    }

    // ranlib tool
    public void setRanlibTool(StringConfiguration ranlibTool) {
        this.ranlibTool = ranlibTool;
    }

    public StringConfiguration getRanlibTool() {
        return ranlibTool;
    }

    // Clone and assign
    public void assign(ArchiverConfiguration conf) {
        // ArchiverConfiguration
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getOutput().assign(conf.getOutput());
        getRunRanlib().assign(conf.getRunRanlib());
        getReplaceOption().assign(conf.getReplaceOption());
        getVerboseOption().assign(conf.getVerboseOption());
        getSupressOption().assign(conf.getSupressOption());
        getAdditionalDependencies().assign(conf.getAdditionalDependencies());
        getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
        getTool().assign(conf.getTool());
        getRanlibTool().assign(conf.getRanlibTool());
    }

    @Override
    public ArchiverConfiguration clone() {
        ArchiverConfiguration clone = new ArchiverConfiguration(getMakeConfiguration());
        // ArchiverConfiguration
        clone.setOutput(getOutput().clone());
        clone.setRunRanlib(getRunRanlib().clone());
        clone.setReplaceOption(getReplaceOption().clone());
        clone.setVerboseOption(getVerboseOption().clone());
        clone.setSupressOption(getSupressOption().clone());
        clone.setAdditionalDependencies(getAdditionalDependencies().clone());
        clone.setCommandLineConfiguration(getCommandLineConfiguration().clone());
        clone.setTool(getTool().clone());
        clone.setRanlibTool(getRanlibTool().clone());
        return clone;
    }

    // Interface OptionsProvider
    public String getOptions() {
        String options = getAllOptions(false) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        options += getOutputValue() + " "; // NOI18N
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    @Override
    public String getAllOptions(Tool tool) {
        return getAllOptions(true);
    }

    private String getAllOptions(boolean includeOutput) {
        StringBuilder options = new StringBuilder();

        options.append(getReplaceOption().getValue() ? "r" : ""); // NOI18N
        options.append(getVerboseOption().getValue() ? "v" : ""); // NOI18N
        options.append(getSupressOption().getValue() ? "c" : ""); // NOI18N
        if (options.length() > 0) {
            options.insert(0, "-"); // NOI18N
        }
        options.append(" "); // NOI18N
        if (includeOutput) {
            options.append(getOutputValue());  // NOI18N
            options.append(" ");  // NOI18N
        }
        return MakeProjectOptionsFormat.reformatWhitespaces(options.toString());
    }

    public String getOutputValue() {
        if (getOutput().getModified()) {
            return getOutput().getValue();
        } else {
            return getOutputDefault();
        }
    }

    private String getOutputDefault() {
        String outputName = CndPathUtilities.getBaseName(getMakeConfiguration().getBaseDir()).toLowerCase(Locale.getDefault());
        switch (getMakeConfiguration().getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_STATIC_LIB:
                outputName = "lib" + outputName + ".a"; // NOI18N
                break;
        }
        outputName = ConfigurationSupport.makeNameLegal(outputName);
        return MakeConfiguration.CND_DISTDIR_MACRO + "/" + MakeConfiguration.CND_CONF_MACRO + "/" + MakeConfiguration.CND_PLATFORM_MACRO + "/" + outputName; // UNIX path // NOI18N
    }

    /*
     * Default output pre version 28
     */
    public String getOutputDefault27() {
        String outputName = CndPathUtilities.getBaseName(getMakeConfiguration().getBaseDir()).toLowerCase(Locale.getDefault());
        outputName = "lib" + outputName + ".a"; // NOI18N
        return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + outputName; // UNIX path // NOI18N
    }
}
