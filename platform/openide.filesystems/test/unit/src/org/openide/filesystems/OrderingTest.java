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

package org.openide.filesystems;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

public class OrderingTest extends NbTestCase {

    public OrderingTest(String n) {
        super(n);
    }

    private FileObject dir, apex, ball, cone, dent;
    private CharSequence LOG;

    protected @Override void setUp() throws Exception {
        super.setUp();
        dir = FileUtil.createMemoryFileSystem().getRoot();
        apex = dir.createData("apex");
        ball = dir.createData("ball");
        cone = dir.createData("cone");
        dent = dir.createData("dent");
        LOG = Log.enable(Ordering.class.getName(), Level.WARNING);
        assertEmptyLog();
    }

    @Override
    protected Level logLevel() {
        return Level.OFF;
    }

    private void assertOrder(boolean logWarnings, FileObject... expectedOrder) throws Exception {
        assertEquals(Arrays.asList(expectedOrder), Ordering.getOrder(Arrays.asList(dir.getChildren()), logWarnings));
    }

    private void assertEmptyLog() {
        assertEquals("", LOG.toString());
    }

    private void assertLog(String mentionedSubstring) {
        assertTrue(LOG.toString(), LOG.toString().contains(mentionedSubstring));
    }

    public void testGetOrderNumeric() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 9);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 5);
        assertOrder(true, dent, ball, apex, cone);
        assertEmptyLog();
    }

    public void testGetOrderNoPositions() throws Exception {
        assertOrder(true, apex, ball, cone, dent);
        assertEmptyLog();
    }

    public void testStableSort() throws Exception {
        List<FileObject> order = Arrays.asList(ball, dent, apex, cone);
        assertEquals(order, Ordering.getOrder(order, false));
    }

    public void testGetOrderMissingPositions() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 9);
        assertOrder(false, ball, apex, cone, dent);
        assertEmptyLog();
        assertOrder(true, ball, apex, cone, dent);
        assertLog("cone");
        assertLog("dent");
    }
    
    private FileObject cdir;
    
    private void setupConfigData() throws IOException {
        String n = "test/" + getName();
        FileObject f = FileUtil.getConfigFile(n);
        if (f != null) {
            f.delete();
        }
        dir = FileUtil.createFolder(FileUtil.getConfigRoot(), n);
        apex = dir.createData("apex");
        ball = dir.createData("ball");
        cone = dir.createData("cone");
        dent = dir.createData("dent");
    }
    
    public void setupFilesOrderedFoldersUnkown() throws Exception {
        FileObject zafod = dir.createFolder("zafod");
        FileObject arthur = dir.createFolder("arthur");

        apex.setAttribute("position", 17);
        ball.setAttribute("position", 5);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 9);

        assertOrder(false, ball, dent, apex, cone, zafod, arthur);
        assertEmptyLog();
        assertOrder(true, ball, dent, apex, cone, zafod, arthur);
    }
    
    public void setupFoldersOrderedFilesUnkown() throws Exception {
        FileObject zafod = dir.createFolder("zafod");
        FileObject arthur = dir.createFolder("arthur");

        zafod.setAttribute("position", 17);
        arthur.setAttribute("position", 5);

        assertOrder(false, apex,ball, cone, dent, arthur, zafod);
        assertEmptyLog();
        assertOrder(true, apex,ball,cone,  dent, arthur, zafod);
    }
    
    public void testMissingFolderOrderingFilesOrderedConfig() throws Exception {
        setupConfigData();
        setupFilesOrderedFoldersUnkown();
        assertEmptyLog();
    }

    public void testMissingFolderOrderingFilesOrderedRegular() throws Exception {
        setupFilesOrderedFoldersUnkown();
        assertLog("zafod");
        assertLog("arthur");
    }

    public void testMissingFileOrderingFoldersOrderedConfig() throws Exception {
        setupConfigData();
        setupFoldersOrderedFilesUnkown();
        assertEmptyLog();
    }

    public void testMissingFileOrderingFoldersOrderedRegular() throws Exception {
        setupFoldersOrderedFilesUnkown();
        assertLog("apex");
        assertLog("ball");
        assertLog("cone");
        assertLog("dent");
    }

    public void testGetOrderEqualPositions() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 5);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 5);
        assertOrder(false, ball, dent, apex, cone);
        assertEmptyLog();
        assertOrder(true, ball, dent, apex, cone);
        assertLog("ball");
        assertLog("dent");
    }

    public void testGetOrderRelativeAttrs() throws Exception {
        dir.setAttribute("dent/ball", true);
        dir.setAttribute("ball/apex", true);
        dir.setAttribute("apex/cone", true);
        assertOrder(false, dent, ball, apex, cone);
        assertEmptyLog();
        assertOrder(true, dent, ball, apex, cone);
        assertLog("dent/ball");
        assertLog("ball/apex");
        assertLog("apex/cone");
    }

    public void testGetOrderMixed() throws Exception {
        dent.setAttribute("position", 10);
        apex.setAttribute("position", 90);
        dir.setAttribute("dent/cone", true);
        dir.setAttribute("cone/ball", true);
        dir.setAttribute("ball/apex", true);
        assertOrder(false, dent, cone, ball, apex);
        assertEmptyLog();
        assertOrder(true, dent, cone, ball, apex);
        assertLog("dent/cone");
        assertLog("cone/ball");
        assertLog("ball/apex");
    }

    public void testGetOrderNonNaturalNumbers() throws Exception {
        apex.setAttribute("position", 33.333);
        ball.setAttribute("position", -213L);
        cone.setAttribute("position", 5.4e3d);
        dent.setAttribute("position", (short) 200);
        assertOrder(true, ball, apex, dent, cone);
        assertEmptyLog();
    }

    public void testGetOrderNonNumericPositionAttrs() throws Exception {
        apex.setAttribute("position", "Timbuktu");
        assertOrder(false, apex, ball, cone, dent);
        assertEmptyLog();
        assertOrder(true, apex, ball, cone, dent);
        assertLog("apex");
        assertLog("Timbuktu");
    }

    public void testGetOrderRelativeAttrsFalse() throws Exception {
        dir.setAttribute("ball/apex", false);
        assertOrder(true, apex, ball, cone, dent);
        assertEmptyLog();
    }

    public void testGetOrderRelativeAttrsNonBoolean() throws Exception {
        dir.setAttribute("ball/apex", "maybe");
        assertOrder(false, apex, ball, cone, dent);
        assertEmptyLog();
        assertOrder(true, apex, ball, cone, dent);
        assertLog("ball/apex");
        assertLog("maybe");
    }

    public void testGetOrderRelativeAttrsNonexistentChildren() throws Exception {
        dir.setAttribute("apex/wacko", true);
        assertOrder(false, apex, ball, cone, dent);
        assertEmptyLog();
        assertOrder(true, apex, ball, cone, dent);
        assertLog("apex/wacko");
    }

    public void testGetOrderOnOnlySomeChildren() throws Exception {
        assertEquals(Collections.emptyList(), Ordering.getOrder(Collections.<FileObject>emptyList(), true));
        assertEmptyLog();
        apex.setAttribute("position", 20);
        ball.setAttribute("position", 10);
        assertEquals(Arrays.asList(ball, apex), Ordering.getOrder(Arrays.asList(apex, ball), true));
        assertEmptyLog();
    }

    public void testGetOrderDifferentParents() throws Exception {
        FileObject other = FileUtil.createData(dir, "subdir/other");
        try {
            Ordering.getOrder(Arrays.asList(apex, ball, other), false);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    public void testGetOrderDuplicates() throws Exception {
        try {
            Ordering.getOrder(Arrays.asList(apex, apex), false);
            fail();
        } catch (IllegalArgumentException e) {}
    }

    public void testGetOrderTopologicalSortException() throws Exception {
        dir.setAttribute("apex/ball", true);
        dir.setAttribute("ball/apex", true);
        Ordering.getOrder(Arrays.asList(apex, ball, cone, dent), false);
        assertEmptyLog();
        Ordering.getOrder(Arrays.asList(apex, ball, cone, dent), true);
        assertLog("apex");
        assertLog("ball");
    }

    public void testSetOrderBasic() throws Exception {
        Ordering.setOrder(Arrays.asList(dent, ball, apex, cone));
        assertOrder(true, dent, ball, apex, cone);
        assertEmptyLog();
    }

    public void testGetOrderZeroPositions() throws Exception { // #107550
        apex.setAttribute("position", 0);
        ball.setAttribute("position", 0);
        cone.setAttribute("position", 22);
        dent.setAttribute("position", 5);
        assertOrder(true, apex, ball, dent, cone);
        assertEmptyLog();
    }

    public void testSetOrderConservativeOneJump() throws Exception {
        apex.setAttribute("position", 17);
        ball.setAttribute("position", 9);
        cone.setAttribute("position", 23);
        dent.setAttribute("position", 5);
        Ordering.setOrder(Arrays.asList(dent, apex, ball, cone));
        assertOrder(true, dent, apex, ball, cone);
        assertEquals(5, dent.getAttribute("position"));
        assertEquals(23, cone.getAttribute("position"));
        assertEquals(17, apex.getAttribute("position"));
        assertEquals(20, ball.getAttribute("position"));
        assertEmptyLog();
        // XXX test also complex reorders; swaps with left bias; larger rotations; moves to start or end; {X} => {X} and {} => {}; ad nauseam
        // XXX test sO when newly added item (e.g. at end, or elsewhere) has no initial position
    }

    public void testSetOrderInitiallyZero() throws Exception { // #115343
        apex.setAttribute("position", 0);
        ball.setAttribute("position", 0);
        cone.setAttribute("position", 0);
        dent.setAttribute("position", 0);
        Ordering.setOrder(Arrays.asList(apex, cone, ball, dent));
        assertOrder(true, apex, cone, ball, dent);
    }

    public void testSetOrderNewChild() throws Exception { // #110981
        apex.setAttribute("position", 200);
        ball.setAttribute("position", 250);
        cone.setAttribute("position", 300);
        Ordering.setOrder(Arrays.asList(apex, ball, cone, dent));
        assertOrder(true, apex, ball, cone, dent);
        assertEquals(200, apex.getAttribute("position"));
        assertEquals(250, ball.getAttribute("position"));
        assertEquals(300, cone.getAttribute("position"));
        assertEquals(400, dent.getAttribute("position"));
        apex.setAttribute("position", 200);
        ball.setAttribute("position", 250);
        cone.setAttribute("position", 300);
        dent.setAttribute("position", null);
        Ordering.setOrder(Arrays.asList(apex, dent, ball, cone));
        assertOrder(true, apex, dent, ball, cone);
        assertEquals(200, apex.getAttribute("position"));
        assertEquals(250, ball.getAttribute("position"));
        assertEquals(300, cone.getAttribute("position"));
        assertEquals(225, dent.getAttribute("position"));
        apex.setAttribute("position", 200);
        ball.setAttribute("position", 250);
        cone.setAttribute("position", 300);
        dent.setAttribute("position", null);
        Ordering.setOrder(Arrays.asList(dent, apex, ball, cone));
        assertOrder(true, dent, apex, ball, cone);
        assertEquals(200, apex.getAttribute("position"));
        assertEquals(250, ball.getAttribute("position"));
        assertEquals(300, cone.getAttribute("position"));
        assertEquals(100, dent.getAttribute("position"));
    }

    /** Tests new child without position attribute is correctly ordered
     * and others are reorderer as well (see issue #131021). */
    public void testSetOrderNewChildAndReorder131021() throws Exception {
        apex.setAttribute("position", 200);
        ball.setAttribute("position", 250);
        cone.setAttribute("position", 300);
        dent.setAttribute("position", null);
        Ordering.setOrder(Arrays.asList(dent, ball, apex, cone));
        assertOrder(true, dent, ball, apex, cone);
        assertEquals(200, apex.getAttribute("position"));
        assertEquals(175, ball.getAttribute("position"));
        assertEquals(300, cone.getAttribute("position"));
        assertEquals(150, dent.getAttribute("position"));
    }

    public void testSetOrderSingle() throws Exception {
        dir = FileUtil.createMemoryFileSystem().getRoot();
        FileObject f = dir.createData("f");
        Ordering.setOrder(Collections.singletonList(f));
    }

    public void testDontComplainAboutHiddenFiles() throws IOException {
        FileObject[] old = dir.getChildren();
        for (int i = 0; i < old.length; i++) {
            old[i].setAttribute("position", i);
        }
        FileObject fo = dir.createData("huk.buk_hidden");
        final List<FileObject> arr = Arrays.asList(dir.getChildren());
        assertTrue("Fo is there", arr.contains(fo));
        Ordering.getOrder(arr, true);
        assertEmptyLog();
    }
    
    // XXX test IAE, ...

}
