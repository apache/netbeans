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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 */
public class RemoveAndInsertDeadBlockTestCase extends ModifyDocumentTestCaseBase {
    public RemoveAndInsertDeadBlockTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        if (Boolean.getBoolean("cnd.modelimpl.trace.test")) {
            TraceFlags.TRACE_182342_BUG = true;
        }
        super.setUp();
    }

    @Override
    protected Class<?> getTestCaseDataClass() {
        return ModifyDocumentTestCaseBase.class;
    }

    public void testRemoveDeadBlock() throws Exception {
        if (Boolean.getBoolean("cnd.modelimpl.trace.test")) {
            TraceFlags.TRACE_182342_BUG = true;
        }
        if (TraceFlags.TRACE_182342_BUG) {
            System.err.printf("TEST REMOVE DEAD BLOCK\n");
        }
        final File sourceFile = getDataFile("fileWithDeadCode.cc");
        super.removeDeadBlock(sourceFile, 1, 0);
    }
}
