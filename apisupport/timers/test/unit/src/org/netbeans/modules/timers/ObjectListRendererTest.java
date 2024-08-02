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

package org.netbeans.modules.timers;

import java.awt.Component;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JList;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class ObjectListRendererTest extends NbTestCase {
    private FileObject fo;
    private DataObject obj;
    private Node node;
    
    
    public ObjectListRendererTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(DL.class);
        
        fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "data.dat");
        obj = DataObject.find(fo);
        assertEquals("MDO: " + obj, MDO.class, obj.getClass());
        node = obj.getNodeDelegate();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    /**
     * Test of getListCellRendererComponent method, of class ObjectListRenderer.
     */
    public void testRenderFileObject() {
        JList list = new JList();
        Object wr = new WeakReference<Object>(fo);
        int index = 0;
        boolean isSelected = false;
        boolean cellHasFocus = false;
        ObjectListRenderer instance = new ObjectListRenderer();
        Component result = instance.getListCellRendererComponent(list, wr, index, isSelected, cellHasFocus);
        if (!(result instanceof JLabel)) {
            fail("Not JLabel: " + result);
        }
        JLabel l = (JLabel)result;
        assertEquals("Name", "Ahoj", l.getText());
    }

    public void testRenderDataObject() {
        JList list = new JList();
        Object wr = new WeakReference<Object>(obj);
        int index = 0;
        boolean isSelected = false;
        boolean cellHasFocus = false;
        ObjectListRenderer instance = new ObjectListRenderer();
        Component result = instance.getListCellRendererComponent(list, wr, index, isSelected, cellHasFocus);
        if (!(result instanceof JLabel)) {
            fail("Not JLabel: " + result);
        }
        JLabel l = (JLabel)result;
        assertEquals("Name", "Ahoj", l.getText());
    }

    public void testRenderInvalidDataObject() throws IOException {
        JList list = new JList();
        Object wr = new WeakReference<Object>(obj);
        int index = 0;
        boolean isSelected = false;
        boolean cellHasFocus = false;
        obj.delete();
        CharSequence log = Log.enable("", Level.WARNING);
        ObjectListRenderer instance = new ObjectListRenderer();
        Component result = instance.getListCellRendererComponent(list, wr, index, isSelected, cellHasFocus);
        if (!(result instanceof JLabel)) {
            fail("Not JLabel: " + result);
        }
        JLabel l = (JLabel)result;
        assertEquals("Name", obj.getName(), l.getText());
        if (log.length() > 0) {
            fail("There should be no warnings!\n" + log);
        }
    }

    public void testRenderNode() {
        JList list = new JList();
        Object wr = new WeakReference<Object>(node);
        int index = 0;
        boolean isSelected = false;
        boolean cellHasFocus = false;
        ObjectListRenderer instance = new ObjectListRenderer();
        Component result = instance.getListCellRendererComponent(list, wr, index, isSelected, cellHasFocus);
        if (!(result instanceof JLabel)) {
            fail("Not JLabel: " + result);
        }
        JLabel l = (JLabel)result;
        assertEquals("Name", "Ahoj", l.getText());
    }

    public static final class DL extends UniFileLoader {
        public DL() {
            super(MDO.class.getName());
            getExtensions().addExtension("dat");
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MDO(primaryFile, this);
        }
    }
    
    private static final class MDO extends MultiDataObject {
        private MDO(FileObject primaryFile, DL loader) throws DataObjectExistsException {
            super(primaryFile, loader);
        }

        @Override
        protected Node createNodeDelegate() {
            AbstractNode n = new AbstractNode(Children.LEAF);
            n.setName("Ahoj");
            return n;
        }
    }
}
