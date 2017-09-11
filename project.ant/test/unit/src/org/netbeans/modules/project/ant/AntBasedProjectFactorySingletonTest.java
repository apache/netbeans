/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.project.ant;

import java.io.IOException;
import java.lang.reflect.Method;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelperTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

public class AntBasedProjectFactorySingletonTest extends NbTestCase {

    public AntBasedProjectFactorySingletonTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;

    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Services");
        if (fo != null) {
            fo.delete();
        }
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/project.xml"), projdir, "nbproject/project.xml");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/private.xml"), projdir, "nbproject/private/private.xml");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/project.properties"), projdir, "nbproject/project.properties");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/private.properties"), projdir, "nbproject/private/private.properties");
        TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/global.properties"), scratch, "userdir/build.properties");
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
    }

    /**Test for second part of #42738.
     */
    public void testAntBasedProjectTypesChanged() throws Exception {
        AntBasedProjectType type1 = AntBasedTestUtil.testAntBasedProjectType();
        AntBasedProjectType type2 = AntBasedTestUtil.testAntBasedProjectType();
        MockLookup.setInstances(type1, type2);
        Method getAntBasedProjectTypeMethod = AntProjectHelper.class.getDeclaredMethod("getType", new Class[0]);
        getAntBasedProjectTypeMethod.setAccessible(true);
        Project p = ProjectManager.getDefault().findProject(projdir);
        AntProjectHelper helper = p.getLookup().lookup(AntProjectHelper.class);
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper) == type2);
        MockLookup.setInstances(type1);
        p = ProjectManager.getDefault().findProject(projdir);
        helper = p.getLookup().lookup(AntProjectHelper.class);
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper) == type1);
        MockLookup.setInstances(type2);
        p = ProjectManager.getDefault().findProject(projdir);
        helper = p.getLookup().lookup(AntProjectHelper.class);
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper) == type2);
        MockLookup.setInstances();
        assertNull(ProjectManager.getDefault().findProject(projdir));
        MockLookup.setInstances(type1, type2);
        assertTrue(getAntBasedProjectTypeMethod.invoke(helper) == type2);
    }

    public void testDoNotLoadInvalidProject() throws Exception {
        String content = projdir.getFileObject("nbproject/project.xml").asText("UTF-8");
        TestFileUtils.writeFile(projdir, "nbproject/project.xml", content.replace("</project>", "<bogus/>\n</project>"));
        try {
            ProjectManager.getDefault().findProject(projdir);
            fail("should not have successfully loaded an invalid project.xml");
        } catch (IOException x) {
            assertTrue(x.toString(), x.getMessage().contains("bogus"));
            // #142079: use simplified error message.
            String loc = Exceptions.findLocalizedMessage(x);
            assertNotNull(loc);
            assertTrue(loc, loc.contains("bogus"));
            assertTrue(loc, loc.contains("project.xml"));
            // Probably should not assert exact string, as this is dependent on parser.
        }
    }

    public void testCorrectableInvalidProject() throws Exception { // #143966
        clearWorkDir();
        TestFileUtils.writeFile(FileUtil.getConfigRoot(), "ProjectXMLCatalog/foo/1.xsd",
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                "            targetNamespace='http://www.netbeans.org/ns/foo/1'\n" +
                "            xmlns='http://www.netbeans.org/ns/foo/1'\n" +
                "            elementFormDefault='qualified'>\n" +
                " <xsd:element name='data'>\n" +
                "  <xsd:complexType>\n" +
                "   <xsd:sequence>\n" +
                "    <xsd:element name='a' minOccurs='0'/>\n" +
                "    <xsd:element name='b' maxOccurs='unbounded'/>\n" +
                "    <xsd:element name='c' maxOccurs='unbounded'/>\n" +
                "   </xsd:sequence>\n" +
                "  </xsd:complexType>\n" +
                " </xsd:element>\n" +
                "</xsd:schema>");
        TestFileUtils.writeFile(FileUtil.getConfigRoot(), "ProjectXMLCatalog/foo/2.xsd",
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
                "            targetNamespace='http://www.netbeans.org/ns/foo/2'\n" +
                "            xmlns='http://www.netbeans.org/ns/foo/2'\n" +
                "            elementFormDefault='qualified'>\n" +
                " <xsd:element name='data'>\n" +
                "  <xsd:complexType>\n" +
                "   <xsd:sequence>\n" +
                "    <xsd:element name='a' minOccurs='0'/>\n" +
                "    <xsd:element name='b' maxOccurs='unbounded'/>\n" +
                "    <xsd:element name='c' maxOccurs='unbounded'/>\n" +
                "    <xsd:element name='d'/>\n" +
                "   </xsd:sequence>\n" +
                "  </xsd:complexType>\n" +
                " </xsd:element>\n" +
                "</xsd:schema>");
        MockLookup.setInstances(new AntBasedProjectType() {
            public String getType() {
                return "test";
            }
            public Project createProject(final AntProjectHelper helper) throws IOException {
                return new Project() {
                    public FileObject getProjectDirectory() {
                        return helper.getProjectDirectory();
                    }
                    public Lookup getLookup() {
                        return Lookups.singleton(helper);
                    }
                };
            }
            public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
                return "http://www.netbeans.org/ns/foo/2";
            }
            public String getPrimaryConfigurationDataElementName(boolean shared) {
                return "data";
            }
        });
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "p1/nbproject/project.xml",
                "<project xmlns='http://www.netbeans.org/ns/project/1'>\n" +
                " <type>test</type>\n" +
                " <configuration>\n" +
                "  <data xmlns='http://www.netbeans.org/ns/foo/2'>\n" +
                "   <a/>\n" +
                "   <c/>\n" +
                "   <c/>\n" +
                "   <b/>\n" +
                "   <b/>\n" +
                "   <d/>\n" +
                "  </data>\n" +
                " </configuration>\n" +
                "</project>");
        Project p = ProjectManager.getDefault().findProject(d.getFileObject("p1"));
        AntProjectHelper helper = p.getLookup().lookup(AntProjectHelper.class);
        Element data = helper.getPrimaryConfigurationData(true);
        assertEquals("a2 b2 b2 c2 c2 d2", namesOfChildren(data));
        TestFileUtils.writeFile(d, "p2/nbproject/project.xml",
                "<project xmlns='http://www.netbeans.org/ns/project/1'>\n" +
                " <type>test</type>\n" +
                " <configuration>\n" +
                "  <data xmlns='http://www.netbeans.org/ns/foo/1'>\n" +
                "   <a/>\n" +
                "   <b/>\n" +
                "   <c/>\n" +
                "   <d/>\n" +
                "  </data>\n" +
                " </configuration>\n" +
                "</project>");
        p = ProjectManager.getDefault().findProject(d.getFileObject("p2"));
        helper = p.getLookup().lookup(AntProjectHelper.class);
        data = helper.getPrimaryConfigurationData(true);
        assertEquals("http://www.netbeans.org/ns/foo/2", data.getNamespaceURI());
        assertEquals("a2 b2 c2 d2", namesOfChildren(data));
        TestFileUtils.writeFile(d, "p3/nbproject/project.xml",
                "<project xmlns='http://www.netbeans.org/ns/project/1'>\n" +
                " <type>test</type>\n" +
                " <configuration>\n" +
                "  <data xmlns='http://www.netbeans.org/ns/foo/2'>\n" +
                "   <b/>\n" +
                "   <b/>\n" +
                "   <a/>\n" +
                "   <c/>\n" +
                "   <c/>\n" +
                "   <d/>\n" +
                "  </data>\n" +
                " </configuration>\n" +
                "</project>");
        p = ProjectManager.getDefault().findProject(d.getFileObject("p3"));
        helper = p.getLookup().lookup(AntProjectHelper.class);
        data = helper.getPrimaryConfigurationData(true);
        assertEquals("a2 b2 b2 c2 c2 d2", namesOfChildren(data));
    }
    private static String namesOfChildren(Element e) {
        StringBuilder b = new StringBuilder();
        for (Element kid : XMLUtil.findSubElements(e)) {
            if (b.length() > 0) {
                b.append(' ');
            }
            b.append(kid.getLocalName());
            String ns = kid.getNamespaceURI();
            b.append(ns.charAt(ns.length() - 1));
        }
        return b.toString();
    }

}
