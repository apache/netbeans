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

import java.net.URI;
import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.java.queries.support.SourceForBinaryQueryImpl2Base;

/**
 * Finds sources corresponding to binaries.
 * Assumes an instance of SourceForBinaryQueryImplementation is in project's lookup.
 * @author Jesse Glick, Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class, position=100)
public class ProjectSourceForBinaryQuery extends SourceForBinaryQueryImpl2Base {
    
    /** Default constructor for lookup. */
    public ProjectSourceForBinaryQuery() {}

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        Project project = FileOwnerQuery.getOwner(URI.create(binaryRoot.toString()));
        if (project != null) {
            SourceForBinaryQueryImplementation sfbqi = project.getLookup().lookup(SourceForBinaryQueryImplementation.class);
            if (sfbqi != null) {
                return sfbqi.findSourceRoots(binaryRoot);
            }
        }
        return null;
    }

    
    public Result findSourceRoots2(URL binaryRoot) {
        Project project = FileOwnerQuery.getOwner(URI.create(binaryRoot.toString()));
        if (project != null) {
            SourceForBinaryQueryImplementation sfbqi = project.getLookup().lookup(SourceForBinaryQueryImplementation.class);
            if (sfbqi != null) {
                if (sfbqi instanceof SourceForBinaryQueryImplementation2) {
                    return ((SourceForBinaryQueryImplementation2)sfbqi).findSourceRoots2(binaryRoot);
                }
                else {
                    final SourceForBinaryQuery.Result result = sfbqi.findSourceRoots(binaryRoot);
                    if (result != null) {
                        return asResult (result);
                    }
                }
            }
        }
        return null;
    }
    
}
