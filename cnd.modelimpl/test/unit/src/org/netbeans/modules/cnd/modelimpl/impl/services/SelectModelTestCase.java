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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.File;
import org.netbeans.modules.cnd.modelimpl.trace.FileModelCpp11Test;
import org.netbeans.modules.cnd.modelimpl.trace.FileModelCpp14Test;

/**
 *
 */
public class SelectModelTestCase extends SelectTestBase {


    public SelectModelTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.tests.cpp11directories", FileModelCpp11Test.class.getSimpleName()); // NOI18N
        System.setProperty("cnd.tests.cpp14directories", FileModelCpp14Test.class.getSimpleName()); // NOI18N
        super.setUp(); 
    }

    @Override
    protected File getProjectRoot() {
        return getDataFile("org");
    }


//    @Test
    public void testSelectModelGetFunctions() throws Exception {
        doTestGetFunctions();
    }

}
