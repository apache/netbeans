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

package org.openide.nodes;

import javax.swing.event.ChangeListener;
import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class IndexedNodeTest implements Index {

    public IndexedNodeTest() {
    }


    @Test
    public void testConstructorWithLookup() {
        Lookup lkp = Lookups.singleton(55);
        Node n = new IndexedNode(Children.LEAF, this, lkp) {};
        assertEquals(Integer.valueOf(55), n.getLookup().lookup(Integer.class));
    }

    @Override
    public int getNodesCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node[] getNodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOf(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reorder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reorder(int[] perm) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void move(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exchange(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void moveUp(int x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void moveDown(int x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addChangeListener(ChangeListener chl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeChangeListener(ChangeListener chl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}