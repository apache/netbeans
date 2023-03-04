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

package org.openide.explorer.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 *
 * @author Jaroslav Tulach
 */
public class DragDropUtilitiesTest extends NbTestCase {
    
    public DragDropUtilitiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(new Class[] {MyClipboard.class});
        last = null;
    }
    
    public void testGetNodeTransferableForSingleNodeCopy() throws Exception {
        N node = new N();
        
        Transferable t = DragDropUtilities.getNodeTransferable(node, NodeTransfer.DND_COPY);
        
        assertEquals("One call to copy", 1, node.copy);
        assertEquals("Also one call to drag which delegates to copy", 1, node.drag);
        assertEquals("No call to cut", 0, node.cut);
        assertNotNull("Call to convertor", last);
        assertTrue("StringSelection got to ExClipboard convertor", last instanceof StringSelection);
    }
    
    public void testGetNodeTransferableForSingleNodeCut() throws Exception {
        N node = new N();
        
        Transferable t = DragDropUtilities.getNodeTransferable(node, NodeTransfer.DND_MOVE);
        
        assertEquals("One call to cut", 1, node.cut);
        assertEquals("No call to drag", 0, node.drag);
        assertEquals("No call to copy", 0, node.copy);
        assertNotNull("Call to convertor", last);
        assertTrue("StringSelection got to ExClipboard convertor", last instanceof StringSelection);
    }
    
    public void testMultiTransferableForCopy() throws Exception {
        N node = new N();
        N n2 = new N();
        N[] arr = { node, n2 };
        
        Transferable t = DragDropUtilities.getNodeTransferable(arr, NodeTransfer.DND_COPY);
        
        assertEquals("One call to copy", 1, node.copy);
        assertEquals("One call to copy on n2", 1, n2.copy);
        assertEquals("Also one call to drag which delegates to copy", 1, node.drag);
        assertEquals("Also one call to drag which delegates to copy on n2", 1, n2.drag);
        assertEquals("No call to cut", 0, node.cut);
        assertEquals("No call to cut", 0, n2.cut);
        
        assertNotNull("Call to convertor", last);
        assertTrue("multi flavor supported", last.isDataFlavorSupported(ExTransferable.multiFlavor));
        Object obj = last.getTransferData(ExTransferable.multiFlavor);
        if (!( obj instanceof MultiTransferObject)) {
            fail("It should be MultiTransferObject: " + obj);
        }
        MultiTransferObject m = (MultiTransferObject)obj;
        
        assertEquals("Two in multi", 2, m.getCount());
        assertTrue("Is string", m.getTransferData(0, DataFlavor.stringFlavor) instanceof String);
        assertTrue("Is string2", m.getTransferData(1, DataFlavor.stringFlavor) instanceof String);
    }
    
    public void testMultiTransferableForCut() throws Exception {
        N node = new N();
        N n2 = new N();
        N[] arr = { node, n2 };
        
        Transferable t = DragDropUtilities.getNodeTransferable(arr, NodeTransfer.DND_MOVE);
        
        assertEquals("One call to cut ", 1, node.cut);
        assertEquals("One call to cut on n2", 1, n2.cut);
        assertEquals("No to drag", 0, node.drag);
        assertEquals("No to drag on n2", 0, n2.drag);
        assertEquals("No call to copy", 0, node.copy);
        assertEquals("No call to copy on n2", 0, n2.copy);
        
        assertNotNull("Call to convertor", last);
        assertTrue("multi flavor supported", last.isDataFlavorSupported(ExTransferable.multiFlavor));
        Object obj = last.getTransferData(ExTransferable.multiFlavor);
        if (!( obj instanceof MultiTransferObject)) {
            fail("It should be MultiTransferObject: " + obj);
        }
        MultiTransferObject m = (MultiTransferObject)obj;
        
        assertEquals("Two in multi", 2, m.getCount());
        assertTrue("Is string", m.getTransferData(0, DataFlavor.stringFlavor) instanceof String);
        assertTrue("Is string2", m.getTransferData(1, DataFlavor.stringFlavor) instanceof String);
    }
    
    
    
    private static class N extends AbstractNode {
        public int copy;
        public int cut;
        public int drag;
        public Transferable ret = new StringSelection("A text");
        
        public N() {
            super(Children.LEAF);
        }
        
        public Transferable clipboardCut() throws IOException {
            cut++;
            return ret;
        }
        
        public Transferable clipboardCopy() throws IOException {
            copy++;
            return ret;
        }
        
        public Transferable drag() throws IOException {
            drag++;
            return super.drag();
        }
    }
    
    public static Transferable last;
    
    public static final class MyClipboard extends ExClipboard {
        
        public MyClipboard() {
            super("Empty");
        }
        
        public ExClipboard.Convertor[] getConvertors() {
            return new ExClipboard.Convertor[] {new ExClipboard.Convertor() {
                public Transferable convert(Transferable t) {
                    last = t;
                    return t;
                }
            }};
        }
    }
}
