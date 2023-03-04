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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.*;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author joelle
 */
public class PageFlowControllerTest extends NbTestCase {

    final String zipPath;
    private PageFlowTestUtility tu;
    PageFlowView view;
    PageFlowScene scene;
    PageFlowController controller;

    static {
        TestUtilities.setLookup(new Object[0]);
    }

    public PageFlowControllerTest(String testName) {
        super(testName);
        zipPath = PageFlowControllerTest.class.getResource("TestJSFApp.zip").getPath();
    }

//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }
//
//    public static Test suite() {
//        TestSuite suite = new NbTestSuite(PageFlowControllerTest.class);
//        return suite;
//    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tu = new PageFlowTestUtility(this);

        tu.setUp(zipPath, "TestJSFApp");

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

    public void testCheckPageFlowControllerMemorySize() {
        System.out.println("Check PageFlowController Memory Size.");
        assertSize("PageFlowController MemorySize:", 13000000, controller);
    }


    /**
     * Test of setShowNoWebFolderDialog method, of class PageFlowController.
     */
    public void testNoWebFolderDialog() {
        System.out.println("setShowNoWebFolderDialog");
        boolean show = false;
        controller.unregisterListeners();
        controller.setShowNoWebFolderDialog(false);
        assertFalse(controller.isShowNoWebFolderDialog());
        controller.setShowNoWebFolderDialog(true);
        assertTrue(controller.isShowNoWebFolderDialog());
    }

