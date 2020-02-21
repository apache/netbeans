/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
