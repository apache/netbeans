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
package org.netbeans.modules.favorites;

import java.util.concurrent.CountDownLatch;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class FavoritesNodeTest extends NbTestCase {
    
    public FavoritesNodeTest(String n) {
        super(n);
    }

    public void testCreateNodeAndChangeDataObject() throws Exception {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        final DataFolder folder = DataFolder.findFolder(fo);
        
        final InstanceContent ic = new InstanceContent();
        ic.add(folder);
        AbstractNode node = new AbstractNode(Children.LEAF, new AbstractLookup(ic));
        
        Node res = FavoritesNode.createFilterNode(node);
        assertFalse("Now it has children", res.isLeaf());
    }

    public void testCreateNodeAndChangeDataObjectInAWT () throws Exception {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        final DataFolder folder = DataFolder.findFolder(fo);
        
        final InstanceContent ic = new InstanceContent();
        ic.add(folder);
        final AbstractNode node = new AbstractNode(Children.LEAF, new AbstractLookup(ic));
        
        Mutex.EVENT.readAccess((Mutex.ExceptionAction<Void>) () -> {
            final CountDownLatch l = new CountDownLatch(1);
            FavoritesNode.RP.post(() -> {
                try {
                    l.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            Node res = FavoritesNode.createFilterNode(node);
            assertTrue("No children", res.isLeaf());
            l.countDown();
            final CountDownLatch l2 = new CountDownLatch(1);
            FavoritesNode.RP.post(l2::countDown);
            l2.await();
            assertFalse("Now it has children", res.isLeaf());
            return null;
        });
    }
}
