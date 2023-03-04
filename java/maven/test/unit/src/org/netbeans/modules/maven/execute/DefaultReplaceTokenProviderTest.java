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

package org.netbeans.modules.maven.execute;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class DefaultReplaceTokenProviderTest extends NbTestCase {

    public DefaultReplaceTokenProviderTest(String name) {
        super(name);
    }

    private FileObject d;

    @Override protected void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testTestSingle() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        TestFileUtils.writeFile(d, "src/test/java/p1/FirstTest.java", "package p1; class FirstTest {}");
        TestFileUtils.writeFile(d, "src/test/java/p1/SecondTest.java", "package p1; class SecondTest {}");
        TestFileUtils.writeFile(d, "src/test/java/p2/deeper/ThirdTest.java", "package p2.deeper; class ThirdTest {}");
        TestFileUtils.writeFile(d, "src/test/java/Suite.java", "class Suite {}");
        Project p = ProjectManager.getDefault().findProject(d);
        assertEquals("p1.FirstTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1/FirstTest.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals("Suite", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/Suite.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        // CLASSNAME no longer used in standard bindings (#147793); CLASSNAME_EXT probably never used; but for compatibility with custom bindings:
        assertEquals("FirstTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1/FirstTest.java"))).get(DefaultReplaceTokenProvider.CLASSNAME));
        assertEquals("FirstTest.java", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1/FirstTest.java"))).get(DefaultReplaceTokenProvider.CLASSNAME_EXT));
        // #212839: multiselections
        assertEquals("p1.FirstTest,p2.deeper.ThirdTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.fixed(d.getFileObject("src/test/java/p1/FirstTest.java"), d.getFileObject("src/test/java/p2/deeper/ThirdTest.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals("p1.**.*Test", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p1"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals("p2.deeper.**.*Test", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/test/java/p2/deeper"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        // XXX TBD what should be passed for src/test/java/p2; is p2.*Test (i.e. nonrecursive) OK? If Surefire supports either, do we check for NonrecursiveFolder?
        // XXX test src/main/java selections
        // XXX test selections across groups, or outside groups
        // XXX test single methods
        
        //#213671
        assertEquals(null, ActionProviderImpl.replacements(p, ActionProvider.COMMAND_RUN, Lookup.EMPTY).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));
        assertEquals(null, ActionProviderImpl.replacements(p, ActionProvider.COMMAND_RUN, Lookup.EMPTY).get(DefaultReplaceTokenProvider.CLASSNAME));
        assertEquals(null, ActionProviderImpl.replacements(p, ActionProvider.COMMAND_RUN, Lookup.EMPTY).get(DefaultReplaceTokenProvider.CLASSNAME_EXT));
        
    }
    
    public void testNgSingle() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        TestFileUtils.writeFile(d, "src/test/java/p1/FirstNGTest.java", "package p1; class FirstNGTest {}");
        TestFileUtils.writeFile(d, "src/main/java/p1/First.java", "package p1; class First {}");
        
        Project p = ProjectManager.getDefault().findProject(d);
        assertEquals("p1.FirstNGTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/main/java/p1/First.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));

    }

}
