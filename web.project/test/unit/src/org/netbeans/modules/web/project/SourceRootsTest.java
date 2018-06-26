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

package org.netbeans.modules.web.project;

import java.net.URL;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

public class SourceRootsTest extends NbTestCase {

    public SourceRootsTest (String testName) {
        super(testName);
    }

    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private WebProject pp;
    private AntProjectHelper helper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLookup.init();
        Collection<? extends AntBasedProjectType> all = Lookups.forPath("Services/AntBasedProjectTypes").lookupAll(AntBasedProjectType.class);
        Iterator<? extends AntBasedProjectType> it = all.iterator();
        AntBasedProjectType t = it.next();
        MockLookup.setInstances(
            t,
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        );
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        projdir = FileUtil.toFileObject(f);
        sources = projdir.getFileObject("src/java");
        tests = projdir.getFileObject("test");
        Project p = ProjectManager.getDefault().findProject(projdir);
        assertTrue("Invalid project type",p instanceof WebProject);
        pp = (WebProject) p;
        helper = pp.getAntProjectHelper();
    }

    @Override
    protected void tearDown() throws Exception {
        projdir = null;
        sources = null;
        tests = null;
        pp = null;
        helper = null;
        MockLookup.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    public void testSourceRoots () throws Exception {
        SourceRoots sources = pp.getSourceRoots();
        String[] srcProps = sources.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 1",1,srcProps.length);
        assertEquals("Source property should be src.dir","src.dir",srcProps[0]);
        FileObject[] srcFos = sources.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 1",1,srcFos.length);
        assertEquals("Root should be "+this.sources.getPath(),this.sources,srcFos[0]);
        URL[] srcURLs = sources.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 1",1,srcURLs.length);
        assertEquals("Root URLs should be "+this.sources.getURL(),this.sources.getURL(),srcURLs[0]);
        SourceRoots tests = pp.getTestSourceRoots();
        srcProps = tests.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 1",1,srcProps.length);
        assertEquals("Source property should be test.src.dir","test.src.dir",srcProps[0]);
        srcFos = tests.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 1",1,srcFos.length);
        assertEquals("Root should be "+this.tests.getPath(),this.tests,srcFos[0]);
        srcURLs = tests.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 1",1,srcURLs.length);
        assertEquals("Root URLs should be "+this.tests.getURL(),this.tests.getURL(),srcURLs[0]);
        //Now add new source root
        TestListener tl = new TestListener();
        sources.addPropertyChangeListener (tl);
        FileObject newRoot = addSourceRoot (helper, projdir, "src.other.dir","other");
        srcProps = sources.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 2",2,srcProps.length);
        assertEquals("The first source property should be src.dir","src.dir",srcProps[0]);
        assertEquals("The second source property should be src.other.dir","src.other.dir",srcProps[1]);
        srcFos = sources.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 2",2,srcFos.length);
        assertEquals("The first root should be "+this.sources.getPath(),this.sources,srcFos[0]);
        assertEquals("The second root should be "+newRoot.getPath(),newRoot,srcFos[1]);
        srcURLs = sources.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 2",2,srcURLs.length);
        assertEquals("The first root URLs should be "+this.sources.getURL(),this.sources.getURL(),srcURLs[0]);
        assertEquals("The second root URLs should be "+newRoot.getURL(),newRoot.getURL(),srcURLs[1]);
        Set events = tl.getEvents();
        assertTrue ("PROP_ROOT_PROPERTIES has to be fired",events.contains(SourceRoots.PROP_ROOT_PROPERTIES));
        assertTrue ("PROP_ROOTS has to be fired",events.contains(SourceRoots.PROP_ROOTS));
        tl.reset();
        newRoot = changeSourceRoot (helper, projdir, "src.other.dir","other2");
        srcProps = sources.getRootProperties();
        assertNotNull ("Source properties can not be null",srcProps);
        assertEquals ("Source properties length must be 2",2,srcProps.length);
        assertEquals("The first source property should be src.dir","src.dir",srcProps[0]);
        assertEquals("The second source property should be src.other.dir","src.other.dir",srcProps[1]);
        srcFos = sources.getRoots();
        assertNotNull ("Roots can not be null",srcFos);
        assertEquals ("Roots length must be 2",2,srcFos.length);
        assertEquals("The first root should be "+this.sources.getPath(),this.sources,srcFos[0]);
        assertEquals("The second root should be "+newRoot.getPath(),newRoot,srcFos[1]);
        srcURLs = sources.getRootURLs();
        assertNotNull ("Root URLs can not be null",srcURLs);
        assertEquals ("Root URLs length must be 2",2,srcURLs.length);
        assertEquals("The first root URLs should be "+this.sources.getURL(),this.sources.getURL(),srcURLs[0]);
        assertEquals("The second root URLs should be "+newRoot.getURL(),newRoot.getURL(),srcURLs[1]);
        events = tl.getEvents();
        assertTrue ("Only PROP_ROOTS has to be fired", events.size() == 1 && events.contains(SourceRoots.PROP_ROOTS));
        sources.removePropertyChangeListener(tl);
    }

    public static FileObject addSourceRoot (AntProjectHelper helper, FileObject projdir,
                                            String propName, String folderName) throws Exception {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagNameNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
        assert nl.getLength() == 1;
        Element roots = (Element) nl.item(0);
        Document doc = roots.getOwnerDocument();
        Element root = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");
        root.setAttributeNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"id",propName);
        roots.appendChild (root);
        helper.putPrimaryConfigurationData (data,true);
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.put (propName,folderName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        FileObject fo = projdir.getFileObject(folderName);
        if (fo==null) {
            fo = projdir.createFolder(folderName);
        }
        return fo;
    }

    public static FileObject changeSourceRoot (AntProjectHelper helper, FileObject projdir,
                                               String propName, String folderName) throws Exception {
        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert props.containsKey(propName);
        props.put (propName,folderName);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        FileObject fo = projdir.getFileObject(folderName);
        if (fo==null) {
            fo = projdir.createFolder(folderName);
        }
        return fo;
    }

    private static final class TestListener implements PropertyChangeListener {
        Set events = new HashSet ();

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (propName != null) {
                this.events.add (propName);
            }
        }

        public void reset () {
            this.events.clear();
        }

        public Set getEvents () {
            return Collections.unmodifiableSet(this.events);
        }
    }

}
