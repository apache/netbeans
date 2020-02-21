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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 *
 */
public class DocumentModificationTest extends CndBaseTestSuite {
    public DocumentModificationTest() {
        super("C/C++ Document modifications test");
        this.addTestSuite(RestoreHandlerTestCase.class);
        if (Boolean.getBoolean("cnd.document.modification.test")) { //NOI18N
            this.addTest(ModifyUndo191307TestCase.class);
            this.addTest(ModifyUndoRedo190950TestCase.class);
            this.addTestSuite(InsertDeadBlockTestCase.class);
            this.addTestSuite(RemoveDeadBlockTestCase.class);
            this.addTestSuite(RemoveAndInsertDeadBlockTestCase.class); // unstable
            this.addTestSuite(ModifyMultiIncludedHeaderTestCase.class); // failing
            this.addTestSuite(ModifyIncludedHeaderTestCase.class);
            this.addTestSuite(ModifyMultiIncludedLibraryHeaderTestCase.class); 
        }
    }

    public static Test suite() {
        TestSuite suite = new DocumentModificationTest();
        return suite;
    }
}
