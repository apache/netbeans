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

package org.netbeans.modules.web.freeform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tests for FreeformProjectGenerator.
 *
 * @author Pavel Buzek
 */
public class WebProjectGeneratorTest extends NbTestCase {

    private File lib1;
    private File lib2;
    private File src;
    private File web;
    private File doc;
    private File buildClasses;
    
    public WebProjectGeneratorTest(String testName) {
        super(testName);
    }
    
        protected void setUp() throws Exception {
            super.setUp();
            Lookup.getDefault().lookup(ModuleInfo.class);
            clearWorkDir();
        }
    
    private AntProjectHelper createEmptyProject(String projectFolder, String projectName) throws Exception {
        File base = new File(getWorkDir(), projectFolder);
        base.mkdir();
        File antScript = new File(base, "build.xml");
        antScript.createNewFile();
        src = new File(base, "src");
        src.mkdir();
        web = new File(base, "web");
        web.mkdir();
        doc = new File(base, "doc");
        doc.mkdir();
        buildClasses = new File(base, "buildClasses");
        buildClasses.mkdir();
        File libs = new File(base, "libs");
        libs.mkdir();
        lib1 = new File(libs, "some.jar");
        createRealJarFile(lib1);
        lib2 = new File(libs, "some2.jar");
        createRealJarFile(lib2);
        
        ArrayList webModules = new ArrayList ();
        WebProjectGenerator.WebModule wm = new WebProjectGenerator.WebModule ();
        wm.docRoot = web.getAbsolutePath();
        wm.contextPath = "/context";
        wm.j2eeSpecLevel = WebModule.J2EE_14_LEVEL;
        wm.classpath = base.getAbsolutePath() + "/buildClasses:" + lib1.getAbsolutePath();
        webModules.add (wm);
        
        AntProjectHelper helper = FreeformProjectGenerator.createProject(base, base, projectName, null);
        WebProjectGenerator.putWebModules(helper, Util.getAuxiliaryConfiguration(helper), webModules);
        return helper;
    }
    
