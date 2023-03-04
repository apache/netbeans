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

package org.netbeans.modules.java.project;

import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Delegates {@link UnitTestForSourceQueryImplementation} to the project which
 * owns the binary file.
 */
@SuppressWarnings("deprecation")
@ServiceProviders({
    @ServiceProvider(service=MultipleRootsUnitTestForSourceQueryImplementation.class),
    @ServiceProvider(service=UnitTestForSourceQueryImplementation.class)
})
public class UnitTestForSourceQueryImpl implements UnitTestForSourceQueryImplementation, MultipleRootsUnitTestForSourceQueryImplementation {
    
    /** Default constructor for lookup. */
    public UnitTestForSourceQueryImpl() {
    }
    
    public URL findUnitTest(FileObject source) {
        Project project = FileOwnerQuery.getOwner(source);
        if (project != null) {
            UnitTestForSourceQueryImplementation query = project.getLookup().lookup(UnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findUnitTest(source);
            }
        }
        return null;
    }

    public URL[] findUnitTests(FileObject source) {
        Project project = FileOwnerQuery.getOwner(source);
        if (project != null) {
            MultipleRootsUnitTestForSourceQueryImplementation query = project.getLookup().lookup(MultipleRootsUnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findUnitTests(source);
            }
        }
        return null;
    }

    public URL findSource(FileObject unitTest) {
        Project project = FileOwnerQuery.getOwner(unitTest);
        if (project != null) {
            UnitTestForSourceQueryImplementation query = project.getLookup().lookup(UnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findSource(unitTest);
            }
        }
        return null;
    }

    public URL[] findSources(FileObject unitTest) {
        Project project = FileOwnerQuery.getOwner(unitTest);
        if (project != null) {
            MultipleRootsUnitTestForSourceQueryImplementation query = project.getLookup().lookup(MultipleRootsUnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findSources(unitTest);
            }
        }
        return null;
    }

}
