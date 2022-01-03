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

package org.netbeans.modules.cnd.highlight.error;

import java.io.File;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 * Deletes all semicolons in Quote project,
 * checks error mesages
 */
public class MissedSemicolonsInQuoteTestCase extends ErrorHighlightingBaseTestCase {

    static {
        System.setProperty("cnd.parser.error.transparent", "false");
        System.setProperty("cnd.modelimpl.trace.error.provider", "true");
        System.setProperty("parser.report.errors", "true");
    }

    public MissedSemicolonsInQuoteTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected File getTestCaseDataDir() {
	String dataPath = getDataDir().getAbsolutePath().replaceAll("highlight/build/", "modelimpl/").replaceAll("highlight\\\\", "modelimpl\\"); //NOI18N
        String filePath = "common/quote_nosyshdr";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }
    
    public void testRun() throws Exception {
        MissedSemicolonsErrorMaker errorMaker = new MissedSemicolonsErrorMaker();
        CsmProject project = getProject();
        assert(project != null);
        for (CsmFile file : project.getSourceFiles()) {
            performDynamicTest(file, errorMaker);
        }
        errorMaker.printStatistics();
    }
}