    private void waitEQ() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
    }
    private static final String JSP_EXT = "jsp";

    /**
     * Test of unregisterListeners method, of class PageFlowController.
     * 
     * This test is sort of tempermental in that if I were to test 
     * register listeners in this same way, it would only work 50% of the time.
     * Regardless, this test should always pass because page2 is always null.
     */
    public void testUnregisterListeners() throws IOException {
        System.out.println("unregisterListeners");
        final String strNewPage2 = "newPage2";
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());

        controller.unregisterListeners();
        webFolder.createData(strNewPage2, JSP_EXT);
        Page page2 = controller.getPageName2Page(strNewPage2 + "." + JSP_EXT);
        assertNull(page2);
    }

    /**
     * Test of isCurrentScope method, of class PageFlowController.
     */
    public void testIsCurrentScope() {
        System.out.println("isCurrentScope");
        controller.unregisterListeners();
        boolean result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);
        assertTrue(result);
        result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);
        assertFalse(result);
        PageFlowToolbarUtilities.getInstance(view).setCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);
        result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_PROJECT);
        assertFalse(result);
        result = controller.isCurrentScope(PageFlowToolbarUtilities.Scope.SCOPE_ALL_FACESCONFIG);
        assertTrue(result);

    }

    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePageFlowNode() throws IOException, DataObjectNotFoundException {
        System.out.println("createPageFlowNode");

        controller.unregisterListeners(); /* So it doesn't try to add the page on it's own. */

        String pageName = "newPage";
        FileObject webFolder = controller.getWebFolder();
        FileObject pageFO = webFolder.createData(pageName, JSP_EXT);

        Node node = DataObject.find(pageFO).getNodeDelegate();
        Page result = controller.createPage(node);
        assertNotNull(result);
        assertEquals(result.getNode(), node);
    }

    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePageFlowNodeNull() throws IOException, DataObjectNotFoundException {
        System.out.println("createPageFlowNode with null value");
        controller.unregisterListeners();
        boolean npeCaught = false;
        try {
            Page result = controller.createPage((Node) null);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
    }

//    /**
//     * Test of destroyPageFlowNode method, of class PageFlowController.
//     */
//    public void testDestroyPageFlowNode() {
//        System.out.println("destroyPageFlowNode");
//
//        controller.unregisterListeners(); /* to avoid reactions once the page is deleted. */
//
//        Collection<Page> pages = scene.getNodes();
//        for (Page page : pages) {
//            controller.destroyPageFlowNode(page);
//        }
//    }
    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePage() {
        System.out.println("createPage");
        final String pageName = "pageJSP";

        controller.unregisterListeners();
        Page result = controller.createPage(pageName + "." + JSP_EXT);
        assertEquals(pageName + "." + JSP_EXT, result.getName());
    }

    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePageEmptyString() {
        System.out.println("createPageEmptyString");

        controller.unregisterListeners();
        boolean aeFound = false;
        try {
            Page result = controller.createPage("");
        } catch (AssertionError ae) {
            aeFound = true;
        }
        assertTrue(aeFound);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    /**
     * Test of createPage method, of class PageFlowController.
     */
    public void testCreatePageNull() {
        System.out.println("createPageNull");

        controller.unregisterListeners();
        boolean npeCaught = false;
        try {
            Page result = controller.createPage((String) null);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
    }

    /**
     * Test of createLink method, of class PageFlowController.
     */
    public void testCreateLink() {
        System.out.println("createLink");
        String page1 = "page1.jsp";
        String page2 = "page2.jsp";

        controller.unregisterListeners();
        Page source = controller.createPage(page1);
        Page target = controller.createPage(page2);
        /* this create an NPE from being thrown in FacesModelListener 
        In order to create the link , the pages need to exist in the node.*/
        view.createNode(source, null, null);
        view.createNode(target, null, null);
        view.validateGraph();

        Pin pinNode = null;
        NavigationCase result = controller.createLink(source, target, pinNode);

        assertEquals(result.getToViewId(), "/" + target.getName());
        assert (result.getParent() instanceof NavigationRule);
        NavigationRule resultRule = (NavigationRule) result.getParent();
        assertEquals(resultRule.getFromViewId(), "/" + source.getName());
        assertEquals(result.getFromOutcome(), "case1");
    }

    /**
     * Test of createLink method, of class PageFlowController.
     */
    public void testCreateLinkWithNullValues() {
        System.out.println("createLink with null values");
        boolean npeCaught = false;
        String page1 = "page1.jsp";
        String page2 = "page2.jsp";

        controller.unregisterListeners();
        Page source = controller.createPage(page1);
        Page target = controller.createPage(page2);
        Pin pinNode = null;

        try {
            NavigationCase result = controller.createLink(null, target, pinNode);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        npeCaught = false;

        try {
            NavigationCase result = controller.createLink(source, null, pinNode);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        npeCaught = false;

        try {
            NavigationCase result = controller.createLink(source, null, pinNode);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);

    }
//
//    /**
//     * Test of updatePageItems method, of class PageFlowController.
//     */
//    public void testUpdatePageItems() {
//        System.out.println("updatePageItems");
//        Page pageNode = null;
//        PageFlowController instance = null;
//        instance.updatePageItems(pageNode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of isKnownFile method, of class PageFlowController.
     */
    public void testIsKnownFile() throws IOException {
        System.out.println("isKnownFile");


        controller.unregisterListeners();
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        String strNewPage = "newPage";
        FileObject newFO = webFolder.createData(strNewPage, "jsp");
        boolean result = controller.isKnownFile(newFO);
        assertTrue(result);
    }

    public void testIsKnownFileXML() throws IOException {

        controller.unregisterListeners();
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        FileObject webINFFile = webFolder.getFileObject("WEB-INF");
        String strNewPage = "newPage";
        FileObject newFO = webINFFile.createData(strNewPage, "xml");
        boolean result = controller.isKnownFile(newFO);
        assertFalse(result);
    }

    public void testIsKnownFileNull() {
        boolean npeFound = false;

        controller.unregisterListeners();
        try {
            boolean result = controller.isKnownFile(null);
        } catch (NullPointerException npe) {
            npeFound = true;
        }
        assertTrue(npeFound);
    }

//     /**
//      * Test if getting the webFolder works properly
//      */
//     public void testGetWebFolder() {
//         System.out.println("CONTROLLER: getWebFolder() ->" + controller.getWebFolder());
//         System.out.println("PageFlowView: getWebFolder() ->" + PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile()));
//         assertEquals(controller.getWebFolder(),PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile()));
//     }
    /**
     * Test of isKnownFolder method, of class PageFlowController.
     */
    public void testIsKnownFolder() throws IOException {
        System.out.println("isKnownFolder");


        controller.unregisterListeners();
        FileObject webFolder = controller.getWebFolder();
        FileObject testFolder = webFolder.createFolder("testFolder");
        boolean result1 = controller.isKnownFolder(webFolder);
        assertTrue(result1);
        boolean result2 = controller.isKnownFolder(testFolder);
        assertTrue(result2);
    }

    /**
     * Test of isKnownFolder method, of class PageFlowController.
     */
    public void testIsKnownFolderWeBINF() {
        System.out.println("isKnownFolder");


        controller.unregisterListeners();
        FileObject webFolder = PageFlowView.getWebFolder(tu.getJsfDO().getPrimaryFile());
        FileObject webINFFolder = webFolder.getFileObject("WEB-INF");
        boolean result = controller.isKnownFolder(webINFFolder);
        assertFalse(result);
    }

    /**
     * Test of createEdge method, of class PageFlowController.
     */
    public void testCreateEdge() {
        System.out.println("createEdge");

        controller.unregisterListeners();

        String page1 = "page1.jsp";
        String page2 = "page2.jsp";
        Page source = controller.createPage(page1);
        Page target = controller.createPage(page2);

        view.createNode(source, null, null);
        view.createNode(target, null, null);

        NavigationCase navCase = controller.createLink(source, target, null);
        NavigationCaseEdge newCaseEdge = new NavigationCaseEdge(view.getPageFlowController(), navCase);
        controller.createEdge(newCaseEdge);
        assertTrue(scene.getEdges().contains(newCaseEdge));
    }

    /**
     * Test of createEdge method, of class PageFlowController.
     */
    public void testCreateEdgeNotNull() {
        System.out.println("createEdge with null value");
        boolean npeCaught = false;

        controller.unregisterListeners();
        try {
            controller.createEdge(null);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
    }

    /**
     * Test of removePageName2Page method, of class PageFlowController.
     */
    public void testRemovePageName2Page() {
        System.out.println("removePageName2Page with false for perm destroy");
        String jspPageName = "welcomeJSF.jsp";

        controller.unregisterListeners();
        FileObject webFolder = controller.getWebFolder();
        FileObject welcomeFile = webFolder.getFileObject(jspPageName);
        assertNotNull(welcomeFile);
        Page page = controller.getPageName2Page(jspPageName);
        assertNotNull(page);
        Page result = controller.removePageName2Page(page, false);
        assertEquals(result, page);
        Page result2 = controller.getPageName2Page(jspPageName);
        assertNull(result2);
    }

    /**
     * Test of removePageName2Page method, of class PageFlowController.
     */
    public void testRemovePageName2PageTrue() {
        System.out.println("removePageName2Page with true for perm destroy");
        String jspPageName = "welcomeJSF.jsp";

        controller.unregisterListeners();
        FileObject webFolder = controller.getWebFolder();
        FileObject welcomeFile = webFolder.getFileObject(jspPageName);
        assertNotNull(welcomeFile);
        Page page = controller.getPageName2Page(jspPageName);
        assertNotNull(page);
        Page result = controller.removePageName2Page(page, true);
        assertEquals(result, page);
        Page result2 = controller.getPageName2Page(jspPageName);
        assertNull(result2);
    }

    /**
     * Test of removePageName2Page method, of class PageFlowController.
     */
    public void testRemovePageName2PageNull() {
        System.out.println("removePageName2Page with null with false for perm destroy");

        controller.unregisterListeners();
        boolean npeCaught = false;
        try {
            Page result = controller.removePageName2Page((Page) null, false);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
    }

    /**
     * Test of removePageName2Page method, of class PageFlowController.
     */
    public void testRemovePageName2PageNullTrue() {
        System.out.println("removePageName2Page with null with true for perm destroy");

        controller.unregisterListeners();
        boolean npeCaught = false;
        try {
            Page result = controller.removePageName2Page((Page) null, true);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
    }

    /**
     * Test of replacePageName2Page method, of class PageFlowController.
     */
    public void testReplacePageName2Page() {
        System.out.println("replacePageName2Page");


        String oldName = "welcomeJSF.jsp";
        String newName = "welcomeJSF2.jsp";
        Page page = controller.getPageName2Page(oldName);
        Page pageNon = controller.getPageName2Page(newName);
        assertNotNull(page);
        assertNull(pageNon);
        boolean result = controller.replacePageName2Page(page, newName, oldName);
        assertTrue(result);
        Page page2 = controller.getPageName2Page(newName);
        Page page3 = controller.getPageName2Page(oldName);
        assertNotNull(page2);
        assertNull(page3);
    }

    /**
     * Test of replacePageName2Page method, of class PageFlowController.
     */
    public void testReplacePageName2PageNull() {
        System.out.println("replacePageName2Page when null page");


        String oldName = "welcomeJSF.jsp";
        String newName = "welcomeJSF2.jsp";
        boolean npeCaught = false;
        try {
            boolean result = controller.replacePageName2Page(null, newName, oldName);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        assertNotNull(controller.getPageName2Page(oldName)); //Confirming it wasn't removed.
    }

    /**
     * Test of replacePageName2Page method, of class PageFlowController.
     */
    public void testReplacePageName2PageEmptyString() {
        System.out.println("replacePageName2Page when new name or old names are empty string.");

        String oldName = "welcomeJSF.jsp";
        String newName = "welcomeJSF2.jsp";
        boolean npeCaught = false;
        try {
            boolean result = controller.replacePageName2Page(null, "", oldName);
        } catch (NullPointerException error) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        assertNotNull(controller.getPageName2Page(oldName)); //Confirming it wasn't removed.

        npeCaught = false;
        try {
            boolean result = controller.replacePageName2Page(null, newName, "");
        } catch (NullPointerException error) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        assertNotNull(controller.getPageName2Page(oldName)); //Confirming it wasn't removed.
    }

    /**
     * Test of replacePageName2Page method, of class PageFlowController.
     */
    public void testReplacePageName2PageWhenOldPageNonExsitent() {
        System.out.println("replacePageName2Page when oldpage name did not exist.");


        String oldName = "welcomeJSF.jsp";
        String nonExistantName = "welcomeJSF2.jsp";
        String newName = "welcomeJSF3.jsp";

        Page page = controller.getPageName2Page(oldName);
        assertNotNull(page);

        Page pageNon = controller.getPageName2Page(newName);
        Page pageNon2 = controller.getPageName2Page(nonExistantName);
        assertNull(pageNon);
        assertNull(pageNon2);

        boolean result = controller.replacePageName2Page(page, newName, nonExistantName);
        assertFalse(result);
        Page page2 = controller.getPageName2Page(newName);
        Page page3 = controller.getPageName2Page(oldName);
        assertNull(page2);
        assertNotNull(page3);
    }

    /**
     * Test of clearPageName2Page method, of class PageFlowController.
     */
    public void testClearPageName2Page() {
        System.out.println("clearPageName2Page");
        String oldName = "welcomeJSF.jsp";
        Page page = controller.getPageName2Page(oldName);
        assertNotNull(page);
        controller.clearPageName2Page();
        Page page2 = controller.getPageName2Page(oldName);
        assertNull(page2);
    }

    /**
     * Test of putPageName2Page method, of class PageFlowController.
     */
    public void testPutPageName2Page() {
        System.out.println("putPageName2Page");
        String pageName = "SomeName.jsp";

        Page page = controller.createPage(pageName);
        assertNotNull(page);
        controller.putPageName2Page(pageName, page);
        Page result = controller.getPageName2Page(pageName);
        assertEquals(page, result);
    }

    /**
     * Test of putPageName2Page method, of class PageFlowController.
     */
    public void testPutPageName2PageNull() {
        System.out.println("putPageName2Page when page is null");
        boolean npeCaught = false;
        String someName = "somePage.jsp";
        try {
            controller.putPageName2Page(someName, (Page) null);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
        assertNull(controller.getPageName2Page(someName));
    }

    /**
     * Test of putPageName2Page method, of class PageFlowController.
     */
    public void testPutPageName2PageEmptyString() {
        System.out.println("putPageName2Page when name is empty string");

        String pageName = "SomeName.jsp";

        Page page = controller.createPage(pageName);
        assertNotNull(page);
        controller.putPageName2Page("", page);
        Page page2 = controller.getPageName2Page("");
        assertEquals(page2, page);
    }

    /**
     * Test of getPageName2Page method, of class PageFlowController.
     */
    public void testGetPageName2Page() {
        System.out.println("getPageName2Page");
        String displayName = "welcomeJSF.jsp";
        Page result = controller.getPageName2Page(displayName);
        assertNotNull(result);
    }

    /**
     * Test of getPageName2Page method, of class PageFlowController.
     */
    public void testGetPageName2PageNull() {
        System.out.println("getPageName2Page given a null string");
        boolean npeCaught = false;
        try {
            controller.getPageName2Page(null);
        } catch (NullPointerException npe) {
            npeCaught = true;
        }
        assertTrue(npeCaught);
    }

    /**
     * Test of getPageName2Page method, of class PageFlowController.
     */
    public void testGetPageName2PageEmptyString() {
        System.out.println("getPageName2Page given an empty string");
        Page page = controller.getPageName2Page("");
        assertNull(page);
    }

//    public void testReleaseGraphInfoMemoryLeakFinder() {
//        System.out.println("releaseGraphInfo - Looking for memory leaks");
//        Set<PageFlowSceneElement> elements = (Set<PageFlowSceneElement>) scene.getObjects();
//        Collection<WeakReference<PageFlowSceneElement>> references = new ArrayList<WeakReference<PageFlowSceneElement>>();
//        for (PageFlowSceneElement element : elements) {
//            references.add(new WeakReference<PageFlowSceneElement>(element));
//        }
//        elements = null;
//
//        controller.releaseGraphInfo();
//        assert SwingUtilities.isEventDispatchThread();
//        assertTrue(TopComponent.getRegistry().getActivated().close());
//
//        for (WeakReference<PageFlowSceneElement> ref : references) {
//            assertGC("PageFlowElement should be GC'ed", ref);
//        }
//
////        Chain chain = scene.getActions();
////        List<WidgetAction> actions = chain.getActions();
////        for( WidgetAction action : actions ){
////            if ( action instanceof MyActionMapAction) {
////                assertGC("Actions should all be null as well." + action, new WeakReference(action));
////            }
////        }
//
//    }

    public void testsetupGraphMemoryLeakFinder() {
        System.out.println("setupGraph - Looking for memory leaks");

        final Collection<WeakReference<PageFlowSceneElement>> references = new ArrayList<WeakReference<PageFlowSceneElement>>();

        Set<PageFlowSceneElement> elements = (Set<PageFlowSceneElement>) scene.getObjects();
        for (PageFlowSceneElement element : elements) {
            references.add(new WeakReference<PageFlowSceneElement>(element));
        }
        elements = null;

        Collection<? extends PageContentModelProvider> providers = controller.getPageContentModelProviders();

        controller.setupGraph();

        Set<PageFlowSceneElement> elements2 = (Set<PageFlowSceneElement>) scene.getObjects();
        for (PageFlowSceneElement element : elements2) {
            references.add(new WeakReference<PageFlowSceneElement>(element));
        }
        elements = null;

        controller.setupGraph();

        for (WeakReference<PageFlowSceneElement> ref : references) {
            assertGC("PageFlowElement should be GC'ed", ref);
        }


    }

    public void testSetupGraphMemorySize20() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            controller.setupGraph();
        }
        /* This number could 10MB if I could ensure garbage collection. */
        assertSize("setupGraph MemorySize after 20 setups:", 20000000, controller);
        try {
            assertGC("Force Garbage collection on controller.", new WeakReference(controller));
        } catch (AssertionError ae) {
            assertSize("seuptGraph MemorySize after force garbage collect", 10000000, controller);
        }
    }
//    
//    public void testCheckPageFlowControllerMemorySizeEndOfTests(){
//        System.out.println("Check PageFlowController Memory Size.");
//        assertSize("PageFlowController MemorySize:", 7000000, controller);
//    }
//    /**
//     * Test of renamePageInModel method, of class PageFlowController.
//     */
//    public void testRenamePageInModel() {
//        System.out.println("renamePageInModel");
//        String oldDisplayName = "welcomeJSF.jsp";
//        String newDisplayName = "newName";
//        controller.renamePageInModel(oldDisplayName, newDisplayName);
//        tu.getFacesConfig().getNavigationRules();
//    }
//
//    /**
//     * Test of removeSceneNodeEdges method, of class PageFlowController.
//     */
//    public void testRemoveSceneNodeEdges() {
//        System.out.println("removeSceneNodeEdges");
//        Page pageNode = null;
//        PageFlowController instance = null;
//        instance.removeSceneNodeEdges(pageNode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removePageInModel method, of class PageFlowController.
//     */
//    public void testRemovePageInModel() {
//        System.out.println("removePageInModel");
//        String displayName = "";
//        PageFlowController instance = null;
//        instance.removePageInModel(displayName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getWebFolder method, of class PageFlowController.
//     */
//    public void testGetWebFolder() {
//        System.out.println("getWebFolder");
//        PageFlowController instance = null;
//        FileObject expResult = null;
//        FileObject result = instance.getWebFolder();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isPageInAnyFacesConfig method, of class PageFlowController.
//     */
//    public void testIsPageInAnyFacesConfig() {
//        System.out.println("isPageInAnyFacesConfig");
//        String name = "";
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.isPageInAnyFacesConfig(name);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isNavCaseInFacesConfig method, of class PageFlowController.
//     */
//    public void testIsNavCaseInFacesConfig() {
//        System.out.println("isNavCaseInFacesConfig");
//        NavigationCaseEdge navEdge = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.isNavCaseInFacesConfig(navEdge);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of changeToAbstractNode method, of class PageFlowController.
//     */
//    public void testChangeToAbstractNode() {
//        System.out.println("changeToAbstractNode");
//        Page oldNode = null;
//        String displayName = "";
//        PageFlowController instance = null;
//        instance.changeToAbstractNode(oldNode, displayName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getConfigDataObject method, of class PageFlowController.
//     */
//    public void testGetConfigDataObject() {
//        System.out.println("getConfigDataObject");
//        PageFlowController instance = null;
//        DataObject expResult = null;
//        DataObject result = instance.getConfigDataObject();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveLocation method, of class PageFlowController.
//     */
//    public void testSaveLocation() {
//        System.out.println("saveLocation");
//        String oldDisplayName = "";
//        String newDisplayName = "";
//        PageFlowController instance = null;
//        instance.saveLocation(oldDisplayName, newDisplayName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeWebFile method, of class PageFlowController.
//     */
//    public void testRemoveWebFile() {
//        System.out.println("removeWebFile");
//        FileObject fileObj = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.removeWebFile(fileObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addWebFile method, of class PageFlowController.
//     */
//    public void testAddWebFile() {
//        System.out.println("addWebFile");
//        FileObject fileObj = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.addWebFile(fileObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of containsWebFile method, of class PageFlowController.
//     */
//    public void testContainsWebFile() {
//        System.out.println("containsWebFile");
//        FileObject fileObj = null;
//        PageFlowController instance = null;
//        boolean expResult = false;
//        boolean result = instance.containsWebFile(fileObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of putNavCase2NavCaseEdge method, of class PageFlowController.
//     */
//    public void testPutNavCase2NavCaseEdge() {
//        System.out.println("putNavCase2NavCaseEdge");
//        NavigationCase navCase = null;
//        NavigationCaseEdge navCaseEdge = null;
//        PageFlowController instance = null;
//        instance.putNavCase2NavCaseEdge(navCase, navCaseEdge);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNavCase2NavCaseEdge method, of class PageFlowController.
//     */
//    public void testGetNavCase2NavCaseEdge() {
//        System.out.println("getNavCase2NavCaseEdge");
//        NavigationCase navCase = null;
//        PageFlowController instance = null;
//        NavigationCaseEdge expResult = null;
//        NavigationCaseEdge result = instance.getNavCase2NavCaseEdge(navCase);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeNavCase2NavCaseEdge method, of class PageFlowController.
//     */
//    public void testRemoveNavCase2NavCaseEdge() {
//        System.out.println("removeNavCase2NavCaseEdge");
//        NavigationCase navCase = null;
//        PageFlowController instance = null;
//        NavigationCaseEdge expResult = null;
//        NavigationCaseEdge result = instance.removeNavCase2NavCaseEdge(navCase);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeNavRule2String method, of class PageFlowController.
//     */
//    public void testRemoveNavRule2String() {
//        System.out.println("removeNavRule2String");
//        NavigationRule navRule = null;
//        PageFlowController instance = null;
//        String expResult = "";
//        String result = instance.removeNavRule2String(navRule);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of putNavRule2String method, of class PageFlowController.
//     */
//    public void testPutNavRule2String() {
//        System.out.println("putNavRule2String");
//        NavigationRule navRule = null;
//        String navRuleName = "";
//        PageFlowController instance = null;
//        String expResult = "";
//        String result = instance.putNavRule2String(navRule, navRuleName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getView method, of class PageFlowController.
//     */
//    public void testGetView() {
//        System.out.println("getView");
//        PageFlowController instance = null;
//        PageFlowView expResult = null;
//        PageFlowView result = instance.getView();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setModelNavigationCaseName method, of class PageFlowController.
//     */
//    public void testSetModelNavigationCaseName() {
//        System.out.println("setModelNavigationCaseName");
//        NavigationCase navCase = null;
//        String newName = "";
//        PageFlowController instance = null;
//        instance.setModelNavigationCaseName(navCase, newName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeModelNavigationCase method, of class PageFlowController.
//     */
//    public void testRemoveModelNavigationCase() throws Exception {
//        System.out.println("removeModelNavigationCase");
//        NavigationCase navCase = null;
//        PageFlowController instance = null;
//        instance.removeModelNavigationCase(navCase);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of serializeNodeLocations method, of class PageFlowController.
//     */
//    public void testSerializeNodeLocations() {
//        System.out.println("serializeNodeLocations");
//        PageFlowController instance = null;
//        instance.serializeNodeLocations();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of openNavigationCase method, of class PageFlowController.
//     */
//    public void testOpenNavigationCase() {
//        System.out.println("openNavigationCase");
//        NavigationCaseEdge navCaseEdge = null;
//        PageFlowController instance = null;
//        instance.openNavigationCase(navCaseEdge);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageContentModelProviders method, of class PageFlowController.
//     */
//    public void testGetPageContentModelProviders() {
//        System.out.println("getPageContentModelProviders");
//        Collection<? extends PageContentModelProvider> expResult = null;
//        Collection<? extends PageContentModelProvider> result = PageFlowController.getPageContentModelProviders();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
