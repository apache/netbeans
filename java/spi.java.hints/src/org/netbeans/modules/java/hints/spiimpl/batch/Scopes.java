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
package org.netbeans.modules.java.hints.spiimpl.batch;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.IndexEnquirer;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.MapIndices;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Scope;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public class Scopes {

    public static Scope allOpenedProjectsScope() {
        return new AllOpenedProjectsScope();
    }

    private static final class AllOpenedProjectsScope extends Scope {

        @Override
        public String getDisplayName() {
            return "All Opened Projects";
        }

        @Override
        public Collection<? extends Folder> getTodo() {
            Set<Folder> todo = new HashSet<>();

            for (ClassPath source : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                todo.addAll(Arrays.asList(Folder.convert(source.getRoots())));
            }

            return todo;
        }

        @Override
        public MapIndices getIndexMapper(Iterable<? extends HintDescription> hints) {
            return getDefaultIndicesMapper();
        }
    }

    public static Scope specifiedFoldersScope(Folder... roots) {
        return new SpecificFoldersScope(roots);
    }
    
    private static final class SpecificFoldersScope extends Scope {

        private final Collection<? extends Folder> roots;

        public SpecificFoldersScope(Folder... roots) {
            this.roots = Arrays.asList(roots);
        }

        @Override
        public String getDisplayName() {
            return "Specified Root";
        }

        @Override
        public Collection<? extends Folder> getTodo() {
            return roots;
        }

        @Override
        public MapIndices getIndexMapper(Iterable<? extends HintDescription> hints) {
            return getDefaultIndicesMapper();
        }
    }

    public static MapIndices getDefaultIndicesMapper() {
        return (FileObject root, ProgressHandleWrapper progress, boolean recursive) -> {
            IndexEnquirer e = findIndexEnquirer(root, progress, recursive);
            return e != null ? e : new BatchSearch.FileSystemBasedIndexEnquirer(root, recursive);
        };
    }
    
    public static IndexEnquirer findIndexEnquirer(FileObject root, ProgressHandleWrapper progress, boolean recursive) {
        for (MapIndices mi : Lookup.getDefault().lookupAll(MapIndices.class)) {
            IndexEnquirer r = mi.findIndex(root, progress, recursive);

            if (r != null) return r;
        }

        return null;
    }
}
