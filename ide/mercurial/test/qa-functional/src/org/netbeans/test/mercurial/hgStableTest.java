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
package org.netbeans.test.mercurial;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.main.archeology.AnnotationsTest;
import org.netbeans.test.mercurial.main.commit.CloneTest;
import org.netbeans.test.mercurial.main.commit.CommitDataTest;
import org.netbeans.test.mercurial.main.commit.CommitUiTest;
import org.netbeans.test.mercurial.main.commit.IgnoreTest;
import org.netbeans.test.mercurial.main.commit.InitializeTest;
import org.netbeans.test.mercurial.main.delete.DeleteUpdateTest;
import org.netbeans.test.mercurial.main.properties.HgPropertiesTest;
import org.netbeans.test.mercurial.utils.hgExistsChecker;

/**
 *
 * @author tester
 */
public class hgStableTest extends JellyTestCase {

    public hgStableTest(String name) {
        super(name);
    }

    public static Test suite() {
        if (hgExistsChecker.check(false)) {
            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
                    .addTest(InitializeTest.class, "testInitializeAndFirstCommit")
                    .addTest(CommitDataTest.class, "testCommitFile", "testRecognizeMimeType")
//                    .addTest(CommitUiTest.class, "testInvokeCloseCommit")
                    .addTest(IgnoreTest.class, "testIgnoreUnignoreFile")
//                    .addTest(DeleteUpdateTest.class, "testDeleteUpdate")
                    .addTest(AnnotationsTest.class, "testShowAnnotations")
                    .addTest(HgPropertiesTest.class, "testHgPropertiesTest")
                    .addTest(CloneTest.class, "testCloneProject")
                    .enableModules(".*")
                    .clusters(".*"));
        } else {
            return NbModuleSuite.create(NbModuleSuite.emptyConfiguration());
        }
    }
}
