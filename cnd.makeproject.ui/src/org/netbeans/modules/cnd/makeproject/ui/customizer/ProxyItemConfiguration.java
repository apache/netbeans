/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
