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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private NbModuleProject project;

    public UnitTestForSourceQueryImpl(NbModuleProject project) {
        this.project = project;
    }

    public URL[] findUnitTests(FileObject source) {
        return find(source, "src.dir", "test.unit.src.dir"); // NOI18N
    }
    
    public URL[] findSources(FileObject unitTest) {
        return find(unitTest, "test.unit.src.dir", "src.dir"); // NOI18N
    }
    
    private URL[] find(FileObject file, String from, String to) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        AntProjectHelper helper = project.getHelper();
        String val = project.evaluator().getProperty(from);
        assert val != null : "No value for " + from + " in " + project;
        FileObject fromRoot = helper.resolveFileObject(val);
        if (!file.equals(fromRoot)) {
            return null;
        }
        val = project.evaluator().getProperty(to);
        assert val != null : "No value for " + to + " in " + project;
        try {
            File f = helper.resolveFile(val);
            if (! f.exists()) {
                // #143633: need not to exist, ensure proper URI ending with a slash
                URI u = Utilities.toURI(f);
                String path = u.getPath();
                if (! path.endsWith("/"))
                    path = path.concat("/");
                try {
                    u = new URI(u.getScheme(), u.getHost(), path, u.getFragment());
                    return new URL[] {u.toURL()};
                } catch (URISyntaxException ex) {
                    Logger.getLogger(UnitTestForSourceQueryImpl.class.getName())
                            .log(Level.WARNING, "Problems getting URI for " + f, ex);
                }
            }

            return new URL[] {Utilities.toURI(f).toURL()};
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
}
