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
package org.netbeans.modules.cnd.makeproject.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.util.NbBundle;

/**
 *
 */
public class BrokenLinks {
    
    public static List<BrokenLink> getBrokenLinks(Project project) throws MissingResourceException {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        List<BrokenLink> errs = new ArrayList<>();
        if (pdp.gotDescriptor()) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            MakeConfiguration activeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
            if (activeConfiguration != null) {
                CompilerSet2Configuration csconf = activeConfiguration.getCompilerSet();
                CompilerSet cs = csconf.getCompilerSet();
                String csname = csconf.getOption();
                if (cs == null || cs.getDirectory() == null || cs.getDirectory().isEmpty()) {
                    errs.add(new BrokenToolCollection(project, csname));
                }
            }
        }
        return errs;
    }
    
    public interface BrokenLink {
        String getProblem();
        List<Solution> getSolutions();
    }

    public interface Solution {
        String getDescription();
        Runnable resolve();
    }
    
    private static final class ChangeProjectProperties implements Solution {
        private final Project project;
        private final String name;
        
        private ChangeProjectProperties(Project project, String name) {
            this.project = project;
            this.name = name;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(BrokenLinks.class, "Link_Solution_UnknownCompiler1", name); // NOI18N
        }

        @Override
        public Runnable resolve() {
            return () -> {
                MakeCustomizerProvider cp = project.getLookup().lookup(MakeCustomizerProvider.class);
                if (cp == null) {
                    return;
                }
                cp.showCustomizer("Build"); // NOI18N
            };
        }
    }
    
    private static final class AddToolCollection implements Solution {
        private final String name;
        
        private AddToolCollection(String name) {
            this.name = name;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(BrokenLinks.class, "Link_Solution_UnknownCompiler2", name); // NOI18N
        }

        @Override
        public Runnable resolve() {
            return () -> {
                OptionsDisplayer.getDefault().open("CPlusPlus/ToolsTab"); // NOI18N
            };
        }
    }

    private static final class BrokenToolCollection implements BrokenLink {
        private final String name;
        private final List<Solution> solutions = new ArrayList<>();
        
        private BrokenToolCollection(Project project, String name) {
            this.name = name;
            solutions.add(new ChangeProjectProperties(project, name));
            solutions.add(new AddToolCollection(name));
        }

        @Override
        public String getProblem() {
            return NbBundle.getMessage(BrokenLinks.class, "Link_Problem_UnknownCompiler", name); // NOI18N
        }

        @Override
        public List<Solution> getSolutions() {
            return solutions;
        }
    }
}
