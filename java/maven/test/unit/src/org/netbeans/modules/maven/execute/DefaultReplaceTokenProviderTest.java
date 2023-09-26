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
package org.netbeans.modules.maven.execute;

import java.io.IOException;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class DefaultReplaceTokenProviderTest extends NbTestCase {

    public DefaultReplaceTokenProviderTest(String name) {
        super(name);
    }

    private FileObject d;

    @Override
    protected void setUp() throws Exception {
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
    
    public void testTestNotIT_GH4587() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        TestFileUtils.writeFile(d, "src/test/java/p1/FirstTest.java", "package p1; class FirstTest {}");
        TestFileUtils.writeFile(d, "src/test/java/p1/FirstIT.java", "package p1; class FirstIT {}");
        Project p = ProjectManager.getDefault().findProject(d);
        DefaultReplaceTokenProvider instance = new DefaultReplaceTokenProvider(p);
        FileObject file = d.getFileObject("src/test/java/p1/FirstTest.java");
        String converted = instance.convert(ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(file));
        assertNull(converted);
        converted = instance.convert(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, Lookups.singleton(file));
        assertNull(converted);
        DataObject dob = DataObject.find(file);
        converted = instance.convert(ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(dob));
        assertNull(converted);
        converted = instance.convert(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, Lookups.singleton(dob));
        assertNull(converted);
        file = d.getFileObject("src/test/java/p1/FirstIT.java");
        converted = instance.convert(ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(file));
        assertEquals(ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE, converted);
        dob = DataObject.find(file);
        converted = instance.convert(ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(dob));
        assertEquals(ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE, converted);
    }

    public void testNgSingle() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        TestFileUtils.writeFile(d, "src/test/java/p1/FirstNGTest.java", "package p1; class FirstNGTest {}");
        TestFileUtils.writeFile(d, "src/main/java/p1/First.java", "package p1; class First {}");

        Project p = ProjectManager.getDefault().findProject(d);
        assertEquals("p1.FirstNGTest", ActionProviderImpl.replacements(p, ActionProvider.COMMAND_TEST_SINGLE, Lookups.singleton(d.getFileObject("src/main/java/p1/First.java"))).get(DefaultReplaceTokenProvider.PACK_CLASSNAME));

    }

    public void testIntegration2Files() throws Exception {
        pomWithHelperPluginAndExtraTestFolder();
        TestFileUtils.writeFile(d, "src/extra-test/java/p1/ItTest.java",
                "//license\n"
                + "package p1;\n"
                + "class ItTest {}");
        TestFileUtils.writeFile(d, "src/extra-test/java/p1/a/b/c/ItTestInlined.java",
                "/*a comment*/ package p1.a. b.c ; class ItTestInlined {}"); //spabe between a and b is allowed

        Project p = ProjectManager.getDefault().findProject(d);
        DefaultReplaceTokenProvider instance = new DefaultReplaceTokenProvider(p);
        Map<String, String> replacements = instance.createReplacements(ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE,
                Lookups.fixed(
                        d.getFileObject("src/extra-test/java/p1/ItTest.java"),
                        d.getFileObject("src/extra-test/java/p1/a/b/c/ItTestInlined.java"))
        );

        final String packageClassName = replacements.get("packageClassName");
        assertTrue(packageClassName.contains("p1.ItTest"));
        assertTrue(packageClassName.contains("p1.a.b.c.ItTestInlined"));
    }

    private void pomWithHelperPluginAndExtraTestFolder() throws IOException {
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>0</version><build>"
                + "<plugins><plugin><groupId>org.codehaus.mojo</groupId><artifactId>build-helper-maven-plugin</artifactId>"
                + "<executions><execution><id>add-test-source</id><phase>generate-test-sources</phase><goals>"
                + "<goal>add-test-source</goal></goals><configuration><sources><source>src/extra-test/java</source></sources>"
                + "</configuration></execution></executions></plugin></plugins></build></project>");
    }

    public void testIntegrationPackage() throws Exception {
        pomWithHelperPluginAndExtraTestFolder();
        TestFileUtils.writeFile(d, "src/extra-test/java/p1/a/b/c/ItTestInlined.java",
                "/*a comment*/ package p1.a. b.c ; class ItTestInlined {}"); //space between a and b is allowed

        Project p = ProjectManager.getDefault().findProject(d);

        DefaultReplaceTokenProvider instance = new DefaultReplaceTokenProvider(p);
        Map<String, String> replacements = instance.createReplacements(ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE,
                Lookups.fixed(
                        d.getFileObject("src/extra-test/java/p1/a/b/c/"))
        );

        final String packageClassName = replacements.get("packageClassName");
        assertTrue(packageClassName.contains("p1.a.b.c.**"));
    }

    public void testIntegration2FilesAndPacakge() throws Exception {
        pomWithHelperPluginAndExtraTestFolder();
        TestFileUtils.writeFile(d, "src/extra-test/java/p1/ItTest.java",
                "//license\n"
                + "package p1;\n"
                + "class ItTest {}");
        TestFileUtils.writeFile(d, "src/extra-test/java/p1/a/b/c/ItTestInlined.java",
                "/*a comment*/ package p1.a. b.c ; class ItTestInlined {}"); //space between a and b is allowed
        TestFileUtils.writeFile(d, "src/extra-test/java/p1/d/ItTest3.java",
                "package p1.d;\nclass ItTest3 {}");

        Project p = ProjectManager.getDefault().findProject(d);
        DefaultReplaceTokenProvider instance = new DefaultReplaceTokenProvider(p);
        Map<String, String> replacements = instance.createReplacements(ActionProviderImpl.COMMAND_INTEGRATION_TEST_SINGLE,
                Lookups.fixed(
                        d.getFileObject("src/extra-test/java/p1/ItTest.java"),
                        d.getFileObject("src/extra-test/java/p1/a/b/c/ItTestInlined.java"),
                        d.getFileObject("src/extra-test/java/p1/d/"))
        );

        final String packageClassName = replacements.get("packageClassName");
        assertTrue(packageClassName.contains("p1.ItTest"));
        assertTrue(packageClassName.contains("p1.a.b.c.ItTestInlined"));
        assertTrue(packageClassName.contains("p1.d.**"));
    }
}
