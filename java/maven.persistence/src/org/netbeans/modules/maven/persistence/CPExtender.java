/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.maven.persistence;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=ProjectClassPathModifierImplementation.class,
projectType="org-netbeans-modules-maven")
public class CPExtender extends ProjectClassPathModifierImplementation {
    private static final String SL_15 = "1.5"; //NOI18N
    private Project project;
    /** Creates a new instance of CPExtender */
    public CPExtender(Project project) {
        this.project = project;
    }
    
    @Override
    protected SourceGroup[] getExtensibleSourceGroups() {
        //the default one privides them.
        return new SourceGroup[0];
    }

    @Override
    protected String[] getExtensibleClassPathTypes(SourceGroup arg0) {
        return new String[0];
    }

    @Override
    protected boolean addLibraries(Library[] libs, SourceGroup arg1, String arg2) throws IOException,
                                                                                         UnsupportedOperationException {
        boolean added = false;
        for (Library l : libs) {
            added = added || addLibrary(l);
        }
        return added;
    }

    @Override
    protected boolean removeLibraries(Library[] arg0, SourceGroup arg1,
                                      String arg2) throws IOException,
                                                          UnsupportedOperationException {
        return false;
    }

    @Override
    protected boolean addRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                 UnsupportedOperationException {
        return false;
    }

    @Override
    protected boolean removeRoots(URL[] arg0, SourceGroup arg1, String arg2) throws IOException,
                                                                                    UnsupportedOperationException {
        return false;
    }

    @Override
    protected boolean addAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                      SourceGroup arg2, String arg3) throws IOException,
                                                                            UnsupportedOperationException {
        return false;
    }

    @Override
    protected boolean removeAntArtifacts(AntArtifact[] arg0, URI[] arg1,
                                         SourceGroup arg2, String arg3) throws IOException,
                                                                               UnsupportedOperationException {
        return false;
    }

    private boolean addLibrary(Library library) throws IOException {
        if ("toplink".equals(library.getName())) { //NOI18N
            //TODO would be nice if the toplink lib shipping with netbeans be the same binary
            // then we could just copy the pieces to local repo.
            //not necessary any more. toplink will be handled by default library impl..            
            // checking source doesn't work anymore, the wizard requires the level to be 1.5 up front.
            String source = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, Constants.SOURCE_PARAM, "compile", "maven.compiler.source");
            if (source == null || source.matches("1[.][0-4]")) {
                Utilities.performPOMModelOperations(project.getProjectDirectory().getFileObject("pom.xml"), Collections.singletonList(new ModelOperation<POMModel>() {
                    @Override public void performOperation(POMModel model) {
                        ModelUtils.setSourceLevel(model, SL_15);
                    }
                }));
            }
        }
        //shall not return true, needs processing by the fallback impl as well.
        return false;
    }

    @Override protected boolean addRoots(URI[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        return false;
    }

    @Override protected boolean removeRoots(URI[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        return false;
    }

    @Override protected boolean addProjects(Project[] projects, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        return false;
    }
    
}
