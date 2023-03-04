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
package org.netbeans.spi.search.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class SimpleSearchInfoDefinitionTest extends NbTestCase {

    public SimpleSearchInfoDefinitionTest(String name) {
        super(name);
    }

    /**
     * When creating a SimpleSearchInfoDefinition with some filters that permit
     * searching in the root file, these filters should be removed.
     */
    public void testNiceFilters() throws IOException {

        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject f1 = root.createData("file.txt");
        root.createData("skippedFile.txt");

        SimpleSearchInfoDefinition searchInfo = new SimpleSearchInfoDefinition(
                root, new SearchFilterDefinition[]{
                    new NiceFilter(f1), // this file will be used
                    new BadFilterDef() // this filter should be removed
                });
        Iterator<FileObject> files = searchInfo.filesToSearch(
                SearchScopeOptions.create(), new SearchListener() {
        },
                new AtomicBoolean(false));
        assertEquals("The first line should be found", f1, files.next());
        assertFalse("The second file should be filtered", files.hasNext());
    }

    /**
     * Filter that is bad to all files.
     */
    private static class BadFilterDef extends SearchFilterDefinition {

        @Override
        public boolean searchFile(FileObject file)
                throws IllegalArgumentException {
            return false;
        }

        @Override
        public FolderResult traverseFolder(FileObject folder)
                throws IllegalArgumentException {
            return FolderResult.DO_NOT_TRAVERSE;
        }
    }

    /**
     * Filter that is nice to a single file, but bad to other files.
     */
    private static class NiceFilter extends SearchFilterDefinition {

        FileObject favouredFile;

        public NiceFilter(FileObject favouredFile) {
            this.favouredFile = favouredFile;
        }

        @Override
        public boolean searchFile(FileObject file)
                throws IllegalArgumentException {
            return file.equals(favouredFile);
        }

        @Override
        public FolderResult traverseFolder(FileObject folder)
                throws IllegalArgumentException {
            return FolderResult.TRAVERSE;
        }
    }
}
