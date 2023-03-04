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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.maven.NbMavenProjectImpl;

/**
 *
 * @author  Milos Kleint 
 */
class TestSourceClassPathImpl extends AbstractProjectClassPathImpl {
    
    /**
     * Creates a new instance of TestSourceClassPathImpl
     */
    public TestSourceClassPathImpl(NbMavenProjectImpl proj) {
        super(proj);
        proj.getProjectWatcher().addWatchedPath("target/generated-test-sources");
        proj.getProjectWatcher().addWatchedPath("target/generated-sources"); // MCOMPILER-167
    }
    
    @Override
    URI[] createPath() {
        Collection<URI> col = new ArrayList<URI>();
        col.addAll(Arrays.asList(getMavenProject().getSourceRoots(true)));
        col.addAll(Arrays.asList(getMavenProject().getGeneratedSourceRoots(true)));
        //#180020 remote items from resources that are either duplicate or child roots of source roots.
        List<URI> resources = new ArrayList<URI>(Arrays.asList(getMavenProject().getResources(true)));
        Iterator<URI> it = resources.iterator();
        while (it.hasNext()) {
            URI res = it.next();
            for (URI srcs : col) {
                if (res.toString().startsWith(srcs.toString())) {
                    it.remove();
                }
            }
        }
        col.addAll(resources);
        
        URI[] uris = new URI[col.size()];
        uris = col.toArray(uris);
        return uris;        
    }
    
}
