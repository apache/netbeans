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

package org.netbeans.modules.web.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Radko Najman
 * @author ads 
 */
public class WebInjectionTargetQueryImplementationTest extends JavaSourceTestCase {
    
    private FileObject ordinaryClass;
    private FileObject fileSubclass;
    private FileObject directServletSubclass;
    private FileObject secondLevelServletSubclass;

    public WebInjectionTargetQueryImplementationTest(String testName) {
        super(testName);
    }
    
    public void testIsInjectionTargetJee14() throws Exception  {
        System.out.println("isInjectionTarget for JEE 1.4 profile");
        
        isInjectionTarget( Profile.J2EE_14, false);
    }
    
    public void testIsInjectionTargetJee5() throws Exception {
        System.out.println("isInjectionTarget for JEE 5 profile");
        
        isInjectionTarget( Profile.JAVA_EE_5, true);
    }
    
    private void isInjectionTarget( Profile profile , boolean jee5Profile ) throws Exception {
        final WebInjectionTargetQueryImplementation instance = new WebInjectionTargetQueryImplementation();
        
        createClasses();
        TestWebModuleImplementation.getInstance().setJeeProfile( profile);
        final List<String> source = new ArrayList<String>(1);
        final boolean[] result = {false};  
        
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement thisTypeEl = controller.getElements().getTypeElement(source.get(0));
                result[0] = instance.isInjectionTarget(controller, thisTypeEl);
            }
            public void cancel() {}
        };
        
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.toURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        
        JavaSource javaSrc;
        ordinaryClass = srcFO.getFileObject("org/test/NewClass.java");
        source.add("org.test.NewClass");
        javaSrc = JavaSource.create(cpi, ordinaryClass);
        javaSrc.runUserActionTask(task, true);
        assertEquals(false, result[0]);
        
        fileSubclass = srcFO.getFileObject("org/test/FileSubclass.java");
        source.clear();
        source.add("org.test.FileSubclass");
        javaSrc = JavaSource.create(cpi, fileSubclass);
        javaSrc.runUserActionTask(task, true);
        assertEquals(false, result[0]);
        
        directServletSubclass = srcFO.getFileObject("org/test/NewServlet.java");
        source.clear();
        source.add("org.test.NewServlet");
        javaSrc = JavaSource.create(cpi, directServletSubclass);
        javaSrc.runUserActionTask(task, true);
        assertEquals(jee5Profile, result[0]);
        
        secondLevelServletSubclass = srcFO.getFileObject("org/test/NewServletSubclass.java");
        source.clear();
        source.add("org.test.NewServletSubclass");
        javaSrc = JavaSource.create(cpi, secondLevelServletSubclass);
        javaSrc.runUserActionTask(task, true);
        assertEquals(jee5Profile, result[0]);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtilities.copyStringToFileObject(srcFO, 
                "javax/servlet/Servlet.java",
                "package javax.servlet; " +
                "public interface Servlet  {" +
                " void destroy(); "+
                " String getServletInfo(); "+
                "} ");
        
        TestUtilities.copyStringToFileObject(srcFO, 
                "javax/servlet/http/HttpServlet.java",
                "package javax.servlet.http; " +
                "public class HttpServlet  implements javax.servlet.Servlet{" +
                " public void destroy() {}  "+
                " public String getServletInfo() { return null;} "+
                "} ");
        
    }

    @Override
    protected void tearDown()  {
        ordinaryClass = null;
        fileSubclass = null;
        directServletSubclass = null;
        secondLevelServletSubclass = null;
        
        super.tearDown();
    }
    
    private void createClasses() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, 
                "org/test/FileSubclass.java",
                "package org.test; " +
                "public class FileSubclass extends java.io.File {" +
                " public FileSubclass(String pathname) { "+
                "super(pathname); "+
                "} } ");
        
        TestUtilities.copyStringToFileObject(srcFO, 
                "org/test/NewClass.java",
                "package org.test; " +
                "public class NewClass {" +
                " } ");
        

        TestUtilities.copyStringToFileObject(srcFO, 
                "org/test/NewServlet.java",
                "package org.test; " +
                "public class NewServlet extends  javax.servlet.http.HttpServlet {" +
                " } ");
        
        TestUtilities.copyStringToFileObject(srcFO, 
                "org/test/NewServletSubclass.java",
                "package org.test; " +
                "public class NewServletSubclass extends  NewServlet {" +
                " } ");
    }
}
