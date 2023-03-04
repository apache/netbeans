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

package org.netbeans.modules.web.jspparser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class WindowsLockingTest extends NbTestCase {

    private static final String ISSUE_WA_VENDOR = "Sun Microsystems";
    private static final int ISSUE_WA_MAJOR_VERSION = 1;
    private static final int ISSUE_WA_MINOR_VERSION = 6;

    public WindowsLockingTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setup(this);

        // we don't want tu mess up data dir
        File f = getWorkDir();
        FileObject source = FileUtil.toFileObject(
                FileUtil.normalizeFile(TestUtil.getProjectAsFile(this, "emptyWebProject")));
        FileObject dest = FileUtil.toFileObject(
                FileUtil.normalizeFile(f)).createFolder("emptyWebProject128360");
        TestUtil.copyFolder(source, dest);
    }

    public void testNotLockedIssue128360() throws IOException {
        String vendor = System.getProperty("java.vendor");
        if (!vendor.contains(ISSUE_WA_VENDOR)) {
            return;
        }
        String version = System.getProperty("java.version");
        System.out.println(version);

        String[] parts = version.split("\\.");
        if (parts.length < 2 || Integer.parseInt(parts[0]) < ISSUE_WA_MAJOR_VERSION
                || (Integer.parseInt(parts[0]) == ISSUE_WA_MAJOR_VERSION && Integer.parseInt(parts[1]) < ISSUE_WA_MINOR_VERSION)) {
            return;
        }

        FileObject project = FileUtil.toFileObject(
                FileUtil.normalizeFile(getWorkDir())).getFileObject("emptyWebProject128360");

        FileObject f = FileUtil.createFolder(project, "web/WEB-INF/lib");
        File jarFile = createJar(new File(FileUtil.toFile(f), "test.jar"));

        FileObject jspFo = project.getFileObject("web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        assertNotNull("The result from the parser was not obtained.", result);

        assertTrue("Empty jar file was locked.", jarFile.delete());
    }

    private File createJar(File file) throws IOException {
        JarOutputStream os = new JarOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)), new Manifest());
        os.close();

        return file;
    }
}
