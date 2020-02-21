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
package org.netbeans.modules.cnd.repository.impl;

import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.keys.PersistentFactoryListener;
import org.netbeans.modules.cnd.repository.keys.TestValuePersistentFactory;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;

/**
 * Tests Repository.tryGet()
 *
 */
public abstract class GetPutTestBase extends CndBaseTestCase implements PersistentFactoryListener {

     public static final int SMALL_KEY_HANDLER = 657;
     public static final int LARGE_KEY_HANDLER = 658;
    
    protected GetPutTestBase(java.lang.String testName) {
        super(testName);
    }
    

    protected int getUnitID() {
        return Repository.getUnitId(
                new UnitDescriptor("Repository_Test_Unit",
                CndFileUtils.getLocalFileSystem()));
    }    

    @Override
    protected void setUp() throws Exception {
        RepositoryTestUtils.deleteDefaultCacheLocation();
        super.setUp();
        Repository.startup(0);
        TestValuePersistentFactory.getInstance().addPersistentFactoryListener(this);
    }

    @Override
    protected void tearDown() throws Exception {
        TestValuePersistentFactory.getInstance().removePersistentFactoryListener(this);
        super.tearDown();
        Repository.shutdown();
    }
}
