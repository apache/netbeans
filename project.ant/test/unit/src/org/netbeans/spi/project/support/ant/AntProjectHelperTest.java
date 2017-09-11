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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.ProjectLibraryProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* XXX tests needed:
 * - testProjectXmlSavedException
 * try throwing an exception from PXSH.pXS and check that next save is OK
 * need to delete: assert !modifiedMetadataPaths.isEmpty();
 * - testMalformedConfigDataProducesLoadException
 * make a project with broken XML files and check that it does not load
 * - testCleanCheckout
 * make sure loading proceeds naturally with a clean checkout, i.e. no nbproject/private/ dir
 * and that appropriate private.xml and private.properties files are created on demand
 * (this is perhaps already tested adequately by ProjectGeneratorTest)
 * - testVCSFriendliness
 * make various modifications to project/private.xml files and ensure that the
 * number of lines changed in the diff is kept to a minimum
 * - testIsProject
 */

/**
 * Test functionality of AntProjectHelper and AntBasedProjectFactorySingleton.
 * @author Jesse Glick
 */
public class AntProjectHelperTest extends NbTestCase {

    /**
     * Create test suite.
     * @param name suite name
     */
    public AntProjectHelperTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private ProjectManager pm;
    private Project p;
    private AntProjectHelper h;
    private AntBasedTestUtil.TestListener l;
    
    protected @Override void setUp() throws Exception {
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
        ProjectProperties.antJar = new File(getWorkDir(), "ant/lib/ant.jar");
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        pm = ProjectManager.getDefault();
        p = pm.findProject(projdir);
        h = p.getLookup().lookup(AntProjectHelper.class);
        l = new AntBasedTestUtil.TestListener();
    }
    
    /**
     * Test that Ant-based projects are at least recognized as such.
     * @throws Exception if anything unexpected happens
     */
    public void testBasicRecognition() throws Exception {
        assertNotNull("recognized the project", p);
        assertEquals("correct project directory", projdir, p.getProjectDirectory());
        assertEquals("found something in project lookup", "hello", p.getLookup().lookup(String.class));
    }
    
    /**
     * Test that it is possible to retrieve the main data from project.xml and private.xml.
     * @throws Exception if anything unexpected happens
     */
    public void testGetPrimaryConfigurationData() throws Exception {
        assertNotNull("Had helper in lookup", h);
        Element data = h.getPrimaryConfigurationData(true);
        assertEquals("correct element name", "data", data.getLocalName());
        assertEquals("correct element namespace", "urn:test:shared", data.getNamespaceURI());
        Element stuff = XMLUtil.findElement(data, "shared-stuff", "urn:test:shared");
        assertNotNull("had nested stuff in it", stuff);
        data = h.getPrimaryConfigurationData(false);
        assertEquals("correct element name", "data", data.getLocalName());
        assertEquals("correct element namespace", "urn:test:private", data.getNamespaceURI());
        stuff = XMLUtil.findElement(data, "private-stuff", "urn:test:private");
        assertNotNull("had nested stuff in it", stuff);
    }
    
