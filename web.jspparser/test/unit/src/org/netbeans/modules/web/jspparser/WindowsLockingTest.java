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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
