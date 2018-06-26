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
