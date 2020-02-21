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
public class ModifyUndoRedo190950TestCase extends ModifyDocumentTestCaseBase {
    public ModifyUndoRedo190950TestCase(String testName) {
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

    public void testInsertSaveThenUndoRedo190950() throws Exception {
        // #190950:  Highlighting does not work if undo/redo is done
        if (TraceFlags.TRACE_191307_BUG) {
            System.err.printf("TEST UNDO AFTER SAVE\n");
        }
        final File sourceFile = getDataFile("fileForModification.cc");
        super.insertTextThenSaveAndCheck(sourceFile, 12 + 1, "void foo() {}\n", 
                sourceFile, new DeclarationsNumberChecker(3, 4), true);
    }    
}
