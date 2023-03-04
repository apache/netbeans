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

package org.netbeans.modules.java.api.common.queries;

import org.netbeans.modules.java.api.common.SourceRoots;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Default implementation of {@link MultipleRootsUnitTestForSourceQueryImplementation}.
 * @author Tomas Zezula
 */
class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;

    public UnitTestForSourceQueryImpl(SourceRoots sourceRoots, SourceRoots testRoots) {
        assert sourceRoots != null;
        assert testRoots != null;

        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
    }

    public URL[] findUnitTests(FileObject source) {
        return find(source, sourceRoots, testRoots);
    }

    public URL[] findSources(FileObject unitTest) {
        return find(unitTest, testRoots, sourceRoots);
    }

    private URL[] find(FileObject file, SourceRoots from, SourceRoots to) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        for (FileObject fromRoot : from.getRoots()) {
            if (fromRoot.equals(file)) {
                return to.getRootURLs();
            }
        }
        return null;
    }

}
