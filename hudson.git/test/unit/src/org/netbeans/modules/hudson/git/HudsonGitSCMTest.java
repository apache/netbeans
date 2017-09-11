/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
