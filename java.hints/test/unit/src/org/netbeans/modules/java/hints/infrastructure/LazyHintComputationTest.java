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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            super(file, null, -1, null, null);
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
