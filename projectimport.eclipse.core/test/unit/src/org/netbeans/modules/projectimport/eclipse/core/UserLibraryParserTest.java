/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

/**
 * @author Martin Krauskopf
 */
public class UserLibraryParserTest extends NbTestCase {
    
    public UserLibraryParserTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    
    public void testGetJars_75112() throws Exception {
        String xmlDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<userlibrary systemlibrary=\"true\" version=\"1\">\n" +
                "\t<archive sourceattachment=\"/space/java/0_lib\" path=\"/space/java/0_lib/cb2.jar\">\n" +
                "\t\t<attributes>\n" +
                "\t\t\t<attribute value=\"/space/java/0_lib\" name=\"org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY\"/>\n" +
                "\t\t</attributes>\n" +
                "\t</archive>\n" +
                "\t<archive path=\"/space/java/0_lib/commons-collections-2.1.jar\"/>\n" +
                "\t<archive path=\"/space/java/0_lib/commons-digester-1.4.1.jar\"/>\n" +
                "</userlibrary>\n";
        List<String> jars = new ArrayList<String>();
        List<String> sources = new ArrayList<String>();
        List<String> javadoc = new ArrayList<String>();
        UserLibraryParser.getJars("aName", xmlDoc, jars, javadoc, sources);
        assertEquals("three classpath entries", 3, jars.size());
        assertEquals("one sources entries", 1, sources.size());
        assertEquals("no javadoc entries", 0, javadoc.size());
    }
    
    public void testGetJars_JavadocAndSources() throws Exception {
        File repo = new File(getWorkDir(), "repo");
        repo.mkdir();
        File w = new File(getWorkDir(), "workspace");
        w.mkdir();
        Workspace workspace = EclipseProjectTestUtils.createWorkspace(w, new Workspace.Variable("REPO", repo.getPath()));
        File f = new File(getWorkDir(), "a-project");
        new File(f, "sources1").mkdirs();
        new File(f, "libs").mkdirs();
        TestFileUtils.writeZipFile(new File(f, "javadoc1.jar"), "path/index.html:blank");
        f.mkdir();
        File aZip = new File(repo, "source.zip");
        TestFileUtils.writeZipFile(aZip, "a.smth:content");
        File libraries = new File(repo, "libraries");
        libraries.mkdir();
        File aZip2 = new File(libraries, "source2.zip");
        TestFileUtils.writeZipFile(aZip2, "api/a.smth:content");
        DotClassPath dcp = new DotClassPath(new ArrayList<DotClassPathEntry>(), new ArrayList<DotClassPathEntry>(), null, null);
        EclipseProject eclipse = EclipseProjectTestUtils.createEclipseProject(f, dcp, workspace, "a project name", Collections.<Link>singletonList(new Link("libs/external", false, libraries.getPath())));
        
        String xmlDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<userlibrary systemlibrary=\"true\" version=\"1\">\n" +
                "\t<archive sourceattachment=\"/a project name/sources1\" path=\"/tmp/some.jar\">\n" +
                "\t\t<attributes>\n" +
                "\t\t\t<attribute value=\"jar:platform:/resource/a project name/javadoc1.jar!/path\" name=\"javadoc_location\"/>\n" +
                "\t\t</attributes>\n" +
                "\t</archive>\n" +
                "\t<archive sourceattachment=\""+aZip.getPath()+"\" path=\"/tmp/other.jar\">\n" +
                "\t\t<attributes>\n" +
                "\t\t\t<attribute value=\""+Utilities.toURI(repo).toURL()+"\" name=\"javadoc_location\"/>\n" +
                "\t\t</attributes>\n" +
                "\t</archive>\n" +
                "\t<archive sourceattachment=\"/a project name/libs/external/source2.zip\" path=\"/tmp/another.jar\">\n" +
                "\t\t<attributes>\n" +
                "\t\t\t<attribute value=\"jar:platform:/resource/a project name/libs/external/source2.zip!/api\" name=\"javadoc_location\"/>\n" +
                "\t\t</attributes>\n" +
                "\t</archive>\n" +
                "</userlibrary>\n";
        List<String> jars = new ArrayList<String>();
        List<String> sources = new ArrayList<String>();
        List<String> javadoc = new ArrayList<String>();
        UserLibraryParser.getJars("aName", xmlDoc, jars, javadoc, sources);
        assertEquals("three classpath entries", 3, jars.size());
        assertEquals("three sources entries", 3, sources.size());
        assertEquals("three javadoc entries", 3, javadoc.size());
        String libName = "Some Library";
        workspace.addUserLibrary(libName, jars, javadoc, sources);
        List<URL> urls = workspace.getJavadocForUserLibrary(libName, new ArrayList<String>());
        assertEquals(3, urls.size());
        String baseURLPath = Utilities.toURI(getWorkDir()).toURL().toExternalForm();
        assertEquals("jar:"+baseURLPath+"a-project/javadoc1.jar!/path/", urls.get(0).toExternalForm());
        assertEquals(baseURLPath+"repo/", urls.get(1).toExternalForm());
        assertEquals("jar:"+baseURLPath+"repo/libraries/source2.zip!/api/", urls.get(2).toExternalForm());
        urls = workspace.getSourcesForUserLibrary(libName, new ArrayList<String>());
        assertEquals(3, urls.size());
        assertEquals(baseURLPath+"a-project/sources1/", urls.get(0).toExternalForm());
        assertEquals("jar:"+baseURLPath+"repo/source.zip!/", urls.get(1).toExternalForm());
        assertEquals("jar:"+baseURLPath+"repo/libraries/source2.zip!/", urls.get(2).toExternalForm());
    }
}
