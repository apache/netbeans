/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.test.subversion.testsuites;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.main.delete.DeleteTest;
import org.netbeans.test.subversion.main.delete.FilesViewRefTest;
import org.netbeans.test.subversion.main.delete.RefactoringTest;
import org.netbeans.test.subversion.utils.svnExistsChecker;

/**
 *
 * @author Petr Dvorak, (joshis)
 */
public class deleteTestSuite extends JellyTestCase {
    
    public deleteTestSuite(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
    }

    /**
     * Simple method uniting together all the different tests under subversion
     * tests-qa-functional
     */
    public static Test suite() {
        if (svnExistsChecker.check(false)) {
            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
                    .addTest(DeleteTest.class, "testDeleteRevert", "testDeleteCommit")
                    .addTest(RefactoringTest.class, "testRefactoring")
                    //.addTest(FilesViewRefTest.class, "testFilesViewRefactoring")
                    .enableModules(".*").clusters(".*"));
        } else {
            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration());
        }
    }
}
