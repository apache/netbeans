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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;

/**
 * 
 */
public class WhereUsedInQuoteTestCase extends CsmWhereUsedQueryPluginTestCaseBase {

    public WhereUsedInQuoteTestCase(String testName) {
        super(testName);
        System.setProperty("cnd.repository.hardrefs", "true");
    }

    @Override
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

    public void testIncludeModuleH() throws Exception {
        performWhereUsed("memory.h", 44, 15);
    }

    public void testClassCustomer() throws Exception {
        performWhereUsed("customer.h", 49, 10);
    }

    public void testComputeSupportMetric() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, true);
        props.put(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, true);
        props.put(WhereUsedQuery.FIND_REFERENCES, true);
        performWhereUsed("memory.cc", 46, 15, props);
    }

    public void testCustomerGetName() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, true);
        props.put(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, true);
        props.put(WhereUsedQuery.FIND_REFERENCES, true); // NOW we have zero usages, but this should be fixed soon
        performWhereUsed("customer.h", 52, 20, props);
    }

    public void testModuleAllSubtypes() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQueryConstants.FIND_SUBCLASSES, true);
        props.put(WhereUsedQuery.FIND_REFERENCES, false);
        performWhereUsed("memory.h", 46, 25, props);
    }
    
    public void testModuleDirectSubtypes() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, true);
        props.put(WhereUsedQuery.FIND_REFERENCES, false);
        performWhereUsed("memory.h", 46, 25, props);
    }
    
    public void testModuleGetType() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQuery.FIND_REFERENCES, true);
        performWhereUsed("module.h", 68, 25, props);
    }

    public void testModuleGetTypeNoOverriden() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, false);
        props.put(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, true);
        props.put(WhereUsedQuery.FIND_REFERENCES, true);
        performWhereUsed("module.h", 68, 25, props);
    }

    public void testModuleGetTypeOverriden() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, true);
        props.put(WhereUsedQuery.FIND_REFERENCES, false);
        performWhereUsed("module.h", 68, 25, props);
    }

    public void testMemoryGetTypeNoOverriden() throws Exception {
        Map<Object, Boolean> props = new HashMap<>();
        props.put(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, false);
        props.put(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, true);
        props.put(WhereUsedQuery.FIND_REFERENCES, true);
        performWhereUsed("memory.h", 52, 25, props);
    }

    public void testIZ175700() throws Exception {
        // IZ#175700 : [code model] Parser does not recognized inline initialization in constructor
        performWhereUsed("quote.cc", 169, 12);
    }
}
