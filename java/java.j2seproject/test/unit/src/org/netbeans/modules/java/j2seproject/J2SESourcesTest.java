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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.test.MockLookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 * Tests J2SESources
 * Tests if SourceForBinaryQuery works fine on external build folder.
 *
 * @author Tomas Zezula
 */
public class J2SESourcesTest extends NbTestCase {

    public J2SESourcesTest(String testName) {
        super(testName);
    }

    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject build;
    private FileObject classes;
    private ProjectManager pm;
    private Project project;
    private AntProjectHelper helper;
   
    protected @Override int timeOut() {
        return 300000;
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        );
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        sources = getFileObject(projdir, "src");
        build = getFileObject (scratch, "build");
        classes = getFileObject(build,"classes");
        File f = FileUtil.normalizeFile (FileUtil.toFile(build));
        String path = f.getAbsolutePath ();
//#47657: SourcesHelper.remarkExternalRoots () does not work on deleted folders
// To reproduce it uncomment following line
//        build.delete();
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty(ProjectProperties.BUILD_DIR, path);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        pm = ProjectManager.getDefault();
        project = pm.findProject(projdir);
        assertTrue("Invalid project type", project instanceof J2SEProject);
    }

    protected @Override void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sources = null;
        build = null;
        classes = null;
        pm = null;
        project = null;
        helper = null;
        super.tearDown();
    }

    public void testSourceRoots () throws Exception {        
        FileObject[] roots = SourceForBinaryQuery.findSourceRoots(classes.toURL()).getRoots();
        assertNotNull (roots);        
        assertEquals("There should be 1 src root",1,roots.length);
        assertTrue ("The source root is not valid", sources.isValid());
        assertEquals("Invalid src root", sources, roots[0]);               
        FileObject src2 = projdir.createFolder("src2");        
        addSourceRoot (helper, src2, "src2");        
        roots = SourceForBinaryQuery.findSourceRoots(classes.toURL()).getRoots();
        assertNotNull (roots);
        assertEquals("There should be 2 src roots", 2, roots.length);
        assertTrue ("The source root is not valid", sources.isValid());
        assertEquals("Invalid src root", sources, roots[0]);
        assertTrue ("The source root 2 is not valid", src2.isValid());
        assertEquals("Invalid src2 root", src2, roots[1]);
    }

    public void testIncludesExcludes() throws Exception {
        SourceGroup g = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)[0];
        assertEquals(sources, g.getRootFolder());
        FileObject objectJava = FileUtil.createData(sources, "java/lang/Object.java");
        FileObject jcJava = FileUtil.createData(sources, "javax/swing/JComponent.java");
        FileObject doc = FileUtil.createData(sources, "javax/swing/doc-files/index.html");
        assertTrue(g.contains(objectJava));
        assertTrue(g.contains(jcJava));
        assertTrue(g.contains(doc));
        Method projectOpened = ProjectOpenedHook.class.getDeclaredMethod("projectOpened");
        projectOpened.setAccessible(true);
        projectOpened.invoke(project.getLookup().lookup(ProjectOpenedHook.class));
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("includes/excludes were initialized to defaults", "**", ep.getProperty(ProjectProperties.INCLUDES));
        assertEquals("includes/excludes were initialized to defaults", "", ep.getProperty(ProjectProperties.EXCLUDES));
        ep.setProperty(ProjectProperties.INCLUDES, "javax/swing/");
        ep.setProperty(ProjectProperties.EXCLUDES, "**/doc-files/");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        pm.saveProject(project);
        assertFalse(g.contains(objectJava));
        assertTrue(g.contains(jcJava));
        assertFalse(g.contains(doc));
    }

    @RandomlyFails // on various builders, and w/o dump despite timeOut
    public void testFiring() throws Exception {
        final Sources s = project.getLookup().lookup(Sources.class);
        final SourceRoots roots = ((J2SEProject)project).getSourceRoots();
        SourceGroup[] groups = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, groups.length);
        class EventCounter implements ChangeListener {            
            final AtomicInteger count = new AtomicInteger();
            public @Override void stateChanged(ChangeEvent e) {
                count.incrementAndGet();
            }            
        }
        final EventCounter counter = new EventCounter();
        s.addChangeListener(counter);
        final URL[] oldRootUrls = roots.getRootURLs();
        final String[] oldRootLabels = roots.getRootNames();
        final String[] oldRootProps = roots.getRootProperties();
        final FileObject newRoot = projdir.createFolder("new_src"); //NOI18N
        //test: adding of src root should fire once
        URL[] newRootUrls = new URL[oldRootUrls.length+1];
        System.arraycopy(oldRootUrls, 0, newRootUrls, 0, oldRootUrls.length);
        newRootUrls[newRootUrls.length-1] = newRoot.toURL();
        String[] newRootLabels = new String[oldRootLabels.length+1];
        for (int i=0; i< oldRootLabels.length; i++) {
            newRootLabels[i] = roots.getRootDisplayName(oldRootLabels[i], oldRootProps[i]);
        }
        newRootLabels[newRootLabels.length-1] = newRoot.getName();
        roots.putRoots(newRootUrls, newRootLabels);
        assertEquals(1, counter.count.get());
        groups = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(3, groups.length);
        //test: removing of src root should fire once
        counter.count.set(0);
        newRootUrls = new URL[oldRootUrls.length];
        System.arraycopy(oldRootUrls, 0, newRootUrls, 0, oldRootUrls.length);
        newRootLabels = new String[oldRootLabels.length];
        for (int i=0; i< oldRootLabels.length; i++) {
            newRootLabels[i] = roots.getRootDisplayName(oldRootLabels[i], oldRootProps[i]);
        }
        roots.putRoots(newRootUrls, newRootLabels);
        assertEquals(1, counter.count.get());
        groups = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, groups.length);
    }
    
    private static FileObject getFileObject (FileObject parent, String name) throws IOException {
        FileObject result = parent.getFileObject(name);
        if (result == null) {
            result = parent.createFolder(name);
        }
        return result;
    }   
    

    private static void addSourceRoot (AntProjectHelper helper, FileObject sourceFolder, String propName) throws Exception {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttribute("id", propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        File f = FileUtil.normalizeFile(FileUtil.toFile(sourceFolder));
        props.put (propName, f.getAbsolutePath());
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
    }


}
