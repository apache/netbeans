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
public class ModifyUndo191307TestCase extends ModifyDocumentTestCaseBase {
    public ModifyUndo191307TestCase(String testName) {
        super(testName);
//        System.setProperty("cnd.modelimpl.trace191307", "true");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected File getTestCaseDataDir() {
        File testCaseDataDir = super.getTestCaseDataDir();
        return new File(testCaseDataDir.getParent(), "ModifyUndoTestCase");
    }
    
    public void testRemoveThenUndo191307() throws Exception {
        // #191307:  Undo operation breaks code model
        if (TraceFlags.TRACE_191307_BUG) {
            System.err.printf("TEST UNDO REMOVE\n");
        }
        final File sourceFile = getDataFile("fileForModification.cc");
        long length = sourceFile.length();
        
        super.deleteTextThenUndo(sourceFile, 0, (int)length, 3, 0);
    } 
}
