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
package org.netbeans.modules.java.hints.infrastructure;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class LazyHintComputationTest extends NbTestCase {
    
    /** Creates a new instance of LazyHintComputationTest */
    public LazyHintComputationTest(String name) {
        super(name);
    }
    
    private FileObject data;
    
    @Override
    public void setUp() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        data = fs.getRoot().createData("test.java");
    }
    
    public void testCancel() throws Exception {
        final LazyHintComputation c = new LazyHintComputation(data);
        boolean[] first = new boolean[1];
        boolean[] second = new boolean[1];
        boolean[] third = new boolean[1];
        final boolean[] callback = new boolean[1];
        final boolean[] doCancel = new boolean[] {true};
        final boolean[] firstCancelled = new boolean[1];
        final boolean[] secondCancelled = new boolean[1];
        final boolean[] thirdCancelled = new boolean[1];
        
        LazyHintComputationFactory.addToCompute(data, new CreatorBasedLazyFixListImpl(first, null, new Runnable() {
            public void run() {
                firstCancelled[0] = true;
            }
        }));
        
        LazyHintComputationFactory.addToCompute(data, new CreatorBasedLazyFixListImpl(second, new Runnable() {
            public void run() {
                if (doCancel[0]) {
                    c.cancel();
                    callback[0] = true;
                }
            }
        }, new Runnable() {
            public void run() {
                secondCancelled[0] = true;
            }
        }));
        
        LazyHintComputationFactory.addToCompute(data, new CreatorBasedLazyFixListImpl(third, null, new Runnable() {
            public void run() {
                thirdCancelled[0] = true;
            }
        }));
        
        c.run(null);
        
        assertTrue(first[0]);
        assertTrue(second[0]);
        assertFalse(third[0]);
        assertTrue(callback[0]);
        assertFalse(firstCancelled[0]);
        assertTrue(secondCancelled[0]);
        assertFalse(thirdCancelled[0]);
        
        first[0] = second[0] = callback[0] = secondCancelled[0] = false;
        
        doCancel[0] = false;
        
        c.run(null);
        
        assertFalse(first[0]);
        assertTrue(second[0]);
        assertTrue(third[0]);
        assertFalse(callback[0]);
        assertFalse(firstCancelled[0]);
        assertFalse(secondCancelled[0]);
        assertFalse(thirdCancelled[0]);
    }
    
    public void test88996() throws Exception {
        boolean[] computed = new boolean[1];
        
        CreatorBasedLazyFixListImpl l = new CreatorBasedLazyFixListImpl(data, computed, null, null);
        
        l.getFixes();
        
        Reference r = new WeakReference(l);
        
        l = null;
        
        assertGC("Not holding the CreatorBasedLazyFixList hard", r);
    }
    
    private static final class CreatorBasedLazyFixListImpl extends CreatorBasedLazyFixList {
        
        private final boolean[] marker;
        private final Runnable callback;
        private final Runnable cancelCallback;
        
        public CreatorBasedLazyFixListImpl(FileObject file, boolean[] marker, Runnable callback, Runnable cancelCallback) {
            super(file, null, null, -1, null, null);
            this.marker = marker;
            this.callback = callback;
            this.cancelCallback = cancelCallback;
        }
        
        public CreatorBasedLazyFixListImpl(boolean[] marker, Runnable callback, Runnable cancelCallback) {
            this(null, marker, callback, cancelCallback);
        }
        
        @Override
        public void compute(CompilationInfo info, AtomicBoolean cancelled) {
            marker[0] = true;
            
            if (callback != null)
                callback.run();
        }
        
        @Override
        public void cancel() {
            if (cancelCallback != null) {
                cancelCallback.run();
            }
        }
    }
    
}
