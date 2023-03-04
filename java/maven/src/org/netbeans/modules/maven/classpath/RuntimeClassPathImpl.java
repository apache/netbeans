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
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.util.Utilities;

/**
 * class path def for runtime..
 * @author  Milos Kleint
 */
public class RuntimeClassPathImpl extends AbstractProjectClassPathImpl {
    
    /** Creates a new instance of RuntimeClassPathImpl */
    public RuntimeClassPathImpl(NbMavenProjectImpl proj) {
        super(proj);
        
    }
    
    @Override
    URI[] createPath() {
        List<URI> lst = createPath(getMavenProject().getOriginalMavenProject());
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
    }

    public static List<URI> createPath(MavenProject prj) {
        assert prj != null;
        List<URI> lst = new ArrayList<URI>();
        Build build = prj.getBuild();
        if (build != null) {
            String outputDirectory = build.getOutputDirectory();
            if (outputDirectory != null) {
                lst.add(NbMavenProjectImpl.convertStringToUri(outputDirectory, true));
            }
        }
        List<Artifact> arts = prj.getRuntimeArtifacts();
        for (Artifact art : arts) {
            File f = getFile(art);
            if (f != null) {
                lst.add(Utilities.toURI(f));
            } else {
              //NOPMD   //null means dependencies were not resolved..
            }
        }
        return lst;
    }
}
