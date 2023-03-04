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
