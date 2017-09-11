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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.timers;

import java.awt.Component;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JList;
import junit.framework.TestCase;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class ObjectListRendererTest extends TestCase {
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
