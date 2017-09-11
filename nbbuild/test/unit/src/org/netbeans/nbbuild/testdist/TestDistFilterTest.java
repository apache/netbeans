/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild.testdist;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.tools.ant.Project;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author pzajac
 */
public class TestDistFilterTest extends NbTestCase {
    private static final String ORG_OPENIDE_UNIT = "unit/platform/org-openide";
    private static final String ORG_OPENIDE_FS = "unit/platform/org-openide-fs";
    private static final String ORG_OPENIDE_LOADERS = "unit/platform/org-openide-loaders";
    private static final String ORG_OPENIDE_FS_QA = "qa-functional/platform/org-openide-fs";   
    private static final String ORG_OPENIDE_NO_LOADERS = "unit/platform/org-openide-no-loaders";
    public TestDistFilterTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testDistFilter() throws IOException {
        TestDistFilter filter = new TestDistFilter();
        filter.setTestDistDir(getWorkDir());
        createModule(ORG_OPENIDE_UNIT);
        Project prj = getProject();
        filter.setProject(prj);
        filter.setTesttype("unit");
        
        createModule(ORG_OPENIDE_UNIT);
        
        filter.setTestListProperty("list.prop1");
        filter.execute();
        assertProperty(prj,"list.prop1",new String[]{ORG_OPENIDE_UNIT});
        
        filter.setTestListProperty("list.prop3");
        createModule(ORG_OPENIDE_LOADERS); 
        filter.execute();
        assertProperty(prj,"list.prop3",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_LOADERS});

        createModule(ORG_OPENIDE_FS_QA);
        filter.setTestListProperty("list.prop4");
        filter.execute();
        assertProperty(prj,"list.prop4",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_LOADERS});

        filter.setTestListProperty("list.prop7");
        filter.setTesttype("qa-functional");
        filter.execute();
        assertProperty(prj,"list.prop7",new String[]{ORG_OPENIDE_FS_QA});        
   }
   
    public void testRequiredModules() throws IOException {
        TestDistFilter filter = new TestDistFilter();
        filter.setTestDistDir(getWorkDir());
        Project prj = getProject();
        filter.setProject(prj);
        filter.setTesttype("unit");
        
        String FS = "modules/org-openide-filesystems.jar";
        String LOADERS = "modules/org-openide-loaders.jar";
        createModule(ORG_OPENIDE_UNIT,LOADERS); 
        createModule(ORG_OPENIDE_FS,FS); 
        createModule(ORG_OPENIDE_LOADERS,FS + ":" + LOADERS ); 
        createModule(ORG_OPENIDE_NO_LOADERS,FS); 
        filter.setRequiredModules("org-openide-filesystems.jar");
        filter.setTestListProperty("list.prop");
        filter.execute();
        assertProperty(prj,"list.prop",new String[]{ORG_OPENIDE_FS,ORG_OPENIDE_LOADERS,ORG_OPENIDE_NO_LOADERS});

        filter.setRequiredModules(null);
        filter.setTestListProperty("list.prop1");
        filter.execute();
        assertProperty(prj,"list.prop1",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_FS,ORG_OPENIDE_LOADERS,ORG_OPENIDE_NO_LOADERS});

        filter.setRequiredModules("");
        filter.setTestListProperty("list.prop11");
        filter.execute();
        assertProperty(prj,"list.prop11",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_FS,ORG_OPENIDE_LOADERS,ORG_OPENIDE_NO_LOADERS});
        
        filter.setRequiredModules("org-openide-loaders.jar");
        filter.setTestListProperty("list.prop2");
        filter.execute();
        assertProperty(prj,"list.prop2",new String[]{ORG_OPENIDE_UNIT,ORG_OPENIDE_LOADERS});

        filter.setRequiredModules("none.jar");
        filter.setTestListProperty("list.prop3");
        filter.execute();
        assertProperty(prj,"list.prop3",new String[]{});
        
        filter.setRequiredModules("org-openide-loaders.jar,org-openide-filesystems.jar");
        filter.setTestListProperty("list.prop4");
        filter.execute();
        assertProperty(prj,"list.prop4",new String[]{ORG_OPENIDE_LOADERS,ORG_OPENIDE_UNIT,ORG_OPENIDE_FS,ORG_OPENIDE_NO_LOADERS});
        
    }
   
    private File createModule(String path) throws IOException {
        File dir = new File(getWorkDir(),path);
        dir.mkdirs();
        File jar = new File(dir, "tests.jar");
        jar.createNewFile();
        return dir;
    }

    private Project getProject() throws IOException {
        Project project = new Project();
        project.setBaseDir(getWorkDir());
        return project;
    }

    private void assertProperty(Project prj, String propName, String modules[]) throws IOException {
        String listModules = prj.getProperty(propName);
        assertNotNull("prop " + propName + " was not defined",listModules);
        log(" listModules " + listModules);
        String arrayModules[] = (listModules.length() == 0) ? new String[0] :listModules.split(":");
        Set<File> set1 = new HashSet<File>();
        for (int i = 0 ; i < arrayModules.length ; i++) {
            String module = arrayModules[i];
            if (module.length() == 1 && i < arrayModules.length + 1) { 
                // module is e:/dd/dd/ on windows
                module = module + ":" + arrayModules[++i];
            }
            log(i + " = " + module );
            set1.add(new File(module)); 
        }
        Set<File> set2 = new HashSet<File>();
        for (int i = 0 ; i < modules.length ; i++) {
            set2.add(new File(getWorkDir(),modules[i]));
        }
        assertEquals("paths length",set2.size(),set1.size());
        assertEquals("Different paths: ", set2,set1);
    }

    private void createModule(String path, String runcp) throws IOException {
        File dir = createModule(path);
        File props = new File(dir,"test.properties");
        PrintStream ps = new PrintStream(props);
        try { 
            ps.println("test.unit.run.cp=" + runcp);
        } finally {
            ps.close();
        }
    }
}
