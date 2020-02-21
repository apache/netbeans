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

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.AssemblerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;

class ProxyItemConfiguration extends ItemConfiguration {

    private AssemblerConfiguration assemblerConfiguration;
    private CCCompilerConfiguration cCCompilerConfiguration;
    private CCompilerConfiguration cCompilerConfiguration;
    private FortranCompilerConfiguration fortranCompilerConfiguration;
    private CustomToolConfiguration customToolConfiguration;

    static ProxyItemConfiguration proxyFactory(Configuration configuration, Item item){
        ItemConfiguration itemConfiguration = item.getItemConfiguration(configuration);
        if (itemConfiguration != null) {
            return new ProxyItemConfiguration(itemConfiguration);
        }
        return null;
    }

    private ProxyItemConfiguration(ItemConfiguration itemConfiguration) {
        super(itemConfiguration);
    }

    @Override
    public synchronized AssemblerConfiguration getAssemblerConfiguration() {
        if (getTool() == PredefinedToolKind.Assembler) {
            return super.getAssemblerConfiguration();
        } else {
            if (assemblerConfiguration == null) {
                MakeConfiguration conf = (MakeConfiguration) getConfiguration();
                assemblerConfiguration = new AssemblerConfiguration(conf.getBaseDir(), conf.getAssemblerConfiguration(), conf);
            }
            return assemblerConfiguration;
        }
    }

    @Override
    public synchronized CCCompilerConfiguration getCCCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.CCCompiler) {
            return super.getCCCompilerConfiguration();
        } else {
            if (cCCompilerConfiguration == null) {
                MakeConfiguration conf = (MakeConfiguration) getConfiguration();
                FolderConfiguration folderConfiguration = getItem().getFolder().getFolderConfiguration(getConfiguration());
                if (folderConfiguration != null) {
                    cCCompilerConfiguration = new CCCompilerConfiguration(conf.getBaseDir(), folderConfiguration.getCCCompilerConfiguration(), conf);
                } else {
                    cCCompilerConfiguration = new CCCompilerConfiguration(conf.getBaseDir(), null, conf);
                }
            }
            return cCCompilerConfiguration;
        }
    }

    @Override
    public CCompilerConfiguration getCCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.CCompiler) {
            return super.getCCompilerConfiguration();
        } else {
            MakeConfiguration conf = (MakeConfiguration) getConfiguration();
            FolderConfiguration folderConfiguration = getItem().getFolder().getFolderConfiguration(getConfiguration());
            if (folderConfiguration != null) {
                cCompilerConfiguration = new CCompilerConfiguration(conf.getBaseDir(), folderConfiguration.getCCompilerConfiguration(), conf);
            } else {
                cCompilerConfiguration = new CCompilerConfiguration(conf.getBaseDir(), null, conf);
            }
            return cCompilerConfiguration;
        }
    }

    @Override
    public synchronized FortranCompilerConfiguration getFortranCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.FortranCompiler) {
            return super.getFortranCompilerConfiguration();
        } else {
            if (fortranCompilerConfiguration == null) {
                MakeConfiguration conf = (MakeConfiguration) getConfiguration();
                fortranCompilerConfiguration = new FortranCompilerConfiguration(conf.getBaseDir(),conf.getFortranCompilerConfiguration(), conf);
            }
            return fortranCompilerConfiguration;
        }
    }

    @Override
    public synchronized CustomToolConfiguration getCustomToolConfiguration() {
        if (getTool() == PredefinedToolKind.CustomTool || isProCFile()) {
            return super.getCustomToolConfiguration();
        } else {
            if (customToolConfiguration == null) {
                customToolConfiguration = new CustomToolConfiguration();
            }
            return customToolConfiguration;
        }
    }
}
