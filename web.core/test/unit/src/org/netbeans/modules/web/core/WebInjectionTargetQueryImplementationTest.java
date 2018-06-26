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

package org.netbeans.modules.web.core;

import java.io.File;
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
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;



import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.junit.NbTestCase;

import org.netbeans.modules.web.core.test.TestUtil;

/**
 *
 * @author Radko Najman
 * @author ads 
 */
public class WebInjectionTargetQueryImplementationTest extends JavaSourceTestCase {
    
    private String serverID;
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
        
        CancellableTask task = new CancellableTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement thisTypeEl = controller.getElements().getTypeElement(source.get(0));
                result[0] = instance.isInjectionTarget(controller, thisTypeEl);
            }
            public void cancel() {}
        };
        
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
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

    protected void tearDown()  {
        serverID = null;
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
