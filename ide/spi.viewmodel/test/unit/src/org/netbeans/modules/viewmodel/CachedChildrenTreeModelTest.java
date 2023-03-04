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

package org.netbeans.modules.viewmodel;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.CachedChildrenTreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import static org.junit.Assert.*;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;

/**
 *
 * @author martin
 */
public class CachedChildrenTreeModelTest {
    
    public CachedChildrenTreeModelTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getChildren method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testGetChildren() throws Exception {
        System.out.println("getChildren");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModel instance = new CachedChildrenTreeModelImpl();
        Object[] result = instance.getChildren("", from, to);
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
        result = instance.getChildren("b", from, to);
        assertArrayEquals(new Object[] {"ba", "bb", "bc"}, result);
    }

    /**
     * Test of computeChildren method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testComputeChildren() throws Exception {
        System.out.println("computeChildren");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModelImpl instance = new CachedChildrenTreeModelImpl();
        assertFalse(instance.isChildernComputed());
        Object[] result = instance.getChildren("", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
        // Ask again
        result = instance.getChildren("", from, to);
        assertFalse(instance.isChildernComputed());
        
        result = instance.getChildren("b", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"ba", "bb", "bc"}, result);
        
        result = instance.getChildren("b", from, to);
        assertFalse(instance.isChildernComputed());
        result = instance.getChildren("", from, to);
        assertFalse(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
    }

    /**
     * Test of cacheChildrenOf method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testCacheChildrenOf() throws Exception {
        System.out.println("cacheChildrenOf");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModelImpl instance = new CachedChildrenTreeModelImpl();
        assertFalse(instance.isChildernComputed());
        Object[] result = instance.getChildren("", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
        result = instance.getChildren("c", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"ca", "cb", "cc"}, result);
        result = instance.getChildren("cc", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"cca", "ccb", "ccc"}, result);
        result = instance.getChildren("cc", from, to);
        // cc is always re-computed
        assertTrue(instance.isChildernComputed());
        result = instance.getChildren("c", from, to);
        assertFalse(instance.isChildernComputed());
    }

    /**
     * Test of refreshCache method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testRefreshCache() throws Exception {
        System.out.println("refreshCache");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModelImpl instance = new CachedChildrenTreeModelImpl();
        assertFalse(instance.isChildernComputed());
        Object[] result = instance.getChildren("", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
        result = instance.getChildren("a", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"aa", "ab", "ac"}, result);
        
        result = instance.getChildren("", from, to);
        result = instance.getChildren("a", from, to);
        assertFalse(instance.isChildernComputed());
        
        instance.doRefreshCache("a");
        instance.asynchronous(null, AsynchronousModelFilter.CALL.CHILDREN, "a");
        result = instance.getChildren("a", from, to);
        assertTrue(instance.isChildernComputed());
    }

    /**
     * Test of clearCache method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testClearCache() throws Exception {
        System.out.println("clearCache");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModelImpl instance = new CachedChildrenTreeModelImpl();
        assertFalse(instance.isChildernComputed());
        Object[] result = instance.getChildren("", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
        result = instance.getChildren("a", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"aa", "ab", "ac"}, result);
        
        result = instance.getChildren("", from, to);
        result = instance.getChildren("a", from, to);
        assertFalse(instance.isChildernComputed());
        
        instance.doClearCache();
        result = instance.getChildren("", from, to);
        assertTrue(instance.isChildernComputed());
        result = instance.getChildren("a", from, to);
        assertTrue(instance.isChildernComputed());
    }

    /**
     * Test of reorder method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testReorder() throws Exception {
        System.out.println("reorder");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModelImpl instance = new CachedChildrenTreeModelImpl() {
            @Override
            protected Object[] reorder(Object[] nodes) {
                return new Object[] { nodes[2], nodes[0], nodes[1] };
            }
        };
        Object[] result = instance.getChildren("", from, to);
        assertArrayEquals(new Object[] {"c", "a", "b"}, result);
    }

    /**
     * Test of recomputeChildren method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testRecomputeChildren_0args() throws Exception {
        System.out.println("recomputeChildren");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModelImpl instance = new CachedChildrenTreeModelImpl();
        assertFalse(instance.isChildernComputed());
        Object[] result = instance.getChildren("", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
        result = instance.getChildren("a", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"aa", "ab", "ac"}, result);
        
        result = instance.getChildren("", from, to);
        result = instance.getChildren("a", from, to);
        assertFalse(instance.isChildernComputed());
        
        instance.doRecomputeChildren();
        assertTrue(instance.isChildernComputed());
        
        result = instance.getChildren("", from, to);
        result = instance.getChildren("a", from, to);
        assertFalse(instance.isChildernComputed());
    }

    /**
     * Test of recomputeChildren method, of class CachedChildrenTreeModel.
     */
    @Test
    public void testRecomputeChildren_Object() throws Exception {
        System.out.println("recomputeChildren");
        int from = 0;
        int to = Integer.MAX_VALUE;
        CachedChildrenTreeModelImpl instance = new CachedChildrenTreeModelImpl();
        assertFalse(instance.isChildernComputed());
        Object[] result = instance.getChildren("", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"a", "b", "c"}, result);
        result = instance.getChildren("a", from, to);
        assertTrue(instance.isChildernComputed());
        assertArrayEquals(new Object[] {"aa", "ab", "ac"}, result);
        
        result = instance.getChildren("", from, to);
        result = instance.getChildren("a", from, to);
        assertFalse(instance.isChildernComputed());
        
        instance.doRecomputeChildren("a");
        assertTrue(instance.isChildernComputed());
        
        result = instance.getChildren("", from, to);
        result = instance.getChildren("a", from, to);
        assertFalse(instance.isChildernComputed());
    }

    public class CachedChildrenTreeModelImpl extends CachedChildrenTreeModel {
        
        private AtomicBoolean childernComputed = new AtomicBoolean(false);

        @Override
        public Object[] computeChildren(Object node) throws UnknownTypeException {
            String s = (String) node;
            childernComputed.set(true);
            return new Object[] { s+"a", s+"b", s+"c" };
        }
        
        boolean isChildernComputed() {
            return childernComputed.getAndSet(false);
        }

        @Override
        protected boolean cacheChildrenOf(Object node) {
            if ("cc".equals(node)) {
                return false;
            }
            return super.cacheChildrenOf(node);
        }
        
        void doRefreshCache(Object node) {
            refreshCache(node);
        }
        
        void doClearCache() {
            clearCache();
        }
        
        void doRecomputeChildren() throws UnknownTypeException {
            recomputeChildren();
        }
        
        void doRecomputeChildren(Object node) throws UnknownTypeException {
            recomputeChildren(node);
        }
        
        @Override
        public Object getRoot() {
            return "";
        }

        @Override
        public boolean isLeaf(Object node) throws UnknownTypeException {
            return false;
        }

        @Override
        public int getChildrenCount(Object node) throws UnknownTypeException {
            return Integer.MAX_VALUE;
        }

        @Override
        public void addModelListener(ModelListener l) {
        }

        @Override
        public void removeModelListener(ModelListener l) {
        }
    }
    
}
