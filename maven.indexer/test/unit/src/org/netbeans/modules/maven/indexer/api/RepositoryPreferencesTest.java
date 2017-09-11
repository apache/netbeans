/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.indexer.api;

import java.util.Date;
import static junit.framework.Assert.assertEquals;
import org.apache.maven.settings.Mirror;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.maven.embedder.EmbedderFactory;

public class RepositoryPreferencesTest extends NbTestCase {

    public RepositoryPreferencesTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new RepositoryPreferencesTest("testNoConsecutiveSlashesInRepositoryID"));
        suite.addTest(new RepositoryPreferencesTest("testGetRepositoryInfos"));
        suite.addTest(new RepositoryPreferencesTest("testGetMirrorRepositoryInfos"));
        return suite;
    }

    @Override protected void setUp() throws Exception {
        System.setProperty("no.local.settings", "true");
    }
    
    // issue http://netbeans.org/bugzilla/show_bug.cgi?id=239898
    public void testNoConsecutiveSlashesInRepositoryID() throws Exception {
        RepositoryPreferences rp = RepositoryPreferences.getInstance();
        assertEquals("[local, central]", rp.getRepositoryInfos().toString());
        rp.addTransientRepository(1, "foo_http://nowhere.net", "Foo", "http://nowhere.net/", RepositoryInfo.MirrorStrategy.NONE);
        assertEquals("[local, foo_http:_nowhere.net]", rp.getRepositoryInfos().toString());
        RepositoryPreferences.getLastIndexUpdate("foo_http://nowhere.net");
        RepositoryPreferences.setLastIndexUpdate("foo_http://nowhere.net", new Date());
        rp.removeTransientRepositories(1);
    }   
    
    public void testGetRepositoryInfos() throws Exception {
        RepositoryPreferences rp = RepositoryPreferences.getInstance();
        assertEquals("[local, central]", rp.getRepositoryInfos().toString());
        rp.addTransientRepository(1, "foo", "Foo", "http://nowhere.net/", RepositoryInfo.MirrorStrategy.NONE);
        assertEquals("[local, foo]", rp.getRepositoryInfos().toString());
        rp.addTransientRepository(2, "foo.bar", "Foo Bar", "http://nowhere.net/", RepositoryInfo.MirrorStrategy.NONE);
        assertEquals("[local, foo]", rp.getRepositoryInfos().toString());
        rp.removeTransientRepositories(1);
        assertEquals("[local, foo.bar]", rp.getRepositoryInfos().toString());
        rp.addTransientRepository(3, "foo.bar", "Foo Bar", "http://somewhere.net/", RepositoryInfo.MirrorStrategy.NONE);
        assertEquals("[local, foo.bar]", rp.getRepositoryInfos().toString());
        rp.removeTransientRepositories(2);
        rp.removeTransientRepositories(3);
        assertEquals("[local, central]", rp.getRepositoryInfos().toString());
    }
    
public void testNonHttpRepositoryInfos() throws Exception { //#227322
        RepositoryPreferences rp = RepositoryPreferences.getInstance();
        assertEquals("[local, central]", rp.getRepositoryInfos().toString());
        rp.addTransientRepository(1, "foo", "Foo", "scp://192.168.1.1/mkleint", RepositoryInfo.MirrorStrategy.NONE);
        assertEquals("[local, central]", rp.getRepositoryInfos().toString());
        rp.addTransientRepository(2, "bar", "bar", "ftp://192.168.1.1/mkleint", RepositoryInfo.MirrorStrategy.NONE);
        assertEquals("[local, central]", rp.getRepositoryInfos().toString());
    }    
    
    /** created in attempt of reproducing issue http://netbeans.org/bugzilla/show_bug.cgi?id=214980
     */
    public void testGetMirrorRepositoryInfos() throws Exception {
        Mirror mirror = new Mirror();
        mirror.setId("mirror");
        mirror.setMirrorOf("*");
        mirror.setUrl("http://localhost");
        mirror.setName("mirror repository");
        try {
            EmbedderFactory.getOnlineEmbedder().getSettings().addMirror(mirror);

            RepositoryPreferences rp = RepositoryPreferences.getInstance();

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            RepositoryInfo m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[central]", m.getMirroredRepositories().toString());

            //add a repository
            rp.addTransientRepository(1, "eclipselink", "Repository for library Library[eclipselink]", "http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo", RepositoryInfo.MirrorStrategy.ALL);

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink]", m.getMirroredRepositories().toString());

            //add the same repository again..
            rp.addTransientRepository(1, "eclipselink", "Repository for library Library[eclipselink]", "http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo", RepositoryInfo.MirrorStrategy.ALL);

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink]", m.getMirroredRepositories().toString());

            //add as non-transient repository now..
            RepositoryInfo ii = new RepositoryInfo("eclipselink", "Repository for library Library[eclipselink]", null, "http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo");
            rp.addOrModifyRepositoryInfo(ii);
            //in this case mirror is not used and direct reference is used..
            assertEquals("[local, eclipselink]", rp.getRepositoryInfos().toString());

            //remove and mirror should show up again..
            rp.removeRepositoryInfo(ii);

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink]", m.getMirroredRepositories().toString());

            //add central now.. should have 2 mirrored repositories..
            rp.addTransientRepository(1, "central", "central", "http://repo1.maven.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink, central]", m.getMirroredRepositories().toString());

            //add central AGAIN and AGAIN.. should have still just 2 mirrored repositories..
            rp.addTransientRepository(2, "central", "central", "http://repo1.maven.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(3, "central", "central", "http://repo1.maven.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(4, "central", "central", "http://repo1.maven.org/maven2", RepositoryInfo.MirrorStrategy.ALL);

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink, central]", m.getMirroredRepositories().toString());

            //try adding slightly modified transient repositories..
            rp.addTransientRepository(3, "central", "central", "http://repo1.maven.org/maven2/", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(2, "central", "central rep", "http://repo1.maven.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(2, "eclipselink", "Repository for library Library", "http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo", RepositoryInfo.MirrorStrategy.ALL);

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink, central]", m.getMirroredRepositories().toString());
        } finally {
           EmbedderFactory.getOnlineEmbedder().getSettings().removeMirror(mirror); 
        }
    } 


}
