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

package org.netbeans.modules.hudson.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;

public class HudsonGitSCMTest extends NbTestCase {

    public HudsonGitSCMTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    @Override protected String logRoot() {
        return HudsonGitSCMTest.class.getPackage().getName();
    }

    public void testForFolder() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), ".git/config"),
                  "[core]\n"
                + "\trepositoryformatversion = 0\n"
                + "[remote \"origin\"]\n"
                + "\tfetch = +refs/heads/*:refs/remotes/origin/*\n"
                + "\turl = git@github.com:x/y.git\n"
                + "[branch \"master\"]\n"
                + "\tremote = origin\n"
                + "\tmerge = refs/heads/master\n");
        // ref: http://www.kernel.org/pub/software/scm/git/docs/git-clone.html#_git_urls_a_id_urls_a
        assertEquals("ssh://git@github.com/x/y.git", String.valueOf(HudsonGitSCM.getRemoteOrigin(Utilities.toURI(getWorkDir()), null)));
        HudsonSCM.Configuration cfg = new HudsonGitSCM().forFolder(getWorkDir());
        assertNotNull(cfg);
        Document doc = XMLUtil.createDocument("whatever", null, null, null);
        cfg.configure(doc);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String text = baos.toString("UTF-8");
        assertTrue(text, text.contains("x/y.git"));
    }

    public void testForFolderLocal() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), ".git/config"),
                  "[core]\n"
                + "\trepositoryformatversion = 0\n");
        assertEquals(null, HudsonGitSCM.getRemoteOrigin(Utilities.toURI(getWorkDir()), null));
        HudsonSCM.Configuration cfg = new HudsonGitSCM().forFolder(getWorkDir());
        assertNotNull(cfg);
        Document doc = XMLUtil.createDocument("whatever", null, null, null);
        cfg.configure(doc);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String text = baos.toString("UTF-8");
        assertTrue(text, text.contains(getWorkDirPath()));
    }

    public void testForFolderUnversioned() throws Exception { // #204834
        assertNull(new HudsonGitSCM().forFolder(getWorkDir()));
    }

    public void testROReplacement() throws Exception {
        assertEquals("git://github.com/x/y.git", HudsonGitSCM.roReplacement("ssh://git@github.com/x/y.git"));
        // XXX should https://user@github.com/user/repo.git also be replaced?
        assertEquals("git://kenai.com/repo", HudsonGitSCM.roReplacement("ssh://user@git.kenai.com/repo"));
        // XXX test java.net too
        assertEquals(null, HudsonGitSCM.roReplacement("ssh://elsewhere/stuff.git"));
    }
    
}
