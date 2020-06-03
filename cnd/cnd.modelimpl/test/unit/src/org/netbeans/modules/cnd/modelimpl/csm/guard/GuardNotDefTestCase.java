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

package org.netbeans.modules.cnd.modelimpl.csm.guard;

import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 * base class for guard block tests
 *
 */
public class GuardNotDefTestCase extends GuardTestBase {
    
    public GuardNotDefTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGuard() throws Exception {
        parse("cstdlib.h", "argc.cc", "-m");
        boolean checked = false;
        for(FileImpl file : getProject().getAllFileImpls()){
            if ("cstdlib.h".equals(file.getName().toString())){ // NOI18N
                assertTrue("Guard guard block defined", file.getMacros().size()==2); // NOI18N
                //String guard = file.testGetGuardState().testGetGuardName();
                //assertTrue("Guard guard block name not _STDLIB_H", "_STDLIB_H".equals(guard)); // NOI18N
                checked = true;
            } else if ("iostream.h".equals(file.getName())){ // NOI18N
                //String guard = file.testGetGuardState().testGetGuardName();
                //assertTrue("Guard guard block found", guard == null); // NOI18N
            } else if ("argc.cc".equals(file.getName())){ // NOI18N
                //String guard = file.testGetGuardState().testGetGuardName();
                //assertTrue("Guard guard block name not MAIN", "MAIN".equals(guard)); // NOI18N
            }
        }
        assertTrue("Not found FileImpl for cstdlib.h", checked); // NOI18N
    }
    
}
