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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.ProjectLibraryProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/* XXX tests needed
 * - testAddArtifactCollocation
 * check that collocated foreign projects update project.properties
 * while noncollocated foreign projects update private.properties
 * and that absolute artifact locns -> private.properties
 * also addForeignFileReference(File,String) on plain files needs to
 * check collocation
 * - testUniquifyProjectNames
 * check that foreign project names are uniquified correctly
 * both in addReference and addForeignFileReference etc.
 * - testVcsFriendliness
 * check that refs are added in a predictable order
 */

/**
 * Test functionality of ReferenceHelper.
 * @author Jesse Glick
 */
public class ReferenceHelperTest extends NbTestCase {

    /**
     * Create the test suite.
     * @param name suite name
     */
    public ReferenceHelperTest(String name) {
        super(name);
    }
    
    /** Scratch directory. */
    private FileObject scratch;
    /** Directory of master project (proj). */
    private FileObject projdir;
    /**
     * Directory of a collocated sister project (proj2).
     * Has artifacts build.jar=dist/proj2.jar and
     * build.javadoc=build/javadoc as well as
     * build.javadoc.complete=build/complete-javadoc.
     */
    private FileObject sisterprojdir;
    /**
     * The same structure as sisterprojdir but in different folder.
     * Useful for testing that referenceIDs are uniquified.
     */
    private FileObject sisterprojdir2;
    /**
     * Directory of a noncollocated project (proj3).
     * Has artifact build.jar=d i s t/p r o j 3.jar.
     */
    private FileObject sepprojdir;
    /** The project manager singleton. */
    private ProjectManager pm;
    /** The master project. */
    private Project p;
    /** The master project's helper. */
    private AntProjectHelper h;
    /** The collocated sister project's helper. */
    private AntProjectHelper sisterh;
    /** The collocated sister2 project's helper. */
    private AntProjectHelper sisterh2;
    /** The noncollocated project's helper. */
    private AntProjectHelper seph;
    /** The master project's reference helper. */
    private ReferenceHelper r;
    //private AntBasedTestUtil.TestListener l;
    private PropertyEvaluator pev;
    
    private static void setCodeNameOfTestProject(AntProjectHelper helper, String name) {
        Element data = helper.getPrimaryConfigurationData(true);
        Element nameEl = data.getOwnerDocument().createElementNS("urn:test:shared", "name");
        nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
        data.appendChild(nameEl);
        helper.putPrimaryConfigurationData(data, true);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        ClassLoader l = ReferenceHelper.class.getClassLoader();
        MockLookup.setLookup(
            Lookups.fixed(AntBasedTestUtil.testAntBasedProjectType(), AntBasedTestUtil.testCollocationQueryImplementation(Utilities.toURI(getWorkDir()))),
            Lookups.singleton(l),
            Lookups.exclude(Lookups.metaInfServices(l), CollocationQueryImplementation.class));
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        TestUtil.createFileFromContent(ReferenceHelperTest.class.getResource("data/project.xml"), projdir, "nbproject/project.xml");
        pm = ProjectManager.getDefault();
        p = pm.findProject(projdir);
        assertNotNull("found project in " + projdir, p);
        h = p.getLookup().lookup(AntProjectHelper.class);
        assertNotNull("found helper for " + p, h);
        r = p.getLookup().lookup(ReferenceHelper.class);
        assertNotNull("found ref helper for " + p, r);
        sisterprojdir = FileUtil.createFolder(scratch, "proj2");
        assertTrue("projdir and sisterprojdir collocated",
            CollocationQuery.areCollocated(projdir.toURI(), sisterprojdir.toURI()));
        sisterh = ProjectGenerator.createProject(sisterprojdir, "test");
        setCodeNameOfTestProject(sisterh, "proj2");
        EditableProperties props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "dist/proj2.jar");
        props.setProperty("build.javadoc", "build/javadoc");
        props.setProperty("build.javadoc.complete", "build/complete-javadoc");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        // Probably unnecessary: pm.saveProject(pm.findProject(sisterprojdir));
        
