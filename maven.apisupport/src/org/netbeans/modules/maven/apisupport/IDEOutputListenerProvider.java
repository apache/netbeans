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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.apisupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputUtils;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.windows.OutputListener;




/**
 * exec plugin output processing, just handle stacktraces.
 * @author  Milos Kleint 
 */
public class IDEOutputListenerProvider implements OutputProcessor {
    
    private static final String[] EXECGOALS = new String[] {
        "mojo-execute#nbm:run-ide", //NOI18N
        "mojo-execute#nbm:run-platform" //NOI18N
    };
    private final Project project;
    private ClassPath classpath;
    
    /** Creates a new instance of TestOutputListenerProvider */
    public IDEOutputListenerProvider(Project proj) {
        project = proj;
    }
    
    private ClassPath createCP(Project prj, HashSet<Project> parents) {
        parents.add(prj);
        List<ClassPath> list = new ArrayList<ClassPath>();
        ProjectSourcesClassPathProvider cpp = prj.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath[] cp = cpp.getProjectClassPaths(ClassPath.EXECUTE);
        list.addAll(Arrays.asList(cp));
        //for pom packaging projects subprojects/modules matter
        //TODO for application project it's DependencyProjectProvider, for pom project (run-ide?) it's containerprojectprovider
        SubprojectProvider spp = prj.getLookup().lookup(SubprojectProvider.class);
        if (spp != null) {
            for (Project sub : spp.getSubprojects()) {
                if (parents.contains(sub)) {
                    continue;
                }
                ClassPath c = createCP(sub, parents);
                if (c != null) {
                    list.add(c);
                }
            }
        }
        if (list.size() > 0) {
            return ClassPathSupport.createProxyClassPath(list.toArray(new ClassPath[list.size()]));
        }
        return null;
    }
    
    @Override
    public void processLine(String line, OutputVisitor visitor) {
        if (classpath == null) {
            return;
        }
        OutputListener list = OutputUtils.matchStackTraceLine(line, classpath);
        if (list != null) {
            visitor.setOutputListener(list);
        }
    }

    @Override
    public String[] getRegisteredOutputSequences() {
        return EXECGOALS;
    }

    @Override
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
        OutputVisitor.Context context = visitor.getContext();
        Project prj = project;
        if (context != null && context.getCurrentProject() != null) {
            prj = context.getCurrentProject();
        }        
        classpath = createCP(prj, new HashSet<Project>());
    }

    @Override
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        classpath = null;
    }
    
    @Override
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
        classpath = null;
    }
    
}
