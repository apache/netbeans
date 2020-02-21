/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.simpleunit.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.builds.MakefileTargetProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 */
public class MakefileUtils {

    private MakefileUtils() {
    }

    public static FileObject getMakefile(Project project) {
        ConfigurationDescriptorProvider confDescriptorProvider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (confDescriptorProvider != null) {
            MakeConfigurationDescriptor makeConfDescriptor = confDescriptorProvider.getConfigurationDescriptor();
            if (makeConfDescriptor != null && !makeConfDescriptor.getProjectMakefileName().isEmpty()) {
                return project.getProjectDirectory().getFileObject(makeConfDescriptor.getProjectMakefileName());
            }
        }
        return null;
    }

    public static boolean hasTestTargets(Project project) {
        FileObject makefile = getMakefile(project);
        if(makefile != null && makefile.isValid()) {
            try {
                DataObject dataObject = DataObject.find(makefile);
                MakefileTargetProvider targetProvider = dataObject.getLookup().lookup(MakefileTargetProvider.class);
                if (targetProvider != null) {
                    Set<String> targets = targetProvider.getRunnableTargets();
                    return targets.contains("test") || targets.contains("build-tests"); // NOI18N
                }
            } catch (DataObjectNotFoundException ex) {
            } catch (IOException ex) {}        
        }
        return false;
    }

    public static void createTestTargets(Project project) {
        if (hasTestTargets(project)) {
            return;
        }
        FileObject makefile = getMakefile(project);
        StringBuilder makefiledata;
        try {
            makefiledata = new StringBuilder(makefile.asText());
            makefiledata.append("\n\n") // NOI18N
                .append("# build tests\n") // NOI18N
                .append("build-tests: .build-tests-post\n") // NOI18N
                .append("\n") // NOI18N
                .append(".build-tests-pre:\n") // NOI18N
                .append("# Add your pre 'build-tests' code here...\n") // NOI18N
                .append("\n") // NOI18N
                .append(".build-tests-post: .build-tests-impl\n") // NOI18N
                .append("# Add your post 'build-tests' code here...\n") // NOI18N
                .append("\n") // NOI18N
                .append("\n") // NOI18N
                .append("# run tests\n") // NOI18N
                .append("test: .test-post\n") // NOI18N
                .append("\n") // NOI18N
                .append(".test-pre:\n") // NOI18N
                .append("# Add your pre 'test' code here...\n") // NOI18N
                .append("\n") // NOI18N
                .append(".test-post: .test-impl\n") // NOI18N
                .append("# Add your post 'test' code here...\n"); // NOI18N
            OutputStream outputStream = makefile.getOutputStream();
            try {
                outputStream.write(makefiledata.toString().getBytes());
            } finally {
                outputStream.close();
            }
        } catch (IOException ex) {
        }
    }
}
