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
package org.netbeans.modules.php.zend2.util;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class Zend2UtilsTest extends NbTestCase {

    private FileObject zf2project = null;


    public Zend2UtilsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        zf2project = FileUtil.toFileObject(new File(getDataDir(), "zf2project"));
        assertNotNull(zf2project);
    }

    public void testIsView() {
        FileObject deleteView = zf2project.getFileObject("module/Album/view/album/album/delete.phtml");
        assertNotNull(deleteView);
        assertTrue(Zend2Utils.isView(FileUtil.toFile(deleteView)));

        FileObject helloWorldView = zf2project.getFileObject("module/Album/view/album/my-test/hello-world.phtml");
        assertNotNull(helloWorldView);
        assertTrue(Zend2Utils.isView(FileUtil.toFile(helloWorldView)));

        FileObject noControllerView = zf2project.getFileObject("module/Album/view/album/no-controller/index.phtml");
        assertNotNull(noControllerView);
        assertTrue(Zend2Utils.isView(FileUtil.toFile(noControllerView)));
    }

    public void testIsNotView() {
        File project = FileUtil.toFile(zf2project);
        assertNotNull(project);

        assertFalse(Zend2Utils.isView(null));

        File notView = new File(project, "module/Album/view/album/my-test/non-existing-file.phtml");
        assertFalse(notView.exists());
        assertFalse(Zend2Utils.isView(notView));

        notView = new File(project, "module/Album/view/album/my-test/not-view.html");
        assertTrue(notView.exists());
        assertFalse(Zend2Utils.isView(notView));

        notView = new File(project, "module/Album/view/album/my-test/not-view.php");
        assertTrue(notView.exists());
        assertFalse(Zend2Utils.isView(notView));

        notView = new File(project, "module/Album/view/album/my-test/dummy/not-view.phtml");
        assertTrue(notView.exists());
        assertFalse(Zend2Utils.isView(notView));

        notView = new File(project, "module/Album/view/album/not-view.phtml");
        assertTrue(notView.exists());
        assertFalse(Zend2Utils.isView(notView));

        notView = new File(project, "module/Album/view/not-view.phtml");
        assertTrue(notView.exists());
        assertFalse(Zend2Utils.isView(notView));
    }

    public void testView() {
        PhpClass albumControllerClass = new PhpClass("AlbumController", "Album\\Controller\\AlbumController");
        albumControllerClass.addMethod("deleteAction", null);
        FileObject albumController = zf2project.getFileObject("module/Album/src/Album/Controller/AlbumController.php");
        FileObject deleteView = zf2project.getFileObject("module/Album/view/album/album/delete.phtml");
        assertEquals(FileUtil.toFile(deleteView), Zend2Utils.getView(FileUtil.toFile(albumController), albumControllerClass.getMethods().iterator().next()));

        PhpClass myTestControllerClass = new PhpClass("MyTestController", "Album\\Controller\\MyTestController");
        myTestControllerClass.addMethod("helloWorldAction", null);
        FileObject myTestController = zf2project.getFileObject("module/Album/src/Album/Controller/MyTestController.php");
        FileObject helloWorldView = zf2project.getFileObject("module/Album/view/album/my-test/hello-world.phtml");
        assertEquals(FileUtil.toFile(helloWorldView), Zend2Utils.getView(FileUtil.toFile(myTestController), myTestControllerClass.getMethods().iterator().next()));
    }

    public void testNamespaceFromView() {
        FileObject deleteView = zf2project.getFileObject("module/Album/view/album/album/delete.phtml");
        assertNotNull(deleteView);
        assertEquals("Album", Zend2Utils.getNamespaceFromView(FileUtil.toFile(deleteView)));

        FileObject helloWorldView = zf2project.getFileObject("module/Album/view/album/my-test/hello-world.phtml");
        assertNotNull(helloWorldView);
        assertEquals("Album", Zend2Utils.getNamespaceFromView(FileUtil.toFile(helloWorldView)));
    }

    public void testViewName() {
        assertEquals("index", Zend2Utils.getViewName("indexAction"));
        assertEquals("all-jobs", Zend2Utils.getViewName("allJobsAction"));
    }

    public void testViewFolderName() {
        assertEquals("album", Zend2Utils.getViewFolderName("AlbumController.php"));
        assertEquals("my-test", Zend2Utils.getViewFolderName("MyTestController.php"));
    }

    public void testIsController() {
        FileObject albumController = zf2project.getFileObject("module/Album/src/Album/Controller/AlbumController.php");
        assertTrue(Zend2Utils.isController(FileUtil.toFile(albumController)));

        FileObject myTestController = zf2project.getFileObject("module/Album/src/Album/Controller/MyTestController.php");
        assertTrue(Zend2Utils.isController(FileUtil.toFile(myTestController)));
    }

    public void testIsNotController() {
        File project = FileUtil.toFile(zf2project);
        assertFalse(Zend2Utils.isController(project));

        File notController = new File(project, "module/Album/view/album/not-view.phtml");
        assertTrue(notController.isFile());
        assertFalse(Zend2Utils.isController(notController));

        notController = new File(project, "module/Album/view/album/my-test/non-existing-file.phtml");
        assertFalse(notController.exists());
        assertFalse(Zend2Utils.isController(notController));
    }

    public void testController() {
        FileObject deleteView = zf2project.getFileObject("module/Album/view/album/album/delete.phtml");
        FileObject albumController = zf2project.getFileObject("module/Album/src/Album/Controller/AlbumController.php");
        assertEquals(FileUtil.toFile(albumController), Zend2Utils.getController(FileUtil.toFile(deleteView)));

        FileObject helloWorldView = zf2project.getFileObject("module/Album/view/album/my-test/hello-world.phtml");
        FileObject myTestController = zf2project.getFileObject("module/Album/src/Album/Controller/MyTestController.php");
        assertEquals(FileUtil.toFile(myTestController), Zend2Utils.getController(FileUtil.toFile(helloWorldView)));

        FileObject noControllerView = zf2project.getFileObject("module/Album/view/album/no-controller/index.phtml");
        assertNull(Zend2Utils.getController(FileUtil.toFile(noControllerView)));
    }

    public void testNamespaceFromController() {
        FileObject albumController = zf2project.getFileObject("module/Album/src/Album/Controller/AlbumController.php");
        assertNotNull(albumController);
        assertEquals("Album", Zend2Utils.getNamespaceFromController(FileUtil.toFile(albumController)));

        FileObject myTestController = zf2project.getFileObject("module/Album/src/Album/Controller/MyTestController.php");
        assertNotNull(myTestController);
        assertEquals("Album", Zend2Utils.getNamespaceFromController(FileUtil.toFile(myTestController)));
    }

    public void testControllerName() {
        assertEquals("IndexController", Zend2Utils.getControllerName("index"));
        assertEquals("AllJobsController", Zend2Utils.getControllerName("all-jobs"));
    }

    public void testActionName() {
        assertEquals("indexAction", Zend2Utils.getActionName("index"));
        assertEquals("allJobsAction", Zend2Utils.getActionName("all-jobs"));

        FileObject deleteView = zf2project.getFileObject("module/Album/view/album/album/delete.phtml");
        assertNotNull(deleteView);
        assertEquals("deleteAction", Zend2Utils.getActionName(FileUtil.toFile(deleteView)));

        FileObject helloWorldView = zf2project.getFileObject("module/Album/view/album/my-test/hello-world.phtml");
        assertNotNull(helloWorldView);
        assertEquals("helloWorldAction", Zend2Utils.getActionName(FileUtil.toFile(helloWorldView)));
    }

    public void testDashize() {
        assertEquals("test", Zend2Utils.dashize("Test"));
        assertEquals("test", Zend2Utils.dashize("test"));

        assertEquals("all-jobs", Zend2Utils.dashize("AllJobs"));
        assertEquals("all-jobs", Zend2Utils.dashize("allJobs"));

        assertEquals("all-my-jobs", Zend2Utils.dashize("AllMyJobs"));
        assertEquals("all-my-jobs", Zend2Utils.dashize("allMyJobs"));
    }

    public void testUndashize() {
        assertEquals("Test", Zend2Utils.undashize("test", false));
        assertEquals("test", Zend2Utils.undashize("test", true));

        assertEquals("AllJobs", Zend2Utils.undashize("all-jobs", false));
        assertEquals("allJobs", Zend2Utils.undashize("all-jobs", true));

        assertEquals("AllMyJobs", Zend2Utils.undashize("all-my-jobs", false));
        assertEquals("allMyJobs", Zend2Utils.undashize("all-my-jobs", true));
    }

}
