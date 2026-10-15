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

package org.netbeans.modules.maven.indexer.api;

import java.util.Date;
import java.util.Locale;
import org.apache.maven.settings.Mirror;
import org.junit.Ignore;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.maven.embedder.EmbedderFactory;

public class RepositoryPreferencesTest extends NbTestCase {

    public RepositoryPreferencesTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        return new NbTestSuite(RepositoryPreferencesTest.class);
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

    @Ignore
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
            rp.addTransientRepository(1, "central", "central", "https://repo.maven.apache.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink, central]", m.getMirroredRepositories().toString());

            //add central AGAIN and AGAIN.. should have still just 2 mirrored repositories..
            rp.addTransientRepository(2, "central", "central", "https://repo.maven.apache.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(3, "central", "central", "https://repo.maven.apache.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(4, "central", "central", "https://repo.maven.apache.org/maven2", RepositoryInfo.MirrorStrategy.ALL);

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink, central]", m.getMirroredRepositories().toString());

            //try adding slightly modified transient repositories..
            rp.addTransientRepository(3, "central", "central", "https://repo.maven.apache.org/maven2/", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(2, "central", "central rep", "https://repo.maven.apache.org/maven2", RepositoryInfo.MirrorStrategy.ALL);
            rp.addTransientRepository(2, "eclipselink", "Repository for library Library", "http://ftp.ing.umu.se/mirror/eclipse/rt/eclipselink/maven.repo", RepositoryInfo.MirrorStrategy.ALL);

            assertEquals("[local, mirror]", rp.getRepositoryInfos().toString());
            m = rp.getRepositoryInfoById("mirror");
            assertTrue(m.isMirror());
            assertEquals("[eclipselink, central]", m.getMirroredRepositories().toString());
        } finally {
           EmbedderFactory.getOnlineEmbedder().getSettings().removeMirror(mirror);
        }
    }

    public void testDefaultFreqIsWeek() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            int def = RepositoryPreferences.getDefaultIndexUpdateFrequency();
            assertEquals("Once a week is default", RepositoryPreferences.FREQ_ONCE_WEEK, def);
        } finally {
            Locale.setDefault(orig);
        }
    }

    public void testBrandingFreqToNever() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("te", "ST"));
            int def = RepositoryPreferences.getDefaultIndexUpdateFrequency();
            assertEquals("Branded to startup", RepositoryPreferences.FREQ_STARTUP, def);
        } finally {
            Locale.setDefault(orig);
        }
    }

    public void testDefaultCreateIndex() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            boolean def = RepositoryPreferences.getDefaultIndexRepositories();
            assertTrue("Indexing is on", def);
        } finally {
            Locale.setDefault(orig);
        }
    }

    public void testBrandingCreateIndex() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("te", "ST"));
            boolean def = RepositoryPreferences.getDefaultIndexRepositories();
            assertFalse("Never create index", def);
        } finally {
            Locale.setDefault(orig);
        }
    }
}
