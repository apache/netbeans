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

import org.netbeans.modules.cnd.makeproject.api.configurations.CompileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.ComboStringNodeProp;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class CompileCustomizerNode extends CustomizerNode {

    public CompileCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        Sheet sheet = getSheet(((MakeConfiguration) configuration).getCompileConfiguration());
        return new Sheet[]{sheet};
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsCompile"); // NOI18N
    }

    private static String getString(String key) {
        return NbBundle.getMessage(CompileCustomizerNode.class, key);
    }

    private Sheet getSheet(CompileConfiguration cc) {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Compile"); // NOI18N
        set.setDisplayName(getString("CompileTxt")); // NOI18N
        set.setShortDescription(getString("CompileHint")); // NOI18N
        set.put(new ComboStringNodeProp(cc.getCompileCommandWorkingDir(), true, getString("CompileWorkingDirectory_LBL"), getString("CompileWorkingDirectory_TT"))); // NOI18N
        set.put(new ComboStringNodeProp(cc.getCompileCommand(), true, getString("CompileCommandLine_LBL"), getString("CompileCommandLine_TT"))); // NOI18N
        sheet.put(set);
        
        return sheet;
    }
}
