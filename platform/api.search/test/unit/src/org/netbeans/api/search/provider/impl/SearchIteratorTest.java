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

package org.netbeans.api.search.provider.impl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.textui.TestRunner;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Marian Petras
 */
public final class SearchIteratorTest extends NbTestCase {

    /** */
    private FileObject dataDir;
    /** */
    FileObject projectRoot;

    /**
     */
    public SearchIteratorTest(String name) {
        super(name);
    }

    /**
     */
    public static void main(String args[]) {
        TestRunner.run(new NbTestSuite(SearchIteratorTest.class));
    }

    @Override
    protected void setUp() throws Exception {
        dataDir = FileUtil.toFileObject(getDataDir());
         assert dataDir != null;
        
        projectRoot = dataDir.getFileObject("projects/Project1");       //NOI18N
        assert projectRoot != null;
    }

    /**
     */
    public void testPlainSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    false,          //check visibility?
                                    false,          //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testVisibilitySearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    true,           //check visibility?
                                    false,          //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testSharabilitySearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    false,          //check visibility?
                                    true,           //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testVisibSharSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    true,           //check visibility?
                                    true,           //check sharability?
                                    getRef());
        compareReferenceFiles();
    }

    /**
     */
    private void generateSearchableFileNames(
            FileObject folder,
            boolean recursive,
            boolean checkVisibility,
            boolean checkSharability,
            PrintStream refPrintStream) {

        FileObject[] foldersToCheck = new FileObject[2];
        foldersToCheck[0] = folder.getFileObject("src");
        foldersToCheck[1] = folder.getFileObject("test");
        for (FileObject f : foldersToCheck) {
            if ((f == null) || !f.isFolder()) {
                throw new IllegalStateException();
            }
        }
                
        SearchFilterDefinition[] filters;

        int filtersCount = 0;
        if (checkVisibility) {
            filtersCount++;
        }
        if (checkSharability) {
            filtersCount++;
        }

        if (filtersCount == 0) {
            filters = new SearchFilterDefinition[0];
        } else {
            filters = new SearchFilterDefinition[filtersCount];

            int i = 0;
            if (checkVisibility) {
                filters[i++] = SearchInfoDefinitionFactory.VISIBILITY_FILTER;
            }
            if (checkSharability) {
                filters[i++] = SearchInfoDefinitionFactory.SHARABILITY_FILTER;
            }
        }

        SearchInfo searchInfo = SearchInfoUtils.createSearchInfoForRoots(
                foldersToCheck,
                false,
                filters);

        assertTrue("project root not searchable", searchInfo.canSearch());
        
        List<String> foundFilesPaths = new ArrayList<>(16);
        SearchScopeOptions sso = SearchScopeOptions.create("*", false);
        SearchListener lstnr = new SearchListener() {};
        AtomicBoolean terminated = new AtomicBoolean(false);
        for (FileObject primaryFile : searchInfo.getFilesToSearch(
                sso, lstnr, terminated)) {
            String relativePath = FileUtil.getRelativePath(projectRoot,
                                                           primaryFile);
            foundFilesPaths.add(relativePath);
        }
        
        Collections.sort(foundFilesPaths);
        
        for (String path : foundFilesPaths) {
            refPrintStream.println(path);
        }
    }

}