        sisterprojdir2 = FileUtil.createFolder(scratch, "proj2-copy");
        assertTrue("projdir and sisterprojdir2 collocated",
            CollocationQuery.areCollocated(projdir.toURI(), sisterprojdir2.toURI()));
        sisterh2 = ProjectGenerator.createProject(sisterprojdir2, "test");
        setCodeNameOfTestProject(sisterh2, "proj2");
        props = sisterh2.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "dist/proj2.jar");
        props.setProperty("build.javadoc", "build/javadoc");
        props.setProperty("build.javadoc.complete", "build/complete-javadoc");
        sisterh2.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        
        sepprojdir = FileUtil.createFolder(scratch, "separate/proj3");
        // If following assert fails then there is a global impl of collocation
        // query which says that these two files are collocated. Hidden it
        // similarly as it is done for SampleCVSCollocationQueryImpl
        // in META-INF/services.
        assertFalse("" + projdir + " and " + sepprojdir + " cannot be collocated",
            CollocationQuery.areCollocated(projdir.toURI(), sepprojdir.toURI()));
        seph = ProjectGenerator.createProject(sepprojdir, "test");
        setCodeNameOfTestProject(seph, "proj3");
        props = seph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "d i s t/p r o j 3.jar");
        seph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        //l = new AntBasedTestUtil.TestListener();
        
        pev = h.getStandardPropertyEvaluator();
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sisterprojdir = null;
        sisterh = null;
        //sepprojdir = null;
        pm = null;
        p = null;
        h = null;
        //l = null;
        super.tearDown();
    }

    /**
     * Check that the raw add, get, and remove calls work.
     * @throws Exception in case of unexpected failures
     */
    @SuppressWarnings("deprecation")
    public void testRawReferenceManipulation() throws Exception {
        assertEquals("starting with no raw references", Collections.EMPTY_LIST, Arrays.asList(r.getRawReferences()));
        // Test simple adding of a reference.
        ReferenceHelper.RawReference ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("successfully added a raw ref to otherproj.dojar", r.addRawReference(ref));
        assertNull("project.properties not changed", pev.getProperty("project.otherproj"));
        assertTrue("project is modified", pm.isModified(p));
        ref = r.getRawReference("otherproj", "dojarID");
        assertNotNull("found otherproj.dojar", ref);
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", URI.create("build.xml"), ref.getScriptLocation());
        assertEquals("correct script location", "${project.otherproj}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        // Nonexistent references are not returned.
        ref = r.getRawReference("otherproj2", "dojarID");
        assertNull("no such ref otherproj2.dojar", ref);
        ref = r.getRawReference("otherproj", "dojar2");
        assertNull("no such ref otherproj.dojar2", ref);
        ref = r.getRawReference("otherproj2", "dojar2");
        assertNull("no such ref otherproj2.dojar2", ref);
        // The reference is found now.
        ReferenceHelper.RawReference[] refs = r.getRawReferences();
        assertEquals("one reference here", 1, refs.length);
        ref = refs[0];
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", "${project.otherproj}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        // Test removing it.
        assertTrue("successfully removed otherproj.dojar", r.removeRawReference("otherproj", "dojarID"));
        refs = r.getRawReferences();
        assertEquals("no references here", 0, refs.length);
        ref = r.getRawReference("otherproj", "dojar");
        assertNull("otherproj.dojar is gone", ref);
        // Test adding several references.
        ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("added ref to otherproj.dojar", r.addRawReference(ref));
        ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar2", "clean", "dojar2ID");
        assertTrue("added ref to otherproj.dojar2", r.addRawReference(ref));
        ref = new ReferenceHelper.RawReference("otherproj2", "ear", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("added ref to otherproj2.dojar", r.addRawReference(ref));
        assertEquals("have three refs", 3, r.getRawReferences().length);
        // Test no-op adds and removes.
        pm.saveProject(p);
        assertFalse("project is saved", pm.isModified(p));
        ref = new ReferenceHelper.RawReference("otherproj", "jar", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertFalse("already had ref to otherproj.dojar", r.addRawReference(ref));
        assertFalse("project is not modified by no-op add", pm.isModified(p));
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        assertFalse("did not have ref to foo.bar", r.removeRawReference("foo", "bar"));
        assertFalse("project is not modified by no-op remove", pm.isModified(p));
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        // Test modifications.
        ref = new ReferenceHelper.RawReference("otherproj", "war", URI.create("build.xml"), "dojar", "clean", "dojarID");
        assertTrue("modified ref to otherproj.dojar", r.addRawReference(ref));
        assertTrue("project is modified by changed ref", pm.isModified(p));
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        ref = r.getRawReference("otherproj", "dojarID");
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "war", ref.getArtifactType());
        assertEquals("correct script location", "${project.otherproj}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        ref = new ReferenceHelper.RawReference("otherproj", "war", URI.create("build2.xml"), "dojar", "clean", "dojarID");
        assertTrue("modified ref to otherproj.dojar", r.addRawReference(ref));
        ref = new ReferenceHelper.RawReference("otherproj", "war", URI.create("build2.xml"), "dojar", "clean2", "dojarID");
        assertTrue("modified ref to otherproj.dojar", r.addRawReference(ref));
        ref = r.getRawReference("otherproj", "dojarID");
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "war", ref.getArtifactType());
        assertEquals("correct script location", "${project.otherproj}/build2.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean2", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        assertEquals("still have three refs", 3, r.getRawReferences().length);
        // More removals and adds.
        assertTrue("now removing otherproj.dojar2", r.removeRawReference("otherproj", "dojar2ID"));
        assertNull("otherproj.dojar2 is gone", r.getRawReference("otherproj", "dojar2ID"));
        assertNotNull("otherproj.jar is still there", r.getRawReference("otherproj", "dojarID"));
        assertNotNull("otherproj2.dojar is still there", r.getRawReference("otherproj2", "dojarID"));
        assertEquals("down to two refs", 2, r.getRawReferences().length);
        ref = new ReferenceHelper.RawReference("aardvark", "jar", URI.create("build.xml"), "jar", "clean", "jarID");
        assertTrue("added ref to aardvark.jar", r.addRawReference(ref));
        // Check list of refs.
        refs = r.getRawReferences();
        assertEquals("back to three refs", 3, refs.length);
        // NOTE on undocumented constraint: getRawReferences should sort results by proj then target
        ref = refs[0];
        assertEquals("correct foreign project name", "aardvark", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", "${project.aardvark}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "jar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "jarID", ref.getID());
        ref = refs[1];
        assertEquals("correct foreign project name", "otherproj", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "war", ref.getArtifactType());
        assertEquals("correct script location", "${project.otherproj}/build2.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean2", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        ref = refs[2];
        assertEquals("correct foreign project name", "otherproj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "ear", ref.getArtifactType());
        assertEquals("correct script location", "${project.otherproj2}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct ID name", "dojarID", ref.getID());
        // Try saving and checking that project.xml is correct.
        assertTrue("Project is still modified", pm.isModified(p));
        pm.saveProject(p);
        Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
        Element config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", AntProjectHelper.PROJECT_NS);
        assertNotNull("have <configuration>", config);
        Element references = XMLUtil.findElement(config, ReferenceHelper.REFS_NAME, ReferenceHelper.REFS_NS);
        assertNotNull("have <references>", references);
        NodeList nl = references.getElementsByTagNameNS(ReferenceHelper.REFS_NS, "reference");
        assertEquals("have three <reference>s", 3, nl.getLength());
        String[] elementNames = {
            "foreign-project",
            "artifact-type",
            "script",
            "target",
            "clean-target",
            "id",
        };
        String[][] values = {
            {
                "aardvark",
                "jar",
                "build.xml",
                "jar",
                "clean",
                "jarID",
            },
            {
                "otherproj",
                "war",
                "build2.xml",
                "dojar",
                "clean2",
                "dojarID",
            },
            {
                "otherproj2",
                "ear",
                "build.xml",
                "dojar",
                "clean",
                "dojarID",
            },
        };
        for (int i = 0; i < 3; i++) {
            Element reference = (Element)nl.item(i);
            for (int j = 0; j < 6; j++) {
                String elementName = elementNames[j];
                Element element = XMLUtil.findElement(reference, elementName, ReferenceHelper.REFS_NS);
                assertNotNull("had element " + elementName + " in ref #" + i, element);
                assertEquals("correct text in " + elementName + " in ref #" + i, values[i][j], XMLUtil.findText(element));
            }
        }
    }
    
    /**
     * Check that the adding and removing artifact objects updates everything it should.
     * @throws Exception in case of unexpected failures
     */
    @SuppressWarnings("deprecation")
    public void testAddRemoveArtifact() throws Exception {
        // Add one artifact. Check that the raw reference is there.
        assertFalse("project not initially modified", pm.isModified(p));
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertFalse("reference exist", r.isReferenced(art, art.getArtifactLocations()[0]));
        assertTrue("added a ref to proj2.dojar", r.addReference(art));
        assertTrue("reference exist", r.isReferenced(art, art.getArtifactLocations()[0]));
        assertTrue("project now modified", pm.isModified(p));
        ReferenceHelper.RawReference[] refs = r.getRawReferences();
        assertEquals("one ref now", 1, refs.length);
        ReferenceHelper.RawReference ref = refs[0];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", "${project.proj2}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        // Check that the project properties are correct.
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("correct evaluated ${project.proj2}",
            FileUtil.toFile(sisterprojdir),
            h.resolveFile(pev.getProperty("project.proj2")));
        assertEquals("correct ${reference.proj2.dojar}", "${project.proj2}/dist/proj2.jar",
            props.getProperty("reference.proj2.dojar"));
        assertEquals("correct evaluated ${reference.proj2.dojar}",
            new File(new File(FileUtil.toFile(sisterprojdir), "dist"), "proj2.jar"),
            h.resolveFile(pev.getProperty("reference.proj2.dojar")));
        // Check no-op adds.
        pm.saveProject(p);
        assertTrue("reference exist", r.isReferenced(art, art.getArtifactLocations()[0]));
        assertFalse("no-op add", r.addReference(art));
        assertFalse("project not modified by no-op add", pm.isModified(p));
        // Try another artifact from the same project.
        art = sisterh.createSimpleAntArtifact("javadoc", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "clean");
        assertFalse("reference does not exist", r.isReferenced(art, art.getArtifactLocations()[0]));
        assertNotNull("added a ref to proj2.dojavadoc", r.addReference(art, art.getArtifactLocations()[0]));
        assertTrue("project now modified", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("two refs now", 2, refs.length);
        ref = refs[0];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        ref = refs[1];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct artifact type", "javadoc", ref.getArtifactType());
        assertEquals("correct script location", "${project.proj2}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("still correct ${reference.proj2.dojar}", "${project.proj2}/dist/proj2.jar",
            props.getProperty("reference.proj2.dojar"));
        assertEquals("correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        assertEquals("correct evaluated ${reference.proj2.dojavadoc}",
            new File(new File(FileUtil.toFile(sisterprojdir), "build"), "javadoc"),
            h.resolveFile(pev.getProperty("reference.proj2.dojavadoc")));
        pm.saveProject(p);
        assertTrue("reference exist", r.isReferenced(art, art.getArtifactLocations()[0]));
        r.addReference(art, art.getArtifactLocations()[0]);
        assertFalse("project not modified by no-op add", pm.isModified(p));
        // Try modifying the second artifact in some way.
        // Note that only changes in the type, clean target, and artifact path count as modifications.
        art = sisterh.createSimpleAntArtifact("javadoc.html", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "clean");
        assertFalse("reference exist but needs to be updated", r.isReferenced(art, art.getArtifactLocations()[0]));
        r.addReference(art, art.getArtifactLocations()[0]);
        assertTrue("project modified by ref mod", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("still two refs", 2, refs.length);
        ref = refs[1];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "javadoc.html", ref.getArtifactType());
        assertEquals("correct script location", "${project.proj2}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        art = sisterh.createSimpleAntArtifact("javadoc.html", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "realclean");
        r.addReference(art, art.getArtifactLocations()[0]);
        pm.saveProject(p);
        art = sisterh.createSimpleAntArtifact("javadoc.html", "build.javadoc.complete", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "realclean");
        r.addReference(art, art.getArtifactLocations()[0]);
        assertTrue("project modified by ref mod", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("still two refs", 2, refs.length);
        ref = refs[1];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "javadoc.html", ref.getArtifactType());
        assertEquals("correct script location", "${project.proj2}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct modified clean target name", "realclean", ref.getCleanTargetName());
        // Check that changing the artifact location property changed the reference property too.
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("still correct ${reference.proj2.dojar}", "${project.proj2}/dist/proj2.jar",
            props.getProperty("reference.proj2.dojar"));
        assertEquals("correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/complete-javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        assertEquals("correct evaluated ${reference.proj2.dojavadoc}",
            new File(new File(FileUtil.toFile(sisterprojdir), "build"), "complete-javadoc"),
            h.resolveFile(pev.getProperty("reference.proj2.dojavadoc")));
        // Check that changing the value of the artifact location property
        // in the subproject modifies this project.
        pm.saveProject(p);
        assertTrue("reference exist but needs to be updated", r.isReferenced(art, art.getArtifactLocations()[0]));
        r.addReference(art, art.getArtifactLocations()[0]);
        assertFalse("project not modified by no-op add", pm.isModified(p));
        props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.javadoc.complete", "build/total-javadoc");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        r.addReference(art, art.getArtifactLocations()[0]);
        assertTrue("project modified by new ${reference.proj2.dojavadoc}", pm.isModified(p));
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/total-javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        assertEquals("correct evaluated ${reference.proj2.dojavadoc}",
            new File(new File(FileUtil.toFile(sisterprojdir), "build"), "total-javadoc"),
            h.resolveFile(pev.getProperty("reference.proj2.dojavadoc")));
        // Now try removing first ref. Should remove raw ref, ref property, but not project property.
        pm.saveProject(p);
        assertTrue("remove proj2.dojar succeeded", r.destroyReference("${reference.proj2.dojar}"));
        assertTrue("remove ref modified project", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("now have just one ref", 1, refs.length);
        ref = refs[0];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct modified artifact type", "javadoc.html", ref.getArtifactType());
        assertEquals("correct script location", "${project.proj2}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojavadoc", ref.getTargetName());
        assertEquals("correct modified clean target name", "realclean", ref.getCleanTargetName());
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("correct ${project.proj2}", "../proj2",
            props.getProperty("project.proj2"));
        assertEquals("no more ${reference.proj2.dojar}", null,
            props.getProperty("reference.proj2.dojar"));
        assertEquals("still correct ${reference.proj2.dojavadoc}",
            "${project.proj2}/build/total-javadoc",
            props.getProperty("reference.proj2.dojavadoc"));
        pm.saveProject(p);
        assertFalse("no-op remove proj2.dojar failed", r.destroyReference("${reference.proj2.dojar}"));
        assertFalse("no-op remove did not modify project", pm.isModified(p));
        // Try removing second ref. Should now remove project property.
        assertTrue("remove proj2.dojavadoc succeeded", r.destroyReference("${reference.proj2.dojavadoc}"));
        assertTrue("remove ref modified project", pm.isModified(p));
        refs = r.getRawReferences();
        assertEquals("now have no refs", 0, refs.length);
        props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("no more ${project.proj2}", null,
            props.getProperty("project.proj2"));
        assertEquals("no more ${reference.proj2.dojar}", null,
            props.getProperty("reference.proj2.dojar"));
        assertEquals("no more ${reference.proj2.dojavadoc}", null,
            props.getProperty("reference.proj2.dojavadoc"));
        // XXX check add ref not coming from project gives IAE
        
        // test AA with multiple outputs:
        AntArtifact aa = new AntBasedTestUtil.TestAntArtifact(new URI[]{new URI("dist/foo.jar"), new URI("dist/bar.jar")}, sisterh);
        String ref1 = r.addReference(aa, aa.getArtifactLocations()[0]);
        String ref2 = r.addReference(aa, aa.getArtifactLocations()[1]);
        assertEquals("correct evaluated ref1",
            new File(new File(FileUtil.toFile(sisterprojdir), "dist"), "foo.jar"),
            h.resolveFile(pev.evaluate(ref1)));
        assertEquals("correct evaluated ref2",
            new File(new File(FileUtil.toFile(sisterprojdir), "dist"), "bar.jar"),
            h.resolveFile(pev.evaluate(ref2)));
        r.destroyReference(ref1);
        assertEquals("correct evaluated ref2",
            new File(new File(FileUtil.toFile(sisterprojdir), "dist"), "bar.jar"),
            h.resolveFile(pev.evaluate(ref2)));
        assertEquals("ref1 does not exist", ref1, pev.evaluate(ref1));
        r.destroyReference(ref2);
        assertEquals("ref1 does not exist", ref1, pev.evaluate(ref1));
        assertEquals("ref2 does not exist", ref2, pev.evaluate(ref2));
        pm.saveProject(p);
    }
    
    @SuppressWarnings("deprecation")
    public void testReferenceEscaping() throws Exception {
        // check that artifact reference is correctly escaped. All dot characters
        // in project name or artifact ID must be escaped, etc.
        FileObject proj4Folder = FileUtil.createFolder(scratch, "proj4");
        AntProjectHelper proj4Helper = ProjectGenerator.createProject(proj4Folder, "test");
        setCodeNameOfTestProject(proj4Helper, "pro-ject.4");
        Project p = pm.findProject(projdir);
        ReferenceHelper referenceHelperProj4 = p.getLookup().lookup(ReferenceHelper.class);
        AntArtifact art = proj4Helper.createSimpleAntArtifact("jar", "build.jar", proj4Helper.getStandardPropertyEvaluator(), "do.jar", "clean");
        String ref = referenceHelperProj4.addReference(art, art.getArtifactLocations()[0]);
        assertEquals("Project reference was not correctly escaped", "${reference.pro-ject_4.do_jar}", ref);
        
        // test that it can be found
        ReferenceHelper.RawReference rr = referenceHelperProj4.getRawReference("pro-ject_4", "do_jar", false);
        assertNull("Cannot be found because it was escaped", rr);
        rr = referenceHelperProj4.getRawReference("pro-ject_4", "do_jar", true);
        assertNotNull("Created reference was not created", rr);
        assertEquals("do.jar", rr.getID());
        assertEquals("pro-ject_4", rr.getForeignProjectName());
        
        // test deletion
        referenceHelperProj4.destroyForeignFileReference(ref);
        rr = referenceHelperProj4.getRawReference("pro-ject_4", "do_jar", true);
        assertNull("Reference was not deleted", rr);
    }
        
    public void testArtifactProperties() throws Exception {
        assertFalse("project not initially modified", pm.isModified(p));
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        art.getProperties().setProperty("configuration", "debug");
        art.getProperties().setProperty("empty", "");
        assertFalse("reference exist", r.isReferenced(art, art.getArtifactLocations()[0]));
        assertEquals("added a ref to proj2.dojar", "${reference.proj2.dojar}", r.addReference(art, art.getArtifactLocations()[0]));
        assertTrue("reference exist", r.isReferenced(art, art.getArtifactLocations()[0]));
        assertTrue("project now modified", pm.isModified(p));
        ProjectManager.getDefault().saveAllProjects();
        ReferenceHelper.RawReference[] refs = r.getRawReferences();
        assertEquals("one ref now", 1, refs.length);
        ReferenceHelper.RawReference ref = refs[0];
        assertEquals("correct foreign project name", "proj2", ref.getForeignProjectName());
        assertEquals("correct artifact type", "jar", ref.getArtifactType());
        assertEquals("correct script location", "${project.proj2}/build.xml", ref.getScriptLocationValue());
        assertEquals("correct target name", "dojar", ref.getTargetName());
        assertEquals("correct clean target name", "clean", ref.getCleanTargetName());
        assertEquals("correct property keys", new TreeSet<String>(Arrays.asList(new String[]{"configuration", "empty"})), ref.getProperties().keySet());
        assertEquals("correct property values", new TreeSet<String>(Arrays.asList(new String[]{"debug", ""})),
                new TreeSet<Object>(ref.getProperties().values()));
    }

    /**
     * Check that the {@link SubprojectProvider} implementation behaves correctly.
     * @throws Exception in case of unexpected failures
     */
    public void testSubprojectProviderImpl() throws Exception {
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertNotNull("added a ref to proj2.dojar", r.addReference(art, art.getArtifactLocations()[0]));
        art = sisterh.createSimpleAntArtifact("javadoc", "build.javadoc", sisterh.getStandardPropertyEvaluator(), "dojavadoc", "clean");
        assertNotNull("added a ref to proj2.dojavadoc", r.addReference(art, art.getArtifactLocations()[0]));
        art = seph.createSimpleAntArtifact("jar", "build.jar", seph.getStandardPropertyEvaluator(), "dojar", "clean");
        assertNotNull("added a ref to proj3.dojar", r.addReference(art, art.getArtifactLocations()[0]));
        SubprojectProvider sp = r.createSubprojectProvider();
        Set<? extends Project> subprojs = sp.getSubprojects();
        assertEquals("two subprojects", 2, subprojs.size());
        Project[] subprojsA = subprojs.toArray(new Project[2]);
        Project proj2, proj3;
        if (ProjectUtils.getInformation(subprojsA[0]).getName().equals("proj2")) {
            proj2 = subprojsA[0];
            proj3 = subprojsA[1];
        } else {
            proj2 = subprojsA[1];
            proj3 = subprojsA[0];
        }
        assertEquals("proj2 was found correctly", pm.findProject(sisterprojdir), proj2);
        assertEquals("proj3 was found correctly", pm.findProject(sepprojdir), proj3);
    }

    /**
     * Check that methods to add foreign file references really work.
     * @throws Exception in case of unexpected failure
     */
    @SuppressWarnings("deprecation")
    public void testForeignFileReferences() throws Exception {        
        // test collocated foreign project reference
        File f = new File(new File(FileUtil.toFile(sisterprojdir), "dist"), "proj2.jar");
        assertEquals("can add a ref to an artifact", "${reference.proj2.dojar}", r.createForeignFileReference(f, "jar"));
        assertEquals("creating reference second time must return already existing ID", "${reference.proj2.dojar}", r.createForeignFileReference(f, "jar"));
        assertNotNull("ref added", r.getRawReference("proj2", "dojar"));                
        EditableProperties privateProps = h.getProperties (AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        String refval = privateProps.getProperty("project.proj2");        
        assertNull("reference correctly stored into private.properties", refval);        
        EditableProperties projectProps = h.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        refval = projectProps.getProperty("project.proj2");        
        assertEquals("reference correctly stored into project.properties", "../proj2", refval);        
        refval = pev.getProperty("reference.proj2.dojar");
        assertNotNull("reference correctly evaluated", refval);                
        assertEquals("reference correctly evaluated", f, h.resolveFile(refval));
        AntArtifact art = r.getForeignFileReferenceAsArtifact("${reference.proj2.dojar}");
        assertNotNull("got the reference back", art);
        assertEquals("correct project", sisterprojdir, art.getProject().getProjectDirectory());
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct type", "jar", art.getType());
        assertEquals("correct artifact location", URI.create("dist/proj2.jar"), art.getArtifactLocations()[0]);
        art = (AntArtifact)r.findArtifactAndLocation("reference.proj2.dojar")[0];
        assertNull("bad format", art);
        art = (AntArtifact)r.findArtifactAndLocation("${reference.proj2.doojar}")[0];
        assertNull("wrong target name", art);
        File f2 = new File(new File(FileUtil.toFile(sisterprojdir2), "dist"), "proj2.jar");
        assertEquals("reference ID must be unique", "${reference.proj2-1.dojar}", r.createForeignFileReference(f2, "jar"));
        assertEquals("creating reference second time must return already existing ID", "${reference.proj2-1.dojar}", r.createForeignFileReference(f2, "jar"));
        r.destroyForeignFileReference("${reference.proj2-1.dojar}");
        assertNull("ref removed", r.getRawReference("proj2-1", "dojar"));
        r.destroyForeignFileReference("${reference.proj2.dojar}");
        assertNull("ref removed", r.getRawReference("proj2", "dojar"));
        assertNull("project ref property removed", pev.getProperty("reference.proj2.dojar"));
        assertEquals("no refs remaining", 0, r.getRawReferences().length);
        
        // test non-collocated foreign project reference
        FileObject nonCollocatedProjectLib = scratch.getFileObject("separate/proj3").createFolder("d i s t").createData("p r o j 3.jar");
        f = FileUtil.toFile(nonCollocatedProjectLib);
        art = AntArtifactQuery.findArtifactByID(pm.findProject(sepprojdir), "dojar");
        assertNotNull("have an artifact proj3.dojar", art);
        assertEquals("can add a reference to a direct artifact", "${reference.proj3.dojar}", r.createForeignFileReference(art));
        assertEquals("creating reference second time must return already existing ID", "${reference.proj3.dojar}", r.createForeignFileReference(art));
        assertNotNull("ref added", r.getRawReference("proj3", "dojar"));
        refval = pev.getProperty("reference.proj3.dojar");
        String val = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty("reference.proj3.dojar");
        assertEquals("reference was not correctly set", "${project.proj3}/d i s t/p r o j 3.jar", val);
        assertEquals("reference correctly evaluated", f, h.resolveFile(refval));
        val = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty("project.proj3");
        /* XXX failing: #137760
        assertEquals("reference correctly evaluated", FileUtil.toFile(sepprojdir).getAbsolutePath(), val);
        art = (AntArtifact)r.findArtifactAndLocation("${reference.proj3.dojar}")[0];
        assertNotNull("got the reference back", art);
        assertEquals("correct project", sepprojdir, art.getProject().getProjectDirectory());
        assertEquals("correct target name", "dojar", art.getTargetName());
        r.destroyForeignFileReference("${reference.proj3.dojar}");
        assertNull("ref removed", r.getRawReference("proj3", "dojar"));
        assertNull("project ref property removed", pev.getProperty("reference.proj3.dojar"));
        assertEquals("no refs remaining", 0, r.getRawReferences().length);

        // test foreign file reference for collocated jar
        FileObject collocatedLib = scratch.createFolder("j a r s").createData("m y l i b.jar");
        f = FileUtil.toFile(collocatedLib);
        String ref = r.createForeignFileReference(f, "jar");
        String ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.m_y_l_i_b.jar}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated from project.properties", "../j a r s/m y l i b.jar", refval);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertNull("Reference was not correctly evaluated from private.properties",  refval);        
        collocatedLib = scratch.createFolder("jars2").createData("m y l i b.jar");
        f = FileUtil.toFile(collocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.m_y_l_i_b.jar-1}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));        
        assertEquals("Reference was not correctly evaluated form project.properties", "../jars2/m y l i b.jar", refval);        
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));        
        assertNull("Reference was not correctly evaluated from private.properties", refval);                        
        collocatedLib = scratch.createFolder("jars3").createData("m y l i b.jar");
        f = FileUtil.toFile(collocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.m_y_l_i_b.jar-2}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated from project.properties", "../jars3/m y l i b.jar", refval);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertNull("Reference was not correctly evaluated from private.properties", refval);
        
        assertTrue("Reference was not removed", r.destroyReference(ref));
        assertFalse("There should not be any reference", r.destroyReference(ref));
        refval = pev.evaluate(ref);
        assertEquals("Reference was not removed", ref, refval);
        
        // test foreign file reference for non-collocated jar
        FileObject nonCollocatedLib = scratch.getFileObject("separate").createFolder("jars").createData("mylib2.jar");
        f = FileUtil.toFile(nonCollocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.mylib2.jar}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertNull ("Foreign file reference is stored into project.properties",refval);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));        
        assertEquals("Reference was not correctly evaluated", f.getAbsolutePath(), refval);
        assertEquals("Reference was not correctly evaluated", f, h.resolveFile(refval));
        nonCollocatedLib = scratch.getFileObject("separate").createFolder("jars2").createData("mylib2.jar");
        f = FileUtil.toFile(nonCollocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.mylib2.jar-1}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertNull ("Foreign file reference is stored into project.properties",refval);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", f.getAbsolutePath(), refval);
        nonCollocatedLib = scratch.getFileObject("separate").createFolder("jars3").createData("mylib2.jar");
        f = FileUtil.toFile(nonCollocatedLib);
        ref = r.createForeignFileReference(f, "jar");
        ref2 = r.createForeignFileReference(f, "jar");
        assertEquals("Duplicate reference created", ref, ref2);
        assertEquals("Foreign file reference was not correctly created", "${file.reference.mylib2.jar-2}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertNull ("Foreign file reference is stored into project.properties",refval);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("Reference was not correctly evaluated", f.getAbsolutePath(), refval);
        r.destroyForeignFileReference(ref);
        refval = pev.evaluate(ref);
        assertEquals("Reference was not removed", ref, refval);
        */
    } 
    
    public void testToAntArtifact() throws Exception {
        ReferenceHelper.RawReference ref = new ReferenceHelper.RawReference(
            "proj2", "irrelevant", new URI("also-irrelevant"), "dojar", "totally-irrelevant", "dojar");
        AntArtifact art = ref.toAntArtifact(r);
        assertNull("${project.proj2} not set, will not be found", art);
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("project.proj2", "../proj2");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        art = ref.toAntArtifact(r);
        assertNotNull("now artifact will be found", art);
        assertEquals("correct directory", sisterprojdir, art.getProject().getProjectDirectory());
        assertEquals("correct artifact location", URI.create("dist/proj2.jar"), art.getArtifactLocations()[0]);
        assertEquals("correct script location", new File(FileUtil.toFile(sisterprojdir), "build.xml"), art.getScriptLocation());
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct clean target name", "clean", art.getCleanTargetName());
        ref = new ReferenceHelper.RawReference(
            "proj2", "irrelevant", new URI("also-irrelevant"), "doojar", "totally-irrelevant", "doojar");
        art = ref.toAntArtifact(r);
        assertNull("wrong target name, will not be found", art);
    }

    public void testAddRemoveExtraBaseDirectory() throws Exception {
        // test foreign file reference for non-collocated jar under extra base folder
        FileObject nonCollocatedLib = scratch.getFileObject("separate").createFolder("jars").createData("mylib.jar");
        File f = FileUtil.toFile(nonCollocatedLib);
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("externalSourceRoot", "../separate");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        r.addExtraBaseDirectory("externalSourceRoot");
        String ref = r.createForeignFileReference(f, "jar");
        assertEquals("foreign file reference created", "${file.reference.mylib.jar}", ref);
        String refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("reference is using extra base folder", "${externalSourceRoot}/jars/mylib.jar", refval);
        assertEquals("reference is correctly evaluated", f, h.resolveFile(h.getStandardPropertyEvaluator().evaluate(refval)));
        // test removal of extra base folder
        r.removeExtraBaseDirectory("externalSourceRoot");
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("reference does not contain extra base folder", "../separate/jars/mylib.jar", refval);
        assertEquals("reference is correctly evaluated", f, h.resolveFile(refval));

        // the same test as above but with extra base folder defined in PRIVATE props
        nonCollocatedLib = scratch.createFolder("separate2").createFolder("jars").createData("mylib2.jar");
        f = FileUtil.toFile(nonCollocatedLib);
        props = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        String absolutePath = FileUtil.toFile(scratch.getFileObject("separate2")).getAbsolutePath();
        props.setProperty("externalSourceRootAbsolute", absolutePath);
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, props);
        r.addExtraBaseDirectory("externalSourceRootAbsolute");
        ref = r.createForeignFileReference(f, "jar");
        assertEquals("foreign file reference created", "${file.reference.mylib2.jar}", ref);
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertEquals("reference is using extra base folder", "${externalSourceRootAbsolute}/jars/mylib2.jar", refval);
        assertEquals("reference is correctly evaluated", f, h.resolveFile(h.getStandardPropertyEvaluator().evaluate(refval)));
        r.removeExtraBaseDirectory("externalSourceRootAbsolute");
        refval = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertNull("reference was removed from PROJECT_PROPERTIES_PATH", refval);
        refval = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref.substring(2, ref.length()-1));
        assertNotNull("reference was moved to PRIVATE_PROPERTIES_PATH", refval);
        assertEquals("reference does not contain extra base folder", absolutePath+"/jars/mylib2.jar", refval);
        assertEquals("reference is correctly evaluated", f, h.resolveFile(h.getStandardPropertyEvaluator().evaluate(refval)));
    }

    /**
     * Check that adding and removing artifact locations dynamically doesn't break anything.
     * Cf. #55413 and #55423.
     */
    public void testDeletionOfArtifactLocations() throws Exception {
        final URI[] locations = {
            URI.create("dist/output1.jar"),
            URI.create("dist/output2.jar"),
            URI.create("dist/output3.jar"),
        };
        final boolean[] includeLocations = {
            true,
            false,
            true,
        };
        class MultiAntArtifact extends AntArtifact {
            public String getType() {
                return "jar";
            }
            public String getTargetName() {
                return "build";
            }
            public String getCleanTargetName() {
                return "clean";
            }
            public File getScriptLocation() {
                return sisterh.resolveFile("build.xml");
            }
            public URI[] getArtifactLocations() {
                List<URI> locs = new ArrayList<URI>();
                for (int i = 0; i < locations.length; i++) {
                    if (includeLocations[i]) {
                        locs.add(locations[i]);
                    }
                }
                return locs.toArray(new URI[0]);
            }
        }
        AntArtifact art = new MultiAntArtifact();
        assertFalse("project not initially modified", pm.isModified(p));
        assertFalse("no refs yet", r.isReferenced(art, locations[0]));
        assertEquals("added a ref to output1.jar", "${reference.proj2.build}", r.addReference(art, locations[0]));
        assertEquals("added a ref to output3.jar", "${reference.proj2.build.1}", r.addReference(art, locations[2]));
        try {
            r.addReference(art, locations[1]);
            fail("Should not be permitted to add ref to output2.jar yet");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        assertTrue("output1.jar ref'd", r.isReferenced(art, locations[0]));
        assertTrue("output3.jar ref'd", r.isReferenced(art, locations[2]));
        try {
            r.isReferenced(art, locations[1]);
            fail("Should not be permitted to check ref to output2.jar yet");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        // Make sure proj2 actually reports our special provider:
        Project sisterproj = ProjectManager.getDefault().findProject(sisterprojdir);
        sisterproj.getLookup().lookup(AntBasedTestUtil.AntArtifactProviderMutable.class).
            setBuildArtifacts(new AntArtifact[] {art});
        // Now check findArtifactAndLocation usage.
        assertEquals("output1.jar found",
                Arrays.asList(new Object[] {art, locations[0]}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build}")));
        assertEquals("output3.jar found",
                Arrays.asList(new Object[] {art, locations[2]}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build.1}")));
        // Now add output2.jar to list and see that lookups work somehow.
        includeLocations[1] = true;
        assertEquals("output1.jar still there",
                Arrays.asList(new Object[] {art, locations[0]}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build}")));
        assertEquals("output2.jar now magically referenced instead of output3.jar (but oh well)",
                Arrays.asList(new Object[] {art, locations[1]}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build.1}")));
        assertEquals("output3.jar now magically referenced (even though we have no such property ourselves)",
                Arrays.asList(new Object[] {art, locations[2]}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build.2}")));
        // Now *remove* some items and see what happens!
        includeLocations[0] = false;
        includeLocations[1] = false;
        assertEquals("output3.jar now only referent",
                Arrays.asList(new Object[] {art, locations[2]}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build}")));
        assertEquals("second item no longer exists",
                Arrays.asList(new Object[] {null, null}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build.1}")));
        assertEquals("third item no longer exists",
                Arrays.asList(new Object[] {null, null}),
                Arrays.asList(r.findArtifactAndLocation("${reference.proj2.build.2}")));
        assertTrue("output3.jar ref'd", r.isReferenced(art, locations[2]));
        try {
            r.isReferenced(art, locations[0]);
            fail("Should not be permitted to check ref to first item any more, oops");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            r.isReferenced(art, locations[1]);
            fail("Should not be permitted to check ref to second item any more");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        // Now destroy the references and make sure there are no issues.
        assertTrue("Can really get rid of output3.jar (even though it used to be output1.jar)",
            r.destroyReference("${reference.proj2.build}"));
        assertTrue("Can also get rid of what used to be output3.jar somehow - just the property at least",
            r.destroyReference("${reference.proj2.build.1}"));
        assertFalse("output3.jar no longer ref'd", r.isReferenced(art, locations[2]));
        assertEquals("No raw references left", Collections.EMPTY_LIST, Arrays.asList(r.getRawReferences()));
        assertEquals("No shared properties left", Collections.EMPTY_MAP, h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        assertEquals("No private properties left", Collections.EMPTY_MAP, h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
    }
    
    private void writeProperties(FileObject prop, String[] keys, String[] values) throws Exception {
        EditableProperties p = new EditableProperties(false);

        for (int cntr = 0; cntr < keys.length; cntr++) {
            p.setProperty(keys[cntr], values[cntr]);
        }

        OutputStream os = prop.getOutputStream();
        try {
            p.store(os);
        } finally {
            os.close();
        }
    }
    
    public void testFixReferences() throws Exception {
	FileObject originalProject = scratch.createFolder("orig-proj");
	FileObject originalSrcDir  = originalProject.createFolder("src");
	
	FileObject testProject = scratch.createFolder("test-proj");
	FileObject nbprojectDir = testProject.createFolder("nbproject");
	
	TestUtil.createFileFromContent(ReferenceHelperTest.class.getResource("data/project.xml"), testProject, "nbproject/project.xml");
	
	FileObject publicProperties  = nbprojectDir.createData("project.properties");
	FileObject privateDir        = nbprojectDir.createFolder("private");
	FileObject privateProperties = privateDir.createData("private.properties");
	FileObject srcDir            = testProject.createFolder("src");
	
	writeProperties(publicProperties, new String[] {
	    "file.reference.x",
	}, new String[] {
	    "src",
	});
	
	writeProperties(privateProperties, new String[] {
	    "file.reference.x",
	}, new String[] {
	    FileUtil.toFile(originalSrcDir).getAbsolutePath(),
	});
	
	Project nue = pm.findProject(testProject);
	assertNotNull("found project in " + testProject, nue);
        AntProjectHelper h = nue.getLookup().lookup(AntProjectHelper.class);
        assertNotNull("found helper for " + nue, h);
        ReferenceHelper r = nue.getLookup().lookup(ReferenceHelper.class);
        assertNotNull("found ref helper for " + p, r);
	
	r.fixReferences(FileUtil.toFile(originalProject));
	
	pm.saveProject(nue);
	
	String resolvedProperty = h.getStandardPropertyEvaluator().getProperty("file.reference.x");
	
	assertNotNull("the property can be resolved", resolvedProperty);
	
	File resolvedFile = PropertyUtils.resolveFile(FileUtil.toFile(testProject), resolvedProperty);
	
	assertNotNull("the file can be resolved", resolvedFile);
	
	assertEquals("referencing correct file", FileUtil.toFile(srcDir).getAbsolutePath(), resolvedFile.getAbsolutePath());
    }

    public void testSlash1ToSlash2Upgrade() throws Exception { // #91760
        class AnArtifact extends AntArtifact {
            URI u;
            Properties p;
            public AnArtifact(URI u, Properties p) {
                this.u = u;
                this.p = p;
            }
            public String getType() {
                return "jar";
            }
            public File getScriptLocation() {
                return sisterh.resolveFile(getTargetName() + ".xml");
            }
            public String getTargetName() {
                return u.toString().replaceAll("\\.jar$", "");
            }
            public String getCleanTargetName() {
                return "clean";
            }
            @Override
            public URI[] getArtifactLocations() {
                return new URI[] {u};
            }
            @Override
            public Properties getProperties() {
                return p;
            }

        }
        URI xJar = new URI(null, null, "x.jar", null);
        r.addReference(new AnArtifact(xJar, new Properties()), xJar);
        assertReferenceXmlFragment(ReferenceHelper.REFS_NS, "x.xml");
        Properties p = new Properties();
        p.setProperty("k", "v");
        URI yJar = new URI(null, null, "y.jar", null);
        r.addReference(new AnArtifact(yJar, p), yJar);
        assertReferenceXmlFragment(ReferenceHelper.REFS_NS2, "${project.proj2}/x.xml", "${project.proj2}/y.xml");
        URI zJar = new URI(null, null, "z.jar", null);
        r.addReference(new AnArtifact(zJar, new Properties()), zJar);
        assertReferenceXmlFragment(ReferenceHelper.REFS_NS2, "${project.proj2}/x.xml", "${project.proj2}/y.xml", "${project.proj2}/z.xml");
    }
    private void assertReferenceXmlFragment(String namespace, String... scriptLocations) {
        Element refs = p.getLookup().lookup(AuxiliaryConfiguration.class).getConfigurationFragment(ReferenceHelper.REFS_NAME, namespace, true);
        assertNotNull(refs);
        List<String> actualScriptLocations = new ArrayList<String>();
        for (Element ref : XMLUtil.findSubElements(refs)) {
            Element script = XMLUtil.findElement(ref, "script", namespace);
            actualScriptLocations.add(XMLUtil.findText(script));
        }
        assertEquals(Arrays.asList(scriptLocations), actualScriptLocations);
    }

    public void testProjectLibraryReferences() throws Exception {
        ProjectLibraryProvider.FIRE_CHANGES_SYNCH = true;
        assertProjectLibraryManagers(null);
        File fooJar = new File(getWorkDir(), "foo.jar");
        File f = new File(getWorkDir(), "libs.properties");
        URL loc = Utilities.toURI(f).toURL();
        LibraryManager mgr = LibraryManager.forLocation(loc);
        assertEquals(loc, mgr.getLocation());
        Library fooLib = mgr.createLibrary("j2se", "foo",
                Collections.singletonMap("classpath", Arrays.asList(new URL("jar:" + Utilities.toURI(fooJar) + "!/"))));
        assertEquals(mgr, fooLib.getManager());
        try {
            r.createLibraryReference(fooLib, "classpath");
            fail("cannot reference library which is not reachable from project");
        } catch (IllegalArgumentException ex) {
            // as expected
        }
        h.setLibrariesLocation(f.getAbsolutePath());
        String fooref = r.createLibraryReference(fooLib, "classpath");
        assertEquals("${libs.foo.classpath}", fooref);
        assertEquals(fooJar.getAbsolutePath(), pev.evaluate(fooref).replace('/', File.separatorChar));
        assertProjectLibraryManagers(loc);
        assertEquals("foo", r.findLibrary("foo").getName());
        assertEquals("foo", r.findLibrary("${libs.foo.classpath}").getName());
        assertNull(r.findLibrary("nonexistent"));
        assertNull(r.findLibrary("${libs.nonexistent.classpath}"));
        assertNull(r.findLibrary("${some.other.foo.stuff}"));
        File barDir = new File(getWorkDir(), "bar");
        barDir.mkdirs();
        String barref = r.createLibraryReference(mgr.createLibrary("j2se", "bar",
                Collections.singletonMap("classpath", Arrays.asList(Utilities.toURI(barDir).toURL()))), "classpath");
        assertEquals("${libs.bar.classpath}", barref);
        assertEquals(barDir.getAbsolutePath(), pev.evaluate(barref).replace('/', File.separatorChar));
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put("javac.classpath", "stuff:" + fooref + ":which-is-not-bar");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertProjectLibraryManagers(loc);
        assertEquals("foo", r.findLibrary("foo").getName());
        assertEquals("foo", r.findLibrary("${libs.foo.classpath}").getName());
        assertEquals("bar", r.findLibrary("bar").getName());
        assertEquals("bar", r.findLibrary("${libs.bar.classpath}").getName());
        mgr.createLibrary("j2se", "empty", Collections.<String,List<URL>>emptyMap());
        assertEquals("stuff:" + fooJar + ":which-is-not-bar", pev.evaluate("${javac.classpath}").replace('/', File.separatorChar));
        props.put("javac.classpath", "nolibshere");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        assertEquals("nolibshere", pev.getProperty("javac.classpath"));
        assertEquals(fooJar.getAbsolutePath(), pev.getProperty("libs.foo.classpath").replace('/', File.separatorChar));
        assertProjectLibraryManagers(loc);
        assertEquals("foo", r.findLibrary("foo").getName());
        assertEquals("foo", r.findLibrary("${libs.foo.classpath}").getName());
        assertEquals("bar", r.findLibrary("bar").getName());
        assertEquals("bar", r.findLibrary("${libs.bar.classpath}").getName());
    }
    private void assertProjectLibraryManagers(URL expectedUrl) {
        LibraryManager mgr = r.getProjectLibraryManager();
        if (mgr == null) {
            assertNull(expectedUrl);
        } else {
            assertEquals(expectedUrl, mgr.getLocation());
        }
    }

}
