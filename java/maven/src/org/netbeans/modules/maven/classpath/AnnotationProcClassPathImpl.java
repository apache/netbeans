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
package org.netbeans.modules.maven.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.PluginPropertyUtils.PluginConfigPathParams;
import static org.netbeans.modules.maven.classpath.AbstractProjectClassPathImpl.getFile;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import static org.netbeans.spi.java.classpath.FlaggedClassPathImplementation.PROP_FLAGS;
import org.openide.util.Utilities;

/**
 * ClassPath that represents annotation processor path from plugin execution's configuration. Information
 * is loaded from the {@code comple} pr {@code testCompile} goals. If no configuration is defined at all, this
 * path will be empty.
 * 
 * @author sdedic
 */
final class AnnotationProcClassPathImpl extends AbstractProjectClassPathImpl implements FlaggedClassPathImplementation {
    private static final String COMPILER_ARTIFACT_ID = "maven-compiler-plugin"; // NOI18N
    private static final String COMPILER_GROUP_ID = "org.apache.maven.plugins"; // NOI18N
    private static final String GOAL_COMPILE = "compile"; // NOI18N
    private static final String GOAL_TEST_COMPILE = "testCompile"; // NOI18N
    private static final String PROPERTY_PATH = "annotationProcessorPaths"; // NOI18N
    private static final String PROPERTY_ITEM = "path"; // NOI18N
    
    private final boolean mainCompile;
    
    private volatile boolean incomplete;

    public AnnotationProcClassPathImpl(NbMavenProjectImpl proj, boolean mainCompile) {
        super(proj);
        this.mainCompile = mainCompile;
    }

    @Override
    URI[] createPath() {
        List<URI> lst = new ArrayList<>();
        boolean broken = getCompileArtifacts(getMavenProject(), 
                mainCompile ? GOAL_COMPILE : GOAL_TEST_COMPILE, getMavenProject().getOriginalMavenProject(), lst);
        if (incomplete != broken) {
            incomplete = broken;
            firePropertyChange(PROP_FLAGS, null, null);
        }
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
    }
    
    
    @Override
    public Set<ClassPath.Flag> getFlags() {
        return incomplete ?
            EnumSet.of(ClassPath.Flag.INCOMPLETE) :
            Collections.<ClassPath.Flag>emptySet();
    }

    static boolean getCompileArtifacts(NbMavenProjectImpl prjImpl, String goal, MavenProject mavenProject, List<URI> lst) {
        PluginConfigPathParams query = new PluginConfigPathParams(COMPILER_GROUP_ID, COMPILER_ARTIFACT_ID, 
                PROPERTY_PATH, PROPERTY_ITEM);
        query.setDefaultScope(Artifact.SCOPE_RUNTIME);
        query.setGoal(goal);
        List<ArtifactResolutionException> errorList = new ArrayList<>();
        List<Artifact> arts = Collections.emptyList();
        
        arts = PluginPropertyUtils.getPluginPathProperty(prjImpl, query, true, errorList);
        if (arts == null) {
            return false;
        }
        boolean broken = false;
        for (Artifact art : arts) {
            File f = getFile(art);
            if (f != null) {
                lst.add(Utilities.toURI(f));
                broken |= !f.exists();
            } else {
                //NOPMD   //null means dependencies were not resolved..
                broken = true;
            } 
        }
        return broken || !errorList.isEmpty();
    }
}
