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

import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.util.Exceptions;

/**
 * Finds binary roots corresponding to source roots.
 * Assumes an instance of BinaryForSourceQueryImplementation is in project's lookup.
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation.class, position=100)
public class ProjectBinaryForSourceQuery implements BinaryForSourceQueryImplementation {
    
    /** Creates a new instance of ProjectBinaryForSourceQuery */
    public ProjectBinaryForSourceQuery() {
    }
    
    public Result findBinaryRoots(URL sourceRoot) {
        try {
            Project p = FileOwnerQuery.getOwner(sourceRoot.toURI());
            if (p != null) {
                BinaryForSourceQueryImplementation impl = p.getLookup().
                    lookup(BinaryForSourceQueryImplementation.class);
                if (impl != null) {
                    return impl.findBinaryRoots(sourceRoot);
                }
            }
        } catch (URISyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

}
