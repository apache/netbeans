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

package org.netbeans.modules.ant.freeform.spi.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author David Konecny
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testAuxiliaryConfiguration() throws Exception {
        File proj = new File(getWorkDir(), "aux_proj");
        proj.mkdir();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(proj, proj, "proj1", null);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("project was created", p);
        assertEquals("expected project folder", base, p.getProjectDirectory());
        
        AuxiliaryConfiguration au = Util.getAuxiliaryConfiguration(helper);
        assertNotNull("project has AuxiliaryConfiguration", au);
    }
    
    public void testRelativizeLocation() throws Exception {
        File srcApp = Utilities.isWindows() ? new File("c:\\src\\app") : new File("/src/app");
        File srcAppFooBar = new File(srcApp, "foo" + File.separatorChar + "bar");
        File projApp = Utilities.isWindows() ? new File("c:\\proj\\app") : new File("/proj/app");
        File otherFooBar = Utilities.isWindows() ? new File("c:\\other\\foo\\bar") : new File("/other/foo/bar");
        assertEquals("foo/bar", Util.relativizeLocation(srcApp, srcApp, srcAppFooBar));
        assertEquals("${project.dir}/foo/bar", Util.relativizeLocation(srcApp, projApp, srcAppFooBar));
        assertEquals(otherFooBar.getAbsolutePath(), Util.relativizeLocation(srcApp, srcApp, otherFooBar));
        assertEquals(otherFooBar.getAbsolutePath(), Util.relativizeLocation(srcApp, projApp, otherFooBar));
        // Mentioned incidentally in #54428:
        assertEquals(".", Util.relativizeLocation(srcApp, srcApp, srcApp));
        assertEquals("${project.dir}", Util.relativizeLocation(srcApp, projApp, srcApp));
    }

    public void testGetDefaultAntScript() throws Exception {
        assertNull("no default ant script", Util.getDefaultAntScript(extsrcroot));
        assertEquals("found build.xml", simple.getProjectDirectory().getFileObject("build.xml"), Util.getDefaultAntScript(simple));
        assertEquals("found build.xml", extbuildscript.getProjectDirectory().getFileObject("scripts/build.xml"), Util.getDefaultAntScript(extbuildscript));
    }

    public void testFormatUpgrade() throws Exception {
        AntProjectHelper helper = FreeformProjectGenerator.createProject(getWorkDir(), getWorkDir(), "prj", null);
        Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        FileObject pxml = helper.resolveFileObject(AntProjectHelper.PROJECT_XML_PATH);
        // To simplify test, overwrite project.xml with a basic version w/o <properties>, <view>, or <comment>.
        Element data = helper.getPrimaryConfigurationData(true);
        data = data.getOwnerDocument().createElementNS(data.getNamespaceURI(), data.getLocalName());
        data.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "name")).appendChild(data.getOwnerDocument().createTextNode("prj"));
        helper.putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(p);
        // Initial check.
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</></>", xmlSimplified(data));
        // Save something in a /1-compatible format.
        Element folder = (Element) data.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "folders")).
                appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "source-folder"));
        folder.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "label")).appendChild(data.getOwnerDocument().createTextNode("Sources"));
        folder.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "location")).appendChild(data.getOwnerDocument().createTextNode("src"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</>" +
                "<folders><source-folder><label>Sources</><location>src</></></></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</><folders><source-folder><label>Sources</><location>src</></></></>", xmlSimplified(data));
        // Save something that forces use of the /2 format.
        data.getElementsByTagName("source-folder").item(0).
                appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "excludes")).
                appendChild(data.getOwnerDocument().createTextNode("junk/"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</></><general-data/ff2><name>prj</>" +
                "<folders><source-folder><label>Sources</><location>src</><excludes>junk/</></></></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</><folders><source-folder><label>Sources</><location>src</><excludes>junk/</></></></>", xmlSimplified(data));
        // Save something old again.
        Element excludes = (Element) data.getElementsByTagName("excludes").item(0);
        excludes.getParentNode().removeChild(excludes);
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</>" +
                "<folders><source-folder><label>Sources</><location>src</></></></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</><folders><source-folder><label>Sources</><location>src</></></></>", xmlSimplified(data));
    }
    private static String xmlSimplified(Element e) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.importNode(e, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        return xmlSimplified(baos.toString("UTF-8"));
    }
    private static String xmlSimplified(FileObject f) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = f.getInputStream();
        try {
            FileUtil.copy(is, baos);
        } finally {
            is.close();
        }
        return xmlSimplified(baos.toString("UTF-8"));
    }
    private static String xmlSimplified(String s) throws Exception {
        return s.replaceFirst("^<\\?xml.+\\?>", "").
                replaceAll("(\r|\n|\r\n) *", "").
                replace(" xmlns=\"http://www.netbeans.org/ns/project/1\"", "/p1").
                replaceAll(" xmlns=\"http://www\\.netbeans\\.org/ns/freeform-project/(\\d+)\"", "/ff$1").
                replaceAll("</[^>]+>", "</>").
                replace("<type>org.netbeans.modules.ant.freeform</>", "<type.../>");
    }
    
}
