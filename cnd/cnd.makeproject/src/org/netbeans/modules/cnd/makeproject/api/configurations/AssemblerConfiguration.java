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

import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;

public class AssemblerConfiguration extends BasicCompilerConfiguration implements Cloneable {
    // Constructors

    public AssemblerConfiguration(String baseDir, AssemblerConfiguration master, MakeConfiguration owner) {
        super(baseDir, master, owner);
    }

    // Clone and assign
    public void assign(AssemblerConfiguration conf) {
        // From XCompiler
        super.assign(conf);
    }

    @Override
    public AssemblerConfiguration clone() {
        AssemblerConfiguration clone = new AssemblerConfiguration(getBaseDir(), (AssemblerConfiguration) getMaster(), getOwner());
        // BasicCompilerConfiguration
        clone.setDevelopmentMode(getDevelopmentMode().clone());
        clone.setWarningLevel(getWarningLevel().clone());
        clone.setSixtyfourBits(getSixtyfourBits().clone());
        clone.setStrip(getStrip().clone());
        clone.setAdditionalDependencies(getAdditionalDependencies().clone());
        clone.setTool(getTool().clone());
        clone.setCommandLineConfiguration(getCommandLineConfiguration().clone());
        return clone;
    }

    // Interface OptionsProvider
    @Override
    public String getOptions(AbstractCompiler compiler) {
        StringBuilder options = new StringBuilder("$(AS) $(ASFLAGS) "); // NOI18N
        options.append(getAllOptions2(compiler)).append(' '); // NOI18N
        options.append(getCommandLineOptions(true));
        return MakeProjectOptionsFormat.reformatWhitespaces(options.toString());
    }

    public String getAsFlagsBasic(AbstractCompiler compiler) {
        String options = ""; // NOI18N
        options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " "; // NOI18N
        if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST) {
            options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
        }
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    public String getAsFlags(AbstractCompiler compiler) {
        String options = getAsFlagsBasic(compiler) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }

    @Override
    public String getAllOptions(Tool tool) {
        if (!(tool instanceof AbstractCompiler)) {
            return "";
        }
        AbstractCompiler compiler = (AbstractCompiler) tool;

        StringBuilder options = new StringBuilder();
        options.append(getAsFlagsBasic(compiler)).append(' ');
        getMasters(true).forEach((master) -> {
            options.append(master.getCommandLineConfiguration().getValue()).append(' ');
        });
        options.append(getAllOptions2(compiler)).append(' ');
        return MakeProjectOptionsFormat.reformatWhitespaces(options.toString());
    }

    public String getAllOptions2(AbstractCompiler compiler) {
        String options = ""; // NOI18N
        options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " "; // NOI18N
        options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " "; // NOI18N
        return MakeProjectOptionsFormat.reformatWhitespaces(options);
    }
}
