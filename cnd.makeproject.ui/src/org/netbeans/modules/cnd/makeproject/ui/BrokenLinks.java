/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
