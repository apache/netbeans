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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.filesystems.FileChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author joelle
 */
public class PageFlowControllerPerformanceTest extends NbTestCase implements TestServices {

    final String zipPath;
    private PageFlowTestUtility tu;
    PageFlowView view;
    PageFlowScene scene;
    PageFlowController controller;

    public PageFlowControllerPerformanceTest(String testName) {
        super(testName);
        zipPath = PageFlowControllerPerformanceTest.class.getResource("TestJSFApp.zip").getPath();
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(PageFlowControllerPerformanceTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tu = new PageFlowTestUtility(this);
        final PageFlowTestUtility pftu = tu;

        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                try {
                    pftu.setUp(zipPath, "TestJSFApp");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });


        importantValuesNotNull();
    }

    public void importantValuesNotNull() throws InterruptedException {

        assertNotNull(tu.getProject());
        assertNotNull(tu.getJsfDO());
        assertNotNull(tu.getFacesConfig());
        assertNotNull(view = tu.getPageFlowView());
        assertNotNull(controller = tu.getController());
        assertNotNull(scene = tu.getScene());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        tu.tearDown();
        tu = null;
        view = null;
        scene = null;
        controller = null;
    }

    public void setupServices() {

        ClassLoader l = this.getClass().getClassLoader();
        MockLookup.setLookup(Lookups.fixed(l), Lookups.metaInfServices(l));
    }

    public void testCheckPageFlowControllerMemorySize() {
        System.out.println("Check PageFlowController Memory Size.");
        assertTrue(true);
    }


//    /* This test is not ready but will be valueable when it is. */
//    public void testCloseProjectMemoryLeakFinder() throws IOException, InvocationTargetException, InterruptedException {
//        System.out.println("Close Project Memory Leak Finder");
//        //tu = new PageFlowTestUtility(this);
//        final PageFlowTestUtility pftu = tu;
//        WeakReference<FileChangeListener> refFCL = new WeakReference<FileChangeListener>(controller.getFCL());
//        WeakReference<PageFlowView> refView = new WeakReference<PageFlowView>(view);
//        WeakReference<PageFlowController> refController = new WeakReference<PageFlowController>(controller);
//        WeakReference<PageFlowScene> refScene = new WeakReference<PageFlowScene>(scene);
//
//        controller.unregisterListeners();
//        EventQueue.invokeAndWait(new Runnable() {
//
//            public void run() {
//                try {
//                    pftu.closeProject();
//                    Set<PageFlowView> views = PageFlowToolbarUtilities.getViews();
//                    for (PageFlowView view : views) {
//                        view.close();
//                    }
//
//                    assertTrue(true);
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//            });
//
//
//
//        controller = null;
//        scene = null;
//        view = null;
//        for (int i = 0; i < 10; i++) {
//            EventQueue.invokeAndWait(new Runnable() {
//
//                public void run() {
//                    assertTrue(true);
//                }
//            });
//        }
//
//        assertGC("FileChangeListener should no longer be reference.", refFCL);
//        assertGC("PageFlowView should now no longer exist.", refView);
//        assertGC("Controller should now no longer exist.", refController);
//        assertGC("Scene should now no longer exist.", refScene);
//    }
}