    public void testWebModules () throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj2", "proj-2");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        WebModule wm = WebModule.getWebModule(FileUtil.toFileObject(web));
        assertNotNull("WebModule not found", wm);
        assertEquals("correct document base", FileUtil.toFileObject(web), wm.getDocumentBase());
        assertEquals("correct j2ee version", WebModule.J2EE_14_LEVEL, wm.getJ2eePlatformVersion());
        assertEquals("correct context path", "/context", wm.getContextPath());
        WebModule wm2 = WebModule.getWebModule(FileUtil.toFileObject (src));
//        assertNotNull("WebModule not found", wm2);
//        assertEquals("the same wm for web and src folder", wm, wm2);
    }
    
    public void test2WebModules () throws Exception {
        clearWorkDir();
        AntProjectHelper helper = createEmptyProject("proj6", "proj-6");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        File src2 = FileUtil.toFile (base.createFolder("src2"));
        File web2 = FileUtil.toFile (base.createFolder("web2"));

        AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
        List webModules = WebProjectGenerator.getWebmodules(helper, aux);
        WebProjectGenerator.WebModule wm = new WebProjectGenerator.WebModule ();
        wm.docRoot = web2.getAbsolutePath();
        wm.contextPath = "/context2";
        wm.j2eeSpecLevel = WebModule.J2EE_13_LEVEL;
        wm.classpath = FileUtil.toFile (base).getAbsolutePath() + "/buildClasses2:" + lib2.getAbsolutePath();
        webModules.add (wm);
        WebProjectGenerator.putWebModules(helper, aux, webModules);
        ProjectManager.getDefault().saveProject(p);
        
        WebModule webModule = WebModule.getWebModule(base.getFileObject("web2"));
        assertNotNull("WebModule not found", webModule);
        assertEquals("correct document base", base.getFileObject("web2"), webModule.getDocumentBase());
        WebModule webModule2 = WebModule.getWebModule(base.getFileObject("src2"));
//        assertNotNull("WebModule not found", webModule2);
//        assertEquals("correct document base", webModule, webModule2);
    }
    
    public void testWebDataUpgrades() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj");
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project should be created", p);
        assertEquals("Project folder should be correct", base, p.getProjectDirectory());
        
        // start with a /1-friendly data set
        List<WebProjectGenerator.WebModule> webModules = new ArrayList<WebProjectGenerator.WebModule>();
        WebProjectGenerator.WebModule webModule = new WebProjectGenerator.WebModule();
        webModule.docRoot = "web";
        webModule.webInf = webModule.docRoot + "/WEB-INF";
        webModule.j2eeSpecLevel = "1.5";
        webModule.contextPath = "mymodule";
        webModules.add(webModule);
        
        AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
        WebProjectGenerator.putWebModules(helper, aux, webModules);
        
        // check that the correct /1 data was saved
        Element el = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true);
        assertNotNull("Web modules should be saved in /1",  el);
        List<Element> subElements = XMLUtil.findSubElements(el);
        assertEquals(1, subElements.size());
        
        // compare the web module
        Element el2 = subElements.get(0);
        assertElement(el2, "web-module", null);
        assertElementArray(XMLUtil.findSubElements(el2),
            new String[] {"doc-root", "context-path", "j2ee-spec-level", "web-inf"},
            new String[] {"web",      "mymodule",     "1.5",             "web/WEB-INF"});
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
        
        // now check that setting web-inf to non-default value on that element forces a /2 save.
        webModules = new ArrayList<WebProjectGenerator.WebModule>();
        webModule = new WebProjectGenerator.WebModule();
        webModule.docRoot = "web";
        webModule.webInf = "somewhereelse/WEB-INF";
        webModule.j2eeSpecLevel = "1.5";
        webModule.contextPath = "mymodule";
        webModules.add(webModule);
        WebProjectGenerator.putWebModules(helper, aux, webModules);
        
        // check that we now have it in /2
        el = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true);
        assertNull("No /1 data should exist.", el);
        el = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true);
        assertNotNull("Should have /2 data.", el);
        subElements = XMLUtil.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare the web module
        el2 = subElements.get(0);
        assertElement(el2, "web-module", null);
        assertElementArray(XMLUtil.findSubElements(el2),
            new String[] {"doc-root", "context-path", "j2ee-spec-level", "web-inf"},
            new String[] {"web",      "mymodule",     "1.5",             "somewhereelse/WEB-INF"});
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
        
        // now try fresh save of /2-requiring data (using web-inf).
        assertTrue("Should removed /2 data", aux.removeConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true));
        webModules = new ArrayList<WebProjectGenerator.WebModule>();
        webModule = new WebProjectGenerator.WebModule();
        webModule.docRoot = "web";
        webModule.webInf = "somewhereelse/WEB-INF";
        webModule.j2eeSpecLevel = "1.5";
        webModule.contextPath = "mymodule";
        webModules.add(webModule);
        WebProjectGenerator.putWebModules(helper, aux, webModules);
        
        // check that we have it in /2
        el = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true);
        assertNull("No /1 data should exist.", el);
        el = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true);
        assertNotNull("Should have /2 data.", el);
        subElements = XMLUtil.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare the web module
        el2 = subElements.get(0);
        assertElement(el2, "web-module", null);
        assertElementArray(XMLUtil.findSubElements(el2),
            new String[] {"doc-root", "context-path", "j2ee-spec-level", "web-inf"},
            new String[] {"web",      "mymodule",     "1.5",             "somewhereelse/WEB-INF"});
        // validate against schema:
        ProjectManager.getDefault().saveAllProjects();
        validate(p);
    }
    
    //Tests issue: #147128:J2SESources does not register new external roots immediately
    public void testProjectFromExtSourcesOwnsTheSources() throws Exception
    {
        AntProjectHelper helper = createEmptyProject("proj-testFileOwner", "proj-testFileOwner");

        File root = FileUtil.toFile(helper.getProjectDirectory());
        File srcRoot = new File (root, "src");
        File testRoot = new File (root, "test");

        final Project expected = FileOwnerQuery.getOwner(helper.getProjectDirectory());
        assertNotNull(expected);
        assertEquals(expected, FileOwnerQuery.getOwner(srcRoot.toURI()));
        assertEquals(expected, FileOwnerQuery.getOwner(testRoot.toURI()));
    }

    // create real Jar otherwise FileUtil.isArchiveFile returns false for it
    public void createRealJarFile(File f) throws Exception {
        OutputStream os = new FileOutputStream(f);
        try {
            JarOutputStream jos = new JarOutputStream(os);
            JarEntry entry = new JarEntry("foo.txt");
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
    }
    
    /**
     * Asserts that given Element has expected name and its text match expected value.
     * @param element element to test
     * @param expectedName expected name of element; cannot be null
     * @param expectedValue can be null in which case value is not tested
     */
    public static void assertElement(Element element, String expectedName, String expectedValue) {
        String message = "Element "+element+" does not match [name="+expectedName+",value="+expectedValue+"]"; // NOI18N
        assertEquals(message, expectedName, element.getLocalName());
        if (expectedValue != null) {
            assertEquals(message, expectedValue, XMLUtil.findText(element));
        }
    }

    /**
     * See {@link #assertElement(Element, String, String)} for more details. This 
     * method does exactly the same just on the list of elements and expected names. 
     */
    public static void assertElementArray(List<Element> elements, String[] expectedNames, String[] expectedValues) {
        for (int i=0; i<elements.size(); i++) {
            assertElement(elements.get(i), expectedNames[i], expectedValues[i]);
        }
    }
    
    /**
     * Asserts that given Element has expected name and its text match expected value and
     * it also has expect attribute with expected value.
     * @param element element to test
     * @param expectedName expected name of element; cannot be null
     * @param expectedValue can be null in which case value is not tested
     * @param expectedAttrName expected name of attribute; cannot be null
     * @param expectedAttrValue expected value of attribute; cannot be null
     */
    public static void assertElement(Element element, String expectedName, String expectedValue, String expectedAttrName, String expectedAttrValue) {
        String message = "Element "+element+" does not match [name="+expectedName+",value="+
            expectedValue+", attr="+expectedAttrName+", attrvalue="+expectedAttrValue+"]"; // NOI18N
        assertEquals(message, expectedName, element.getLocalName());
        if (expectedValue != null) {
            assertEquals(message, expectedValue, XMLUtil.findText(element));
        }
        String val = element.getAttribute(expectedAttrName);
        assertEquals(expectedAttrValue, val);
    }
    
    /**
     * See {@link #assertElement(Element, String, String)} for more details. This 
     * method does exactly the same just on the list of elements and expected names
     * and expected attributes.
     */
    public static void assertElementArray(List<Element> elements, String[] expectedNames, String[] expectedValues, String[] expectedAttrName, String[] expectedAttrValue) {
        assertEquals(expectedNames.length, elements.size());
        for (int i=0; i<elements.size(); i++) {
            assertElement(elements.get(i), expectedNames[i], expectedValues[i], expectedAttrName[i], expectedAttrValue[i]);
        }
    }
    
    public static void validate(Project proj) throws Exception {
        File projF = FileUtil.toFile(proj.getProjectDirectory());
        File xml = new File(new File(projF, "nbproject"), "project.xml");
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setValidating(true);
        SAXParser p = f.newSAXParser();
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
            "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", getSchemas());
        try {
            p.parse(xml.toURI().toString(), new Handler());
        } catch (SAXParseException e) {
            assertTrue("Validation of XML document "+xml+" against schema failed. Details: "+
            e.getSystemId() + ":" + e.getLineNumber() + ": " + e.getLocalizedMessage(), false);
        }
    }
    
    private static String[] getSchemas() throws Exception {
        return new String[] {
            FreeformProjectGenerator.class.getResource("resources/freeform-project-general.xsd").toExternalForm(),
            WebProjectGenerator.class.getResource("resources/freeform-project-web.xsd").toExternalForm(),
            WebProjectGenerator.class.getResource("resources/freeform-project-web-2.xsd").toExternalForm(),
            AntBasedProjectFactorySingleton.class.getResource("project.xsd").toExternalForm(),
        };
    }
    
    private static final class Handler extends DefaultHandler {
        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
    
}