    /**
     * Test error recovery from malformed project.xml
     * @see "#46048"
     */
    public void testBrokenPrimaryConfigurationData() throws Exception {
        // Make an empty, thus invalid, project.xml:
        TestUtil.createFileFromContent(null, projdir, AntProjectHelper.PROJECT_XML_PATH);
        AntProjectHelper.QUIETLY_SWALLOW_XML_LOAD_ERRORS = true;
        Element data;
        try {
            data = h.getPrimaryConfigurationData(true);
        } finally {
            AntProjectHelper.QUIETLY_SWALLOW_XML_LOAD_ERRORS = false;
        }
        assertEquals("correct element name", "data", data.getLocalName());
        assertEquals("correct element namespace", "urn:test:shared", data.getNamespaceURI());
        Element stuff = XMLUtil.findElement(data, "shared-stuff", "urn:test:shared");
        /* This now retains the former contents:
        assertNull("had no stuff in it", stuff);
         */
        // Make sure a subsequent save proceeds normally too:
        data = XMLUtil.createDocument("whatever", "urn:test:shared", null, null).createElementNS("urn:test:shared", "data");
        data.appendChild(data.getOwnerDocument().createElementNS("urn:test:shared", "details"));
        h.putPrimaryConfigurationData(data, true);
        pm.saveProject(p);
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element root = doc.getDocumentElement();
        Element type = XMLUtil.findElement(root, "type", AntProjectHelper.PROJECT_NS);
        assertEquals("correct restored type", "test", XMLUtil.findText(type));
        Element config = XMLUtil.findElement(root, "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("have <configuration>", config);
        data = XMLUtil.findElement(config, "data", "urn:test:shared");
        assertNotNull("have <data>", data);
        Element details = XMLUtil.findElement(data, "details", "urn:test:shared");
        assertNotNull("have <details>", details);
    }
    
    /**
     * Test that after retrieving XML config data, you can't mess up other internal stuff.
     * @throws Exception if anything unexpected happens
     */
    public void testImmutabilityOfGottenConfigurationData() throws Exception {
        Element data = h.getPrimaryConfigurationData(true);
        assertNull("no parent for data", data.getParentNode());
        // XXX assure that modifications to data have no effect on a subsequent call
        // XXX get the ownerDocument and assure that the tree cannot be modified using it
    }
    
    /**
     * Test that it is possible to load properties from .properties files.
     * @throws Exception if anything unexpected happens
     */
    public void testGetProperties() throws Exception {
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("getProperties should never return null", ep);
        assertEquals("three properties defined", 3, ep.size());
        assertEquals("shared.prop correct", "value1", ep.get("shared.prop"));
        assertEquals("overridden.prop correct", "value3", ep.get("overridden.prop"));
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        assertNotNull("getProperties should never return null", ep);
        assertEquals("four properties defined", 4, ep.size());
        assertEquals("private.prop correct", "value2", ep.get("private.prop"));
        assertEquals("overridden.prop correct", "value4", ep.get("overridden.prop"));
        ep = h.getProperties("bogus/path.properties");
        assertNotNull("getProperties should never return null", ep);
        assertEquals("no properties defined", 0, ep.size());
    }
    
    /**
     * Test that Ant properties can be evaluated with proper (recursive) substitutions.
     * @throws Exception if anything unexpected happens
     */
    public void testStandardPropertyEvaluator() throws Exception {
        // Make sure any callbacks happen inside a lock, so changes are not posted asynch:
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
        PropertyEvaluator pev = h.getStandardPropertyEvaluator();
        assertEquals("shared.prop correct", "value1", pev.getProperty("shared.prop"));
        assertEquals("private.prop correct", "value2", pev.getProperty("private.prop"));
        assertEquals("overridden.prop correct", "value4", pev.getProperty("overridden.prop"));
        assertEquals("derived.prop correct", "value2:value1:${undefined.prop}", pev.getProperty("derived.prop"));
        assertEquals("tempdir correct", System.getProperty("java.io.tmpdir") + "/foo", pev.getProperty("tempdir"));
        assertEquals("global.prop correct", "value5", pev.getProperty("global.prop"));
        assertEquals("does not have other defs", null, pev.getProperty("bogus.prop"));
        Map m = pev.getProperties();
        assertEquals("shared.prop correct", "value1", m.get("shared.prop"));
        assertEquals("private.prop correct", "value2", m.get("private.prop"));
        assertEquals("overridden.prop correct", "value4", m.get("overridden.prop"));
        assertEquals("derived.prop correct", "value2:value1:${undefined.prop}", m.get("derived.prop"));
        assertEquals("tempdir correct", System.getProperty("java.io.tmpdir") + "/foo", m.get("tempdir"));
        assertEquals("global.prop correct", "value5", m.get("global.prop"));
        assertEquals("does not have other defs", null, m.get("bogus.prop"));
        assertEquals("correct evaluateString", "value1:value2",
            pev.evaluate("${shared.prop}:${private.prop}"));
        // #44213: try modifying build.properties.
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        pev.addPropertyChangeListener(l);
        FileObject buildProperties = scratch.getFileObject("userdir/build.properties");
        assertNotNull("have build.properties", buildProperties);
        OutputStream os = buildProperties.getOutputStream();
        Properties p = new Properties();
        p.setProperty("global.prop", "value5a");
        p.setProperty("global.prop.2", "globalvalue2");
        p.store(os, null);
        os.close();
        l.assertEvents("global.prop", "global.prop.2");
        assertEquals("global.prop is correct", "value5a", pev.getProperty("global.prop"));
        assertEquals("global.prop.2 is correct", "globalvalue2", pev.getProperty("global.prop.2"));
        // #42147: try modifying project.properties and private.properties on disk.
        FileObject projectProperties = projdir.getFileObject("nbproject/project.properties");
        assertNotNull("have project.properties", projectProperties);
        os = projectProperties.getOutputStream();
        p = new Properties();
        p.setProperty("overridden.prop", "value3a"); // different, but won't matter
        p.setProperty("shared.prop", "value1a"); // changed
        p.setProperty("derived.prop", "${private.prop}:${shared.prop}:${undefined.prop}"); // same literally
        p.store(os, null);
        os.close();
        l.assertEvents("shared.prop", "derived.prop");
        assertEquals("shared.prop is correct", "value1a", pev.getProperty("shared.prop"));
        assertEquals("derived.prop correct", "value2:value1a:${undefined.prop}", pev.getProperty("derived.prop"));
        FileObject privateProperties = projdir.getFileObject("nbproject/private/private.properties");
        assertNotNull("have private.properties", privateProperties);
        os = privateProperties.getOutputStream();
        p = new Properties();
        p.setProperty("private.prop", "value2a"); // changed
        p.setProperty("overridden.prop", "value4"); // same
        p.setProperty("tempdir", "${java.io.tmpdir}/foo"); // same
        p.setProperty("user.properties.file", "../userdir/build.properties"); // same
        p.store(os, null);
        os.close();
        l.assertEvents("private.prop", "derived.prop");
        assertEquals("private.prop is correct", "value2a", pev.getProperty("private.prop"));
        assertEquals("derived.prop correct", "value2a:value1a:${undefined.prop}", pev.getProperty("derived.prop"));
        // Try deleting project.properties and make sure its values are cleared.
        projectProperties.delete();
        l.assertEvents("shared.prop", "derived.prop");
        assertEquals("shared.prop is gone", null, pev.getProperty("shared.prop"));
        assertEquals("derived.prop is gone", null, pev.getProperty("derived.prop"));
        // Now recreate it.
        projectProperties = projdir.getFileObject("nbproject").createData("project.properties");
        os = projectProperties.getOutputStream();
        p = new Properties();
        p.setProperty("derived.prop", "${private.prop}:${shared.prop}:${undefined.prop.2}"); // restoring w/ changes
        p.store(os, null);
        os.close();
        l.assertEvents("derived.prop");
        assertEquals("derived.prop is back", "value2a:${shared.prop}:${undefined.prop.2}", pev.getProperty("derived.prop"));
        // #44213 cont'd: change user.properties.file and make sure the new definitions are read
        FileObject buildProperties2 = scratch.getFileObject("userdir").createData("build2.properties");
        os = buildProperties2.getOutputStream();
        p = new Properties();
        p.setProperty("global.prop", "value5b"); // modified
        p.setProperty("global.prop.2", "globalvalue2"); // same
        p.store(os, null);
        os.close();
        os = privateProperties.getOutputStream();
        p = new Properties();
        p.setProperty("private.prop", "value2a"); // same
        p.setProperty("overridden.prop", "value4"); // same
        p.setProperty("tempdir", "${java.io.tmpdir}/foo"); // same
        p.setProperty("user.properties.file", "../userdir/build2.properties"); // changed
        p.store(os, null);
        os.close();
        l.assertEvents("user.properties.file", "global.prop");
        assertEquals("user.properties.file is correct", "../userdir/build2.properties", pev.getProperty("user.properties.file"));
        assertEquals("global.prop is correct", "value5b", pev.getProperty("global.prop"));
        os = buildProperties2.getOutputStream();
        p = new Properties();
        p.setProperty("global.prop", "value5b"); // same
        // no global.prop.2
        p.store(os, null);
        os.close();
        l.assertEvents("global.prop.2");
        assertEquals("global.prop.2 is gone", null, pev.getProperty("global.prop.2"));
        // XXX try eval when user.properties.file is not defined (tricky, need to preset netbeans.user)
                return null;
            }
        });
    }
    
    /**
     * Test that resolving file names relative to the project basedir works.
     * @throws Exception if anything unexpected happens
     */
    public void testResolveFile() throws Exception {
        // XXX could also be moved to PropertyUtilsTest
        File scratchF = FileUtil.toFile(scratch);
        assertNotNull("scratch directory exists on disk", scratchF);
        File projdirF = FileUtil.toFile(projdir);
        assertNotNull("project directory exists on disk", projdirF);
        assertEquals(". resolves to project basedir", projdirF, h.resolveFile("."));
        assertEquals(". resolves to project basedir", projdir, h.resolveFileObject("."));
        assertEquals("simple relative path resolves", new File(projdirF, "foo"), h.resolveFile("foo"));
        assertEquals("simple relative path resolves (but there is no such file object)", null, h.resolveFileObject("foo"));
        assertEquals("Unix-style ./ resolves", new File(projdirF, "foo"), h.resolveFile("./foo"));
        assertEquals("DOS-style .\\ resolves", new File(projdirF, "foo"), h.resolveFile(".\\foo"));
        assertEquals("Unix-style ./ resolves (but there is no such file object)", null, h.resolveFileObject("./foo"));
        assertEquals("DOS-style ./ resolves (but there is no such file object)", null, h.resolveFileObject(".\\foo"));
        assertEquals(".. resolves up a dir", scratchF, h.resolveFile(".."));
        assertEquals(".. resolves up a dir", scratch, h.resolveFileObject(".."));
        assertEquals("Unix-style ../ resolves up and down", projdirF, h.resolveFile("../proj"));
        assertEquals("DOS-style ..\\ resolves up and down", projdirF, h.resolveFile("..\\proj"));
        assertEquals("Unix-style ../ resolves up and down", projdir, h.resolveFileObject("../proj"));
        assertEquals("DOS-style ..\\ resolves up and down", projdir, h.resolveFileObject("..\\proj"));
        assertEquals("absolute path is left alone", scratchF, h.resolveFile(scratchF.getAbsolutePath()));
        assertEquals("absolute path is left alone", scratch, h.resolveFileObject(scratchF.getAbsolutePath()));
        File somethingElseF = new File(scratchF.getParentFile(), "nonexistent-file-path");
        assertEquals("absolute (nonexistent) path is left alone", somethingElseF, h.resolveFile(somethingElseF.getAbsolutePath()));
        assertEquals("absolute (nonexistent) path has no file object", null, h.resolveFileObject(somethingElseF.getAbsolutePath()));
        assertEquals("URI already normalized (Unix-style)", Utilities.toURI(h.resolveFile("../proj")).normalize().toURL(), Utilities.toURI(h.resolveFile("../proj")).toURL());
        assertEquals("URI already normalized (DOS-style)", Utilities.toURI(h.resolveFile("..\\proj")).normalize().toURL(), Utilities.toURI(h.resolveFile("..\\proj")).toURL());
    }
    
    /**
     * Test that resolving file paths (for example, classpaths) relative to the project basedir works.
     * Note that Ant permits any kind of path separator;
     * see {@link PropertyUtils#tokenizePath} for details of the tokenization.
     * @throws Exception if anything unexpected happens
     */
    public void testResolvePath() throws Exception {
        // XXX could also be moved to PropertyUtilsTest
        File scratchF = FileUtil.toFile(scratch);
        assertNotNull("scratch dir exists on disk", scratchF);
        String scratchS = scratchF.getAbsolutePath();
        File projdirF = FileUtil.toFile(projdir);
        assertNotNull("project dir exists on disk", projdirF);
        String projdirS = projdirF.getAbsolutePath();
        assertEquals("empty path doesn't need to resolve", "", h.resolvePath(""));
        assertEquals(". resolves", projdirS, h.resolvePath("."));
        assertEquals(".. resolves", scratchS, h.resolvePath(".."));
        assertEquals("Unix-style ../ resolves", projdirS, h.resolvePath("../proj"));
        assertEquals("DOS-style ..\\ resolves", projdirS, h.resolvePath("..\\proj"));
        String longpath = projdirS + File.pathSeparator + scratchS + File.pathSeparator + projdirS + File.pathSeparator + projdirS;
        assertEquals("mixed Unix-style path resolves", longpath, h.resolvePath(".:..:../proj:..\\proj"));
        assertEquals("mixed DOS-style path resolves", longpath, h.resolvePath(".;..;../proj;..\\proj"));
        assertEquals("absolute path resolves to itself", scratchS, h.resolvePath(scratchS));
        // XXX check use of Unix symlinks - don't want them canonicalized
        // details of tokenization semantics left to PropertyUtilsTest.testTokenizePath
    }
    
    /**
     * Test that storing changes to .properties files works.
     * @throws Exception if anything unexpected happens
     */
    public void testPutProperties() throws Exception {
        h.addAntProjectListener(l);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("getProperties should never return null", ep);
        assertEquals("three properties defined", 3, ep.size());
        ep.put("testprop", "testval");
        assertTrue("uncommitted changes do not modify project", !pm.isModified(p));
        assertEquals("uncommitted changes not yet in project properties", null, h.getStandardPropertyEvaluator().getProperty("testprop"));
        assertEquals("uncommitted changes fire no events", 0, l.events().length);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        assertTrue("committed changes do modify project", pm.isModified(p));
        AntProjectEvent[] evs = l.events();
        assertEquals("putProperties fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_PROPERTIES_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        assertEquals("committed changes are in project properties", "testval", h.getStandardPropertyEvaluator().getProperty("testprop"));
        Properties props = AntBasedTestUtil.slurpProperties(h, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("project.properties already exists", props);
        assertEquals("project.properties does not yet contain testprop", null, props.getProperty("testprop"));
        pm.saveProject(p);
        assertTrue("project is now saved", !pm.isModified(p));
        assertEquals("saving changes fires no new events", 0, l.events().length);
        assertEquals("committed & saved changes are in project properties", "testval", h.getStandardPropertyEvaluator().getProperty("testprop"));
        props = AntBasedTestUtil.slurpProperties(h, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull("project.properties still exists", props);
        assertEquals("project.properties now contains testprop", "testval", props.getProperty("testprop"));
        // #42147: changes made on disk should fire changes to AntProjectListener, not just to the PropertyEvaluator
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                TestUtil.createFileFromContent(null, h.getProjectDirectory(), AntProjectHelper.PROJECT_PROPERTIES_PATH);
                return null;
            }
        });
        evs = l.events();
        assertEquals("touching project.properties fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_PROPERTIES_PATH, evs[0].getPath());
        assertFalse("unexpected change", evs[0].isExpected());
        assertEquals("empty file now", Collections.EMPTY_MAP, h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        // XXX try creating and deleting property files
        // XXX try modifying several property files and saving in a batch
        // XXX try storing unmodified properties and see what happens
        // XXX try storing a fresh EditableProperties object not returned from getProperties
    }
    
    /**
     * Test that writing changes to project.xml and private.xml works.
     * @throws Exception if anything unexpected happens
     */
    public void testPutPrimaryConfigurationData() throws Exception {
        h.addAntProjectListener(l);
        Element data = h.getPrimaryConfigurationData(true);
        assertNotNull("<shared-stuff/> is there to start", XMLUtil.findElement(data, "shared-stuff", "urn:test:shared"));
        assertTrue("project is not initially modified", !pm.isModified(p));
        assertEquals("gPCD fires no events", 0, l.events().length);
        assertNotNull("config data has an owner document", data.getOwnerDocument());
        Element nue = data.getOwnerDocument().createElementNS("urn:test:shared", "misc");
        data.appendChild(nue);
        assertTrue("project is not modified after uncommitted change", !pm.isModified(p));
        assertEquals("no events fired after uncommitted change", 0, l.events().length);
        assertEquals("after uncommitted change gPCD does not yet have new <misc/>", null, XMLUtil.findElement(h.getPrimaryConfigurationData(true), "misc", "urn:test:shared"));
        h.putPrimaryConfigurationData(data, true);
        assertTrue("project is modified after committed change", pm.isModified(p));
        AntProjectEvent[] evs = l.events();
        assertEquals("putPCD fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        nue = XMLUtil.findElement(h.getPrimaryConfigurationData(true), "misc", "urn:test:shared");
        assertNotNull("after committed change gPCD has new <misc/>", nue);
        assertEquals("new element name is correct", "misc", nue.getLocalName());
        assertEquals("new element namespace is correct", "urn:test:shared", nue.getNamespaceURI());
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element configuration = XMLUtil.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("still has <configuration> on disk", configuration);
        data = XMLUtil.findElement(configuration, "data", "urn:test:shared");
        assertNotNull("still has <data> on disk", data);
        nue = XMLUtil.findElement(data, "misc", "urn:test:shared");
        assertEquals("<misc/> not yet on disk", null, nue);
        pm.saveProject(p);
        assertTrue("project is not modified after save", !pm.isModified(p));
        assertEquals("saving changes fires no new events", 0, l.events().length);
        nue = XMLUtil.findElement(h.getPrimaryConfigurationData(true), "misc", "urn:test:shared");
        assertNotNull("after save gPCD still has new <misc/>", nue);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        configuration = XMLUtil.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("still has <configuration> on disk", configuration);
        data = XMLUtil.findElement(configuration, "data", "urn:test:shared");
        assertNotNull("still has <data> on disk", data);
        nue = XMLUtil.findElement(data, "misc", "urn:test:shared");
        assertNotNull("<misc/> now on disk", nue);
        // #42147: changes made on disk should result in firing of an AntProjectEvent
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                TestUtil.createFileFromContent(AntProjectHelperTest.class.getResource("data/project-modified.xml"), projdir, AntProjectHelper.PROJECT_XML_PATH);
                return null;
            }
        });
        evs = l.events();
        assertEquals("writing project.xml on disk fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        assertFalse("unexpected change", evs[0].isExpected());
        assertEquals("correct new display name", "Some New Name", ProjectUtils.getInformation(p).getDisplayName());
        data = h.getPrimaryConfigurationData(true);
        Element stuff = XMLUtil.findElement(data, "other-shared-stuff", "urn:test:shared");
        assertNotNull("have <other-shared-stuff/> now", stuff);
        AuxiliaryConfiguration aux = p.getLookup().lookup(AuxiliaryConfiguration.class);
        data = aux.getConfigurationFragment("data", "urn:test:shared-aux", true);
        assertNotNull("have aux <data>", data);
        stuff = XMLUtil.findElement(data, "other-aux-shared-stuff", "urn:test:shared-aux");
        assertNotNull("have <other-aux-shared-stuff/> now", stuff);
        // XXX try private.xml too
        // XXX try modifying both XML files, or different parts of the same, and saving in a batch
        // XXX try storing unmodified XML fragments and see what happens
        // XXX try storing a fresh Element not returned from getPrimaryConfigurationData
    }
    
    /**
     * Test that it is possible for external code to store custom data in project.xml and private.xml.
     * @throws Exception if anything unexpected happens
     */
    public void testExtensibleMetadataProviderImpl() throws Exception {
        AuxiliaryConfiguration aux = p.getLookup().lookup(AuxiliaryConfiguration.class);
        assertNotNull("AuxiliaryConfiguration present", aux);
        CacheDirectoryProvider cdp = p.getLookup().lookup(CacheDirectoryProvider.class);
        assertNotNull("CacheDirectoryProvider present", cdp);
        // Check cache dir.
        FileObject cache = cdp.getCacheDirectory();
        assertNotNull("has a cache dir", cache);
        assertTrue("cache dir is a folder", cache.isFolder());
        cache.createData("foo");
        cache = cdp.getCacheDirectory();
        assertNotNull("cache contents still there", cache.getFileObject("foo"));
        // Check read of shared data.
        h.addAntProjectListener(l);
        Element data = aux.getConfigurationFragment("data", "urn:test:shared-aux", true);
        assertNotNull("found shared <data>", data);
        assertEquals("correct name", "data", data.getLocalName());
        assertEquals("correct namespace", "urn:test:shared-aux", data.getNamespaceURI());
        Element stuff = XMLUtil.findElement(data, "aux-shared-stuff", "urn:test:shared-aux");
        assertNotNull("found <aux-shared-stuff/>", stuff);
        assertEquals("gCF fires no changes", 0, l.events().length);
        // Check write of shared data.
        stuff.setAttribute("attr", "val");
        assertFalse("project not modified by local change", pm.isModified(p));
        aux.putConfigurationFragment(data, true);
        assertTrue("now project is modified", pm.isModified(p));
        AntProjectEvent[] evs = l.events();
        assertEquals("pCF fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        pm.saveProject(p);
        assertEquals("saving project fires no new changes", 0, l.events().length);
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("<configuration> still exists", config);
        data = XMLUtil.findElement(config, "data", "urn:test:shared-aux");
        assertNotNull("<data> still exists", data);
        stuff = XMLUtil.findElement(data, "aux-shared-stuff", "urn:test:shared-aux");
        assertNotNull("still have <aux-shared-stuff/>", stuff);
        assertEquals("attr written correctly", "val", stuff.getAttribute("attr"));
        // Check read of private data.
        data = aux.getConfigurationFragment("data", "urn:test:private-aux", false);
        assertNotNull("found shared <data>", data);
        assertEquals("correct name", "data", data.getLocalName());
        assertEquals("correct namespace", "urn:test:private-aux", data.getNamespaceURI());
        stuff = XMLUtil.findElement(data, "aux-private-stuff", "urn:test:private-aux");
        assertNotNull("found <aux-private-stuff/>", stuff);
        assertEquals("gCF fires no changes", 0, l.events().length);
        // Check write of private data.
        stuff.setAttribute("attr", "val");
        assertFalse("project not modified by local change", pm.isModified(p));
        aux.putConfigurationFragment(data, false);
        assertTrue("now project is modified", pm.isModified(p));
        evs = l.events();
        assertEquals("pCF fires one event", 1, evs.length);
        assertEquals("correct helper", h, evs[0].getHelper());
        assertEquals("correct path", AntProjectHelper.PRIVATE_XML_PATH, evs[0].getPath());
        assertTrue("expected change", evs[0].isExpected());
        pm.saveProject(p);
        assertEquals("saving project fires no new changes", 0, l.events().length);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PRIVATE_XML_PATH);
        config = doc.getDocumentElement();
        data = XMLUtil.findElement(config, "data", "urn:test:private-aux");
        assertNotNull("<data> still exists", data);
        stuff = XMLUtil.findElement(data, "aux-private-stuff", "urn:test:private-aux");
        assertNotNull("still have <aux-private-stuff/>", stuff);
        assertEquals("attr written correctly", "val", stuff.getAttribute("attr"));
        // Check that missing fragments are not returned.
        Element bogus = aux.getConfigurationFragment("doesn't exist", "bogus", true);
        assertNull("no such fragment - wrong name/ns", bogus);
        bogus = aux.getConfigurationFragment("data", "bogus", true);
        assertNull("no such fragment - wrong ns", bogus);
        bogus = aux.getConfigurationFragment("doesn't exist", "urn:test:shared-aux", true);
        assertNull("no such fragment - wrong name", bogus);
        bogus = aux.getConfigurationFragment("data", "urn:test:shared-aux", false);
        assertNull("no such fragment - wrong file", bogus);
        // Try adding a new fragment.
        Document temp = XMLUtil.createDocument("whatever", null, null, null);
        data = temp.createElementNS("urn:test:whatever", "hello");
        data.appendChild(temp.createTextNode("stuff"));
        assertFalse("project currently unmodified", pm.isModified(p));
        aux.putConfigurationFragment(data, true);
        assertTrue("adding frag modified project", pm.isModified(p));
        evs = l.events();
        assertEquals("pCF fires one event", 1, evs.length);
        assertEquals("correct path", AntProjectHelper.PROJECT_XML_PATH, evs[0].getPath());
        pm.saveProject(p);
        assertEquals("saving project fires no new changes", 0, l.events().length);
        data = aux.getConfigurationFragment("hello", "urn:test:whatever", true);
        assertNotNull("can retrieve new frag", data);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("<configuration> still exists", config);
        data = XMLUtil.findElement(config, "hello", "urn:test:whatever");
        assertNotNull("<hello> still exists", data);
        assertEquals("correct nested contents too", "stuff", XMLUtil.findText(data));
        // Try removing a fragment.
        assertFalse("project is unmodified", pm.isModified(p));
        assertTrue("can remove new frag", aux.removeConfigurationFragment("hello", "urn:test:whatever", true));
        assertTrue("project is now modified", pm.isModified(p));
        assertNull("now frag is gone", aux.getConfigurationFragment("hello", "urn:test:whatever", true));
        pm.saveProject(p);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("<configuration> still exists", config);
        data = XMLUtil.findElement(config, "hello", "urn:test:whatever");
        assertNull("now <hello> is gone", data);
        assertFalse("cannot remove a frag that is not there", aux.removeConfigurationFragment("hello", "urn:test:whatever", true));
        assertFalse("trying to remove a nonexistent frag does not modify project", pm.isModified(p));
        
        // check that created elements are ordered
        data = temp.createElementNS("namespace", "ccc");
        aux.putConfigurationFragment(data, true);
        data = temp.createElementNS("namespace", "bbb");
        aux.putConfigurationFragment(data, true);
        data = temp.createElementNS("namespace", "aaa");
        aux.putConfigurationFragment(data, true);
        data = temp.createElementNS("namespace-1", "bbb");
        aux.putConfigurationFragment(data, true);
        data = temp.createElementNS("name-sp", "bbb");
        aux.putConfigurationFragment(data, true);
        data = temp.createElementNS("namespace", "aaaa");
        aux.putConfigurationFragment(data, true);
        pm.saveProject(p);
        doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        String[] names = new String[]{"aaa-namespace", "aaaa-namespace", "bbb-name-sp", "bbb-namespace", "bbb-namespace-1", "ccc-namespace", "data-urn:test:shared", "data-urn:test:shared-aux"};
        int count = 0;
        NodeList list = config.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            assertEquals(names[count], n.getNodeName()+"-"+n.getNamespaceURI());
            count++;
        }
        assertEquals("Elements count does not match", names.length, count);
        
        // XXX check that it cannot be used to load or store primary configuration data
        // or other general fixed metadata
        // XXX try overwriting data
    }

    @RandomlyFails // in last assertion in NB-Core-Build #2426 & #2431; maybe due to #2bbacd6640a5?
    public void test68872() throws Exception {
        AuxiliaryConfiguration aux = p.getLookup().lookup(AuxiliaryConfiguration.class);
        assertNotNull("AuxiliaryConfiguration present", aux);

        Element data = aux.getConfigurationFragment("data", "urn:test:private-aux", false);
        Element stuff = XMLUtil.findElement(data, "aux-private-stuff", "urn:test:private-aux");
        assertNotNull("found <aux-private-stuff/>", stuff);
        // Check write of private data.
        stuff.setAttribute("attr", "val");
        aux.putConfigurationFragment(data, false);
        assertTrue("now project is modified", pm.isModified(p));
        
        FileObject privateXMLFO = p.getProjectDirectory().getFileObject("nbproject/private/private.xml");
        
        assertNotNull(privateXMLFO);
        
        File privateXML = FileUtil.toFile(privateXMLFO);
        
        privateXML.delete();
        
        privateXMLFO.refresh();
        privateXMLFO.getParent().refresh();
        
        pm.saveProject(p);
        
        //the file should be renewed with new data:
        assertTrue(privateXML.exists());
        
        //check the data are written:
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PRIVATE_XML_PATH);
        Element config = doc.getDocumentElement();
        data = XMLUtil.findElement(config, "data", "urn:test:private-aux");
        assertNotNull("<data> still exists", data);
        stuff = XMLUtil.findElement(data, "aux-private-stuff", "urn:test:private-aux");
        assertNotNull("still have <aux-private-stuff/>", stuff);
        assertEquals("attr written correctly", "val", stuff.getAttribute("attr"));
        
        //check that on-disk changes are not ignored if the project is saved:
        privateXML.delete();
        
        privateXMLFO.refresh();
        privateXMLFO.getParent().refresh();
        
        assertNull(aux.getConfigurationFragment("data", "urn:test:private-aux", false));
    }
    
    public void testCreatePropertyProvider() throws Exception {
        PropertyProvider pp = h.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Map<String,String> defs = pp.getProperties();
        assertEquals("correct number of defs", 3, defs.size());
        assertEquals("correct value", "value1", defs.get("shared.prop"));
        // Test changes.
        MockChangeListener mcl = new MockChangeListener();
        pp.addChangeListener(mcl);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("foo", "bar");
        mcl.msg("no events from uncommitted changes").assertNoEvents();
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        mcl.msg("got a change from setting a property").assertEvent();
        defs = pp.getProperties();
        assertEquals("correct new size", 4, defs.size());
        assertEquals("correct new value", "bar", defs.get("foo"));
        // No-op changes.
        ep = ep.cloneProperties();
        ep.setProperty("foo", "bar2");
        ep.setProperty("foo", "bar");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        mcl.msg("no events from no-op changes").assertNoEvents();
        // Deleting a property file.
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, null);
        mcl.msg("got a change from removing a property file").assertEvent();
        assertEquals("now have no definitions", Collections.EMPTY_MAP, pp.getProperties());
        // Start off with no file, then create it.
        String path = "foo.properties";
        pp = h.getPropertyProvider(path);
        pp.addChangeListener(mcl);
        assertEquals("no defs initially", Collections.EMPTY_MAP, pp.getProperties());
        assertNull("no file made yet", h.getProjectDirectory().getFileObject(path));
        ep = new EditableProperties(false);
        ep.setProperty("one", "1");
        ep.setProperty("two", "2");
        h.putProperties(path, ep);
        mcl.msg("making the file fired a change").assertEvent();
        defs = pp.getProperties();
        assertEquals("two defs", 2, defs.size());
        assertEquals("right value #1", "1", defs.get("one"));
        assertEquals("right value #2", "2", defs.get("two"));
        assertNull("no file yet saved to disk", h.getProjectDirectory().getFileObject(path));
        ep.setProperty("three", "3");
        mcl.msg("no events from uncomm. change").assertNoEvents();
        h.putProperties(path, ep);
        mcl.msg("now have changed new file").assertEvent();
        defs = pp.getProperties();
        assertEquals("three defs", 3, defs.size());
        // XXX test that saving the project fires no additional changes
        // XXX test changes fired if file modified (or created or removed) on disk
    }

    /* XXX unable to simulate #50198 in a test environment:
    public void testMultithreadedAccessToXmlData() throws Exception {
        class R implements Runnable {
            private final int x;
            public R(int x) {
                this.x = x;
            }
            public void run() {
                System.out.println("starting #" + x);
                for (int i = 0; i < 1000; i++) {
                    Element data = h.getPrimaryConfigurationData(true);
                    XMLUtil.findSubElements(data);
                    if (i % 100 == 0) {
                        System.out.println("in the middle of #" + x);
                        try {
                            Thread.sleep((long) (Math.random() * 25));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println("finishing #" + x);
            }
        }
        Thread t1 = new Thread(new R(1));
        Thread t2 = new Thread(new R(2));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
     */

    public void testStockPropertyPreprovider() throws Exception {
        Map<String,String> props = h.getStockPropertyPreprovider().getProperties();
        assertEquals(FileUtil.toFile(projdir).getAbsolutePath(), props.get("basedir"));
        assertEquals(new File(getWorkDir(), "ant").getAbsolutePath(), props.get("ant.home"));
        assertEquals(ProjectProperties.antJar.getAbsolutePath(), props.get("ant.core.lib"));
    }

    public void testSchemaValidation() throws Exception {
        // Do NOT run inside PM.mutex, for this magically wraps everything in an atomic action,
        // which means that all FS events are delivered *after* the test fails.
        // Really hard just to make a unit test do the obvious thing the first time around when using mutexes...
        ProjectLibraryProvider.FIRE_CHANGES_SYNCH = true;
        // Checking read against schema, and that we stick with the last known good load:
        assertEquals("initial name correct", "somename", currentName());
        String content = projdir.getFileObject("nbproject/project.xml").asText("UTF-8");
        String bogus = "<references xmlns='http://www.netbeans.org/ns/ant-project-references/2'><bogus/></references>\n";
        AntProjectHelper.QUIETLY_SWALLOW_XML_LOAD_ERRORS = true;
        try {
            TestFileUtils.writeFile(projdir, "nbproject/project.xml", content.replace("</project>", bogus + "</project>").replace("<name>somename</name>", "<name>newname</name>"));
            assertEquals("invalid project.xml was not loaded", "somename", currentName());
        } finally {
            AntProjectHelper.QUIETLY_SWALLOW_XML_LOAD_ERRORS = false;
        }
        TestFileUtils.writeFile(projdir, "nbproject/project.xml", content.replace("</project>", "</project>").replace("<name>somename</name>", "<name>newname</name>"));
        assertEquals("valid project.xml was loaded", "newname", currentName());
        // Checking write against schema:
        Element data = h.getPrimaryConfigurationData(true);
        data.getElementsByTagName("*").item(0).getChildNodes().item(0).setNodeValue("newername");
        h.putPrimaryConfigurationData(data, true);
        Element bogusEl = XMLUtil.createDocument("references", "http://www.netbeans.org/ns/ant-project-references/2", null, null).getDocumentElement();
        bogusEl.appendChild(bogusEl.getOwnerDocument().createElementNS("http://www.netbeans.org/ns/ant-project-references/2", "boguser"));
        h.putConfigurationFragment(bogusEl, true);
        try {
            ProjectManager.getDefault().saveProject(p);
            fail("Should not have been able to save invalid XML");
        } catch (IOException x) {
            assertTrue(x.toString(), x.getMessage().contains("boguser"));
        }
        h.removeConfigurationFragment("references", "http://www.netbeans.org/ns/ant-project-references/2", true);
        ProjectManager.getDefault().saveProject(p);
        content = projdir.getFileObject("nbproject/project.xml").asText("UTF-8");
        assertTrue(content, content.contains("newername"));
        assertFalse(content, content.contains("bogus"));
    }
    private String currentName() {
        return h.getPrimaryConfigurationData(true).getElementsByTagName("*").item(0).getChildNodes().item(0).getNodeValue();
    }

    public void testKnownValidProjectXmlCRC32s() throws Exception { // #195029
        CharSequence log = Log.enable(AntBasedProjectFactorySingleton.class.getName(), Level.FINE);
        h.createAuxiliaryConfiguration().putConfigurationFragment(XMLUtil.createDocument("stuff", "urn:test", null, null).getDocumentElement(), true);
        ProjectManager.getDefault().saveProject(p);
        assertNotNull(new AntBasedProjectFactorySingleton().loadProject(projdir, new ProjectState() {
            @Override public void markModified() {}
            @Override public void notifyDeleted() throws IllegalStateException {}
        }));
        String logS = log.toString();
        assertFalse(logS, logS.contains("Validating: "));
    }

}
