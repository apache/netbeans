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

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.ui.configurations.RequiredProjectsNodeProp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

class RequiredProjectsCustomizerNode extends CustomizerNode {

    public RequiredProjectsCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }

    @Override
    public Sheet[] getSheets(Configuration configuration) {
        Sheet generalSheet = getRequiredProjectsSheet(getContext().getProject(), (MakeConfiguration) configuration);
        return new Sheet[]{generalSheet};
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectPropsRequiredProjects"); // NOI18N
    }
    
    private static String getString(String s) {
        return NbBundle.getMessage(RequiredProjectsCustomizerNode.class, s);
    }
    
    private Sheet getRequiredProjectsSheet(Project project, MakeConfiguration conf) {
        Sheet sheet = new Sheet();
        String[] texts = new String[]{getString("ProjectsTxt1"), getString("ProjectsHint"), getString("ProjectsTxt2"), getString("AllOptionsTxt2")};

        Sheet.Set set2 = new Sheet.Set();
        set2.setName("Projects"); // NOI18N
        set2.setDisplayName(getString("ProjectsTxt1"));
        set2.setShortDescription(getString("ProjectsHint"));
        set2.put(new RequiredProjectsNodeProp(conf.getRequiredProjectsConfiguration(), project, conf, conf.getBaseFSPath(), texts));
        sheet.put(set2);

        return sheet;
    }
}
