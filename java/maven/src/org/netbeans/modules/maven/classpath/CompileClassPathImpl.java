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

package org.netbeans.modules.maven.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
class CompileClassPathImpl extends AbstractProjectClassPathImpl implements FlaggedClassPathImplementation {
    private static final Logger LOGGER = Logger.getLogger(CompileClassPathImpl.class.getName());
    private volatile boolean incomplete;
    private final boolean addOutputDir;

    /** Creates a new instance of SrcClassPathImpl */
    public CompileClassPathImpl(NbMavenProjectImpl proj, boolean addOutputDir) {        
        super(proj);
        this.addOutputDir = addOutputDir;
    }
    
    @Override
    URI[] createPath() {
        List<URI> lst = new ArrayList<>();
        boolean broken = getCompileArtifacts(getMavenProject().getOriginalMavenProject(), lst);
        MavenProject mp = getMavenProject().getOriginalMavenProjectOrNull();
        LOGGER.log(Level.FINER, "{0} for project {1}: creating path for {2}: size {4} - {3}", 
                new Object[] { getClass(), getMavenProject(), System.identityHashCode(mp == null ? this : mp), lst, lst.size() });
        if(addOutputDir) {
            lst.add(Utilities.toURI(getProject().getProjectWatcher().getOutputDirectory(false)));
        }
        
        if (incomplete != broken) {
            incomplete = broken;
            firePropertyChange(PROP_FLAGS, null, null);
        }
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
    }

    static boolean getCompileArtifacts(MavenProject mavenProject, List<URI> lst) {
        // according the current 2.1 sources this is almost the same as getCompileClasspath()
        //except for the fact that multiproject references are not redirected to their respective
        // output folders.. we lways retrieve stuff from local repo..
        List<Artifact> arts = mavenProject.getCompileArtifacts();
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
        return broken;
    }

    @Override
    public Set<ClassPath.Flag> getFlags() {
        return incomplete ?
            EnumSet.of(ClassPath.Flag.INCOMPLETE) :
            Collections.<ClassPath.Flag>emptySet();
    }
}
