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
            return ClassPathSupport.createProxyClassPath(list.toArray(new ClassPath[0]));
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
