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
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint 
 */
class TestCompileClassPathImpl extends AbstractProjectClassPathImpl implements FlaggedClassPathImplementation {

    private volatile boolean incomplete;
    private final boolean addTestOutDir;
    private final boolean testScoped;

    /** Creates a new instance of SrcClassPathImpl */
    public TestCompileClassPathImpl(NbMavenProjectImpl proj, boolean addTestOutDir) {
        this(proj, addTestOutDir, false);
    }
    
    public TestCompileClassPathImpl(NbMavenProjectImpl proj, boolean addTestOutDir, boolean testScoped) {
        super(proj);
        this.addTestOutDir = addTestOutDir;        
        this.testScoped = testScoped;        
    }
    
    @Override
    URI[] createPath() {
        List<URI> lst = new ArrayList<>();
        MavenProject mavenProject = getMavenProject().getOriginalMavenProject();
        //TODO we shall add the test class output as well. how?
        // according the current 2.1 sources this is almost the same as getCompileClasspath()
        //except for the fact that multiproject references are not redirected to their respective
        // output folders.. we lways retrieve stuff from local repo..
        List<Artifact> arts = mavenProject.getTestArtifacts();
        boolean broken = false;
        for (Artifact art : arts) {
            File f = getFile(art);
            if (f != null) {
                lst.add(Utilities.toURI(f));
                broken |= !f.exists();
            } else { //NOPMD
                //null means dependencies were not resolved..
                broken = true;
            }
        }
        if(testScoped) {
            List<URI> cmplst = new ArrayList<>();
            broken |= CompileClassPathImpl.getCompileArtifacts(mavenProject, cmplst);
            lst.removeAll(cmplst);
        }
        if (incomplete != broken) {
            incomplete = broken;
            firePropertyChange(PROP_FLAGS, null, null);
        }
        if(addTestOutDir) {
            lst.add(0, Utilities.toURI(getMavenProject().getProjectWatcher().getOutputDirectory(true)));            
        }
        lst.add(0, Utilities.toURI(getMavenProject().getProjectWatcher().getOutputDirectory(false)));
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
}
