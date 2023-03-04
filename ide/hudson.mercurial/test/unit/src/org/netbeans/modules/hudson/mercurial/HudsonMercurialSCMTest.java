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

package org.netbeans.modules.hudson.mercurial;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

public class HudsonMercurialSCMTest extends NbTestCase {

    public HudsonMercurialSCMTest(String n) {
        super(n);
    }

    public void testGetDefaultPull() throws Exception {
        HudsonMercurialSCM.LOG.setLevel(Level.OFF);
        clearWorkDir();
        assertNull("no repo", HudsonMercurialSCM.getDefaultPull(Utilities.toURI(getWorkDir())));
        assertPullURI("http://host/repo/", "[paths]", "default = http://host/repo/");
        assertPullURI("http://host/repo/", "[paths]", "default = http://host/repo");
        assertPullURI("http://host/repo/", "[paths]", "default-pull = http://host/repo/");
        assertPullURI("http://host/repo/", "[paths]", "default-pull = http://host/repo/", "default = http://host/other/");
        assertPullURI(Utilities.toURI(getWorkDir()).toString(), "[paths]", "default=" + getWorkDirPath().replace(File.separatorChar, '/'));
        assertPullURI(Utilities.toURI(getWorkDir()) + "foo/", "[paths]", "default = foo");
        assertPullURI(Utilities.toURI(getWorkDir()).toString(), "[paths]");
        assertPullURI("https://host/repo/", "[paths]", "default = https://bob:sEcReT@host/repo/");
        assertPullURI("https://host/repo/", "[paths]", "default = https://bob@host/repo/");
        assertPullURI("ssh://host/repo/", "[paths]", "default = ssh://bob@host/repo");
        assertPullURI(null, "[paths");
        assertPullURI(Utilities.toURI(getWorkDir()).toString());
    }

    private void assertPullURI(String pull, String... hgrc) throws Exception {
        clearWorkDir();
        TestFileUtils.writeFile(new File(getWorkDir(), ".hg/requires"), "revlogv1\nstore\n");
        if (hgrc.length > 0) {
            StringBuilder b = new StringBuilder();
            for (String line : hgrc) {
                b.append(line).append('\n');
            }
            TestFileUtils.writeFile(new File(getWorkDir(), ".hg/hgrc"), b.toString());
        }
        assertEquals(pull != null ? URI.create(pull) : null, HudsonMercurialSCM.getDefaultPull(Utilities.toURI(getWorkDir())));
    }

}
