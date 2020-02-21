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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;

/**
 *
 */
public class WhereUsedFiltersTestCase extends CsmWhereUsedQueryPluginTestCaseBase {
    public WhereUsedFiltersTestCase(String testName) {
        super(testName);
    }
    
    public void testDeclarationsFilterDefault() throws Exception {
        performWhereUsed("testFUFilters.c", 13, 7);
    }
    
    public void testDeclarationsFilterSelected() throws Exception {
        performWhereUsed("testFUFilters.c", 13, 7, null, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void testDeclarationsFilterDeselected() throws Exception {
        // Only comments filter enabled
        performWhereUsed("testFUFilters.c", 13, 7, null, Arrays.asList(CsmWhereUsedFilters.COMMENTS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void testDeadcodeFilterSelected() throws Exception {
        performWhereUsed("testFUFilters.c", 16, 5, null, Arrays.asList(CsmWhereUsedFilters.DEAD_CODE.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void testDeadcodeFilterDeselected() throws Exception {
        // Only comments filter enabled
        performWhereUsed("testFUFilters.c", 16, 5, null, Arrays.asList(CsmWhereUsedFilters.COMMENTS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void testMacrosFilterSelected() throws Exception {
        performWhereUsed("testFUFilters.c", 16, 5, null, Arrays.asList(CsmWhereUsedFilters.MACROS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void testMacrosFilterDeselected() throws Exception {
        // Only declarations filter selected
        performWhereUsed("testFUFilters.c", 16, 5, null, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void testCommentsFilterSelected() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        // Only declarations filter selected
        performWhereUsed("testFUFilters.c", 29, 7, props, Arrays.asList(CsmWhereUsedFilters.COMMENTS.getKey(), CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
    
    public void testCommentsFilterDeselected() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        // Only declarations filter selected
        performWhereUsed("testFUFilters.c", 29, 7, props, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ.getKey(),CsmWhereUsedFilters.WRITE.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }

    public void testRFilterSelected() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        performWhereUsed("testRWFilters.cpp", 2, 9, props, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ.getKey()));
    }

    public void testWFilterSelected() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        performWhereUsed("testRWFilters.cpp", 2, 9, props, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.WRITE.getKey()));
    }

    public void testRWFilterSelected() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        performWhereUsed("testRWFilters.cpp", 2, 9, props, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }

    public void testRFilterSelected2() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        performWhereUsed("testRWFilters2.cpp", 1, 18, props, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ.getKey()));
    }

    public void testWFilterSelected2() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        performWhereUsed("testRWFilters2.cpp", 1, 18, props, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.WRITE.getKey()));
    }

    public void testRWFilterSelected2() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.SEARCH_IN_COMMENTS, true);
        performWhereUsed("testRWFilters2.cpp", 1, 18, props, Arrays.asList(CsmWhereUsedFilters.DECLARATIONS.getKey(),CsmWhereUsedFilters.READ_WRITE.getKey()));
    }
}

