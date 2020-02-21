/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

