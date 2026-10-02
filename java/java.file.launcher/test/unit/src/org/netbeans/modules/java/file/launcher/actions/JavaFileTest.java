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
package org.netbeans.modules.java.file.launcher.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sarvesh Kesharwani
 */
public class JavaFileTest extends NbTestCase {

    private FileObject javaFO;

    public JavaFileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File f1 = new File(getWorkDir(), "TestSingleJavaFile.java");
        try (FileWriter w = new FileWriter(f1)) {
            w.write(
                    """
                public class TestSingleJavaFile {
                    public static void main (String args[]) {
                        System.out.print("hello world");
                    }
                }
                """
            );
        }
        javaFO = FileUtil.toFileObject(f1);
        assertNotNull("FileObject found: " + f1, javaFO);
    }



    public void testSingleJavaSourceRun() throws Exception {
        SingleJavaSourceRunActionProvider runActionProvider = new SingleJavaSourceRunActionProvider();
        LaunchProcess process = runActionProvider.invokeActionHelper("run.single", javaFO, ExplicitProcessParameters.empty());
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(process.call().getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String result = builder.toString();
        assertEquals("hello world", result);
        FileObject[] siblings = javaFO.getParent().getChildren();
        if (isJDK11OrNewer()) {
            assertEquals("No other sibling", 1, siblings.length);
            assertEquals(javaFO, siblings[0]);
        } else {
            assertEquals("One other sibling created", 2, siblings.length);
            if (javaFO.equals(siblings[0])) {
                assertEquals("TestSingleJavaFile.class", siblings[1].getNameExt());
            } else {
                assertEquals("TestSingleJavaFile.class", siblings[0].getNameExt());
            }
        }
    }
    public void testSingleFileRunAvailableInNoProject() throws Exception {
        FileObject fallbackDir = FileUtil.toFileObject(getWorkDir());

        Lookup lkp = Lookups.fixed(javaFO);

        SingleJavaSourceRunActionProvider ap = new SingleJavaSourceRunActionProvider();
        boolean runEnabled = ap.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, lkp);
        assertTrue("Run is enabled in Java-less project", runEnabled);
        boolean debugEnabled = ap.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, lkp);
        assertTrue("Run is enabled in Java-less project", debugEnabled);
    }

    public void testSingleFileRunAvailableInFallbackProject() throws Exception {
        FileObject fallbackDir = FileUtil.toFileObject(getWorkDir());
        Project fallback = ProjectManager.getDefault().findProjectOrFallback(fallbackDir);
        assertNotNull("Fallback project found", fallback);
        final SourceGroup[] srcGroups = ProjectUtils.getSources(fallback).getSourceGroups("java");
        assertEquals("No Java sources in there", 0, srcGroups.length);

        Lookup lkp = Lookups.fixed(javaFO);

        SingleJavaSourceRunActionProvider ap = new SingleJavaSourceRunActionProvider();
        boolean runEnabled = ap.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, lkp);
        assertTrue("Run is enabled in Java-less project", runEnabled);
        boolean debugEnabled = ap.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, lkp);
        assertTrue("Run is enabled in Java-less project", debugEnabled);
    }

    public void testSingleFileRunDisableInAMavenProject() throws Exception {
        FileObject mavenDir = FileUtil.toFileObject(getWorkDir());
        FileObject pom = mavenDir.createData("pom.xml");
        try (var os = pom.getOutputStream()) {
            os.write("""
            <project>
              <modelVersion>4.0.0</modelVersion>
              <groupId>com.example</groupId>
              <artifactId>my-app</artifactId>
              <version>1.0.0</version>
            </project>
            """.getBytes());
        }
        FileObject emptyJava = FileUtil.createData(mavenDir, "src/main/java/org/Empty.java");
        Project mavenPrj = ProjectManager.getDefault().findProject(mavenDir);
        assertNotNull("Found Maven project", mavenPrj);
        final SourceGroup[] srcGroups = ProjectUtils.getSources(mavenPrj).getSourceGroups("java");
        assertEquals("It has sources", 1, srcGroups.length);
        assertTrue("Empty.java belongs in there", srcGroups[0].contains(emptyJava));

        Lookup lkp = Lookups.fixed(javaFO);

        SingleJavaSourceRunActionProvider ap = new SingleJavaSourceRunActionProvider();
        boolean runEnabled = ap.isActionEnabled(ActionProvider.COMMAND_RUN_SINGLE, lkp);
        assertFalse("Single File Run is disabled in Java project", runEnabled);
        boolean debugEnabled = ap.isActionEnabled(ActionProvider.COMMAND_DEBUG_SINGLE, lkp);
        assertFalse("Single File Debug is disabled in Java project", debugEnabled);
    }

    private boolean isJDK11OrNewer() {
        return SingleSourceFileUtil.findJavaVersion() >= 11;
    }
}
