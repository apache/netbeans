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

package org.netbeans.modules.web.jsf.navigation;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import junit.framework.*;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.junit.*;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.navigation.graph.PFObjectSceneListener;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneAccessor;
import org.netbeans.modules.web.jsf.navigation.graph.layout.FreePlaceNodesLayouter;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author joelle
 */
public class PageFlowViewTestPostPone extends NbTestCase implements TestServices {

    final String zipPath;
    private PageFlowTestUtility tu;

    public PageFlowViewTestPostPone(String testName) {
        super(testName);
        zipPath = PageFlowViewTestPostPone.class.getResource("TestJSFApp.zip").getPath();

    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(PageFlowViewTestPostPone.class);
        return suite;
    }

    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tu = new PageFlowTestUtility(this);

        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

                    public void run()  {
                try {
                    tu.setUp(zipPath, "TestJSFApp");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    fail();
                }
                    }
                });

        importantValuesNotNull();
        waitEQ();
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
    PageFlowView view;
    PageFlowScene scene;
    PageFlowController controller;

    public void importantValuesNotNull() throws InterruptedException {

        assertNotNull(tu.getProject());
        assertNotNull(tu.getJsfDO());
        assertNotNull(tu.getFacesConfig());
        assertNotNull(view = tu.getPageFlowView());
        assertNotNull(controller = tu.getController());
        assertNotNull(scene = tu.getScene());
    }

    public void testAddLink() throws InterruptedException, Exception {
        Page firstPage = controller.createPage("FirstPage");
        Page secondPage = controller.createPage("SecondPage");

        view.createNode(firstPage, null, null);
        view.createNode(secondPage, null, null);
        int i_edgeNum = scene.getEdges().size();


        NavigationCase navCase = controller.createLink(firstPage, secondPage, null);
        /* First Make sure it can be found in the faces Configuration */
        FacesConfig facesConfig = tu.getFacesConfig();
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        boolean foundCase = false;
        for (NavigationRule rule : rules) {
            if (rule.getNavigationCases().contains(navCase)) {
                foundCase = true;
            }
        }
        assertTrue(foundCase);
        waitEQ();

        int e_edgeNum = scene.getEdges().size();
        assertTrue(i_edgeNum + 1 == e_edgeNum);

    }

    public void testPageSetName() throws InterruptedException {
        Page firstPage = controller.createPage("FirstPage");
        Page secondPage = controller.createPage("SecondPage");
        view.createNode(firstPage, null, null);
        view.createNode(secondPage, null, null);

        NavigationCase navCase = controller.createLink(secondPage, firstPage, null);
        firstPage.setName("NewName");
        Collection<Page> pages = scene.getNodes();
        boolean found = false;
        for (Page page : pages) {
            if (page.getName().equals("NewName")) {
                /* It is weird that page.getDisplayName is not "NewName"... 
               * This may have worked more appropriately if I used the 
               * InlineTextEditor which probably sets the DisplayName. 
               * Not sure though */
                found = true;
                break;
            }

        }
        assertTrue(found);
    }

    public void testInitialPageCount() throws InterruptedException {
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        int fileInWebFolderNum = webFolder.getChildren().length - 1; //Remove WEB-INF
        int dataNodePages = 0;


        Collection<Page> pages = scene.getNodes();
        for (Page page : pages) {
            if (page.isDataNode()) {
                dataNodePages++;
            }
        }
        assertTrue(dataNodePages == fileInWebFolderNum);
        pages = scene.getNodes(); /* doing this see if I can work around some inconsistencies */
        assertTrue(pages.size() == 2);
    }

    public void testInitialCaseCount() {
        Collection<NavigationCaseEdge> edges = scene.getEdges();
        assertTrue(edges.size() == 1);
    }

    public void testAdd2Pages() throws InterruptedException {
        String strFirstPage = "FirstPage";
        String strSecondPage = "SecondPage";

        Page firstPage = controller.createPage(strFirstPage);
        Page secondPage = controller.createPage(strSecondPage);

        view.createNode(firstPage, null, null);
        view.createNode(secondPage, null, null);

        Collection<Page> pages = scene.getNodes();
        assertTrue(pages.contains(firstPage));
        assertTrue(pages.contains(secondPage));

    }


    /* Unit Test 1 for Bug: 120355 */
    public void testSimiluateDeletePageThroughExplorer1() throws InterruptedException, IOException {
        String strWelComeFile = "welcomeJSF";
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        FileObject jspFile = webFolder.getFileObject(strWelComeFile, "jsp");
        jspFile.delete();


        boolean found = false;
        for (Page page : scene.getNodes()) {
            if (page.getName().startsWith(strWelComeFile)) {
                found = true;
                assertFalse(page.isDataNode());
            }
        }
        assertTrue(found);
    }

    /* Unit Test 2 for Bug: 120355 */
    public void testSimiluateAddPageThroughExplorer2() throws InterruptedException, IOException {
        String strNewPage = "newPage";
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        boolean found = false;
        webFolder.createData(strNewPage, "jsp");


        for (Page page : scene.getNodes()) {
            if (page.getName().startsWith(strNewPage)) {
                found = true;
                break;
            }
        }
        assertTrue(found);

    }

    public void testMemoryLeaksOnScene() throws InterruptedException, IOException {
        PopupMenuProvider pnp = PageFlowSceneAccessor.getPopupMenuProvider(scene);
        FreePlaceNodesLayouter fpnl = PageFlowSceneAccessor.getFreePlaceNodesLayouter(scene);
        PFObjectSceneListener pfosl = PageFlowSceneAccessor.getPfObjectSceneListener(scene);


        WeakReference refPnp = new WeakReference(pnp);
        WeakReference refFpnl = new WeakReference(fpnl);
        WeakReference refPfosl = new WeakReference(pfosl);

        tu.closeFacesModel();


        pnp = null;
        fpnl = null;
        pfosl = null;
        assertGC("PopupMenuProvider should no longer exist.", refPnp);
        assertGC("FreePlaceNodesLayouter should no longer exist.", refFpnl);
        assertGC("PFObjectSceneListener should no longer exist.", refPfosl);

    }


    /* This Test Currently Fails Do To Memory Leak 
    public void testMemoryLeakPageOnScene() throws InterruptedException {
        Thread.sleep(3000);
        PageFlowScene scene = PageFlowView.PFVTestAccessor.getPageFlowScene(tu.getPageFlowView());
        
        Collection<Page> pages = scene.getNodes();
        Collection<WeakReference> pageRefs = new ArrayList<WeakReference>();
        for ( Page page : pages ){
            pageRefs.add(new WeakReference(page));
        }
        
        tu.closeFacesModel();
        
        for(Page page : pages ){
            page = null;
        }
        for ( WeakReference pageRef : pageRefs ) {
            assertGC("Page has not be garbage collected.", pageRef);
        }
        
    }*/
    private void waitEQ() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                    }
                });
    }

    public void setupServices() {

        ClassLoader l = this.getClass().getClassLoader();
        MockLookup.setLookup(Lookups.fixed(l), Lookups.metaInfServices(l));
    }
}
