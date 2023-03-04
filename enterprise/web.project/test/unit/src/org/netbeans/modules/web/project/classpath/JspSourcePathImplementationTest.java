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

package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class JspSourcePathImplementationTest extends NbTestCase {
    
    public JspSourcePathImplementationTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        // just in order to add our repository implementation
        MockLookup.setLayersAndInstances();
    }
    
    public void testJspSourcePathImplementation() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        FileObject projectDir = FileUtil.toFileObject(f);
        FileUtil.createFolder(projectDir, "web2");
        
        Project p = ProjectManager.getDefault().findProject(projectDir);
        
        // XXX should not cast a Project
        final AntProjectHelper helper = ((WebProject)p).getAntProjectHelper();
        
        JspSourcePathImplementation jspi = new JspSourcePathImplementation(helper, helper.getStandardPropertyEvaluator());
        
        final boolean[] changed = new boolean[1];
        
        jspi.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JspSourcePathImplementation.PROP_RESOURCES)) {
                    changed[0] = true;
                }
            }
        });
        
        // simple test 
        
        PathResourceImplementation res = (PathResourceImplementation)jspi.getResources().get(0);
        assertTrue(res.getRoots()[0].equals(helper.resolveFileObject("web").getURL()));
        
        // test the change of the web pages folder
        
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                props.setProperty(WebProjectProperties.WEB_DOCBASE_DIR, "web2");
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
            }
        });
        
        assertTrue(changed[0]);
        res = (PathResourceImplementation)jspi.getResources().get(0);
        assertTrue(res.getRoots()[0].equals(helper.resolveFileObject("web2").getURL()));
        
        // test the deletion of the web pages folder
        
        changed[0] = false;
        projectDir.getFileObject("web2").delete();
        
        assertTrue(changed[0]);
        assertTrue(jspi.getResources().size() == 0);
        
        // test the recreation of the web pages folder
        
        changed[0] = false;
        FileUtil.createFolder(projectDir, "web2");
        
        assertTrue(changed[0]);
        res = (PathResourceImplementation)jspi.getResources().get(0);
        assertTrue(res.getRoots()[0].equals(helper.resolveFileObject("web2").getURL()));
        
        // test the deletion of web pages folder and changing it to an existing folder
        
        changed[0] = false;
        projectDir.getFileObject("web2").delete();
        
        assertTrue(changed[0]);
        assertTrue(jspi.getResources().size() == 0);
        
        changed[0] = false;
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                props.setProperty(WebProjectProperties.WEB_DOCBASE_DIR, "web");
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
            }
        });
        
        assertTrue(changed[0]);
        res = (PathResourceImplementation)jspi.getResources().get(0);
        assertTrue(res.getRoots()[0].equals(helper.resolveFileObject("web").getURL()));
        
        // test the JSPI instance can be GC'd
        
        WeakReference ref = new WeakReference(jspi);
        jspi = null;
        
        assertGC("The JSPI instance could not be GC'd.", ref);
    }
}
