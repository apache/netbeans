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

package org.netbeans.modules.cnd.navigation.switchfiles;

import java.io.File;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 *
 */
public class CppSwitchTest extends TraceModelTestBase {

    public CppSwitchTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testTwoNamesakes() throws Exception {
        String source = "welcome.cc"; // NOI18N
        performTest("", source + ".dat", null/*source + ".err"*/); // NOI18N
    }

    protected @Override void postTest(String[] args, Object... params) {
        CsmProject project = getCsmProject();
        Collection<CsmFile> files = project.getAllFiles();
        assert files.size() > 0;
        for (CsmFile csmFile : files) {
            if (csmFile.getAbsolutePath().toString().indexOf("welcome.cc")!=-1) { //NOI18N
                CsmFile f = CppSwitchAction.findHeader(csmFile);
                assertNotNull("Correspondent header not found", f);
                assertNotSame("Wrong header was found", f.getAbsolutePath().toString().indexOf("dir1" + File.separator +"welcome.h"), -1);
            } else if (csmFile.getAbsolutePath().toString().indexOf("welcome.h")!=-1) { //NOI18N
                CsmFile f = CppSwitchAction.findSource(csmFile);
                assert f!=null && f.getAbsolutePath().toString().indexOf("welcome.cc")!=-1; //NOI18N
            } else {
                assert(false);
            }
        }
    }
    
}
