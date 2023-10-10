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

package org.netbeans.modules.bugzilla.autoupdate;

import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.TestConstants;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_PASSWD;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_URL;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_USER;
import org.netbeans.modules.bugzilla.TestUtil;


/**
 *
 * @author tomas
 */
public class BugzillaSupportedTest extends NbTestCase implements TestConstants {

    public BugzillaSupportedTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        System.setProperty("netbeans.t9y.bugzilla.supported.version", "9999.9999.9999"); // should be enough
        super.setUp();
    }

    public void testIsSupportedBugzillaVersion() {
        BugzillaVersion version = BugzillaAutoupdate.getInstance().getServerVersion(TestUtil.getRepository("test", REPO_URL, REPO_USER, REPO_PASSWD));
        assertTrue(BugzillaAutoupdate.getInstance().isSupportedVersion(version));
    }

}
