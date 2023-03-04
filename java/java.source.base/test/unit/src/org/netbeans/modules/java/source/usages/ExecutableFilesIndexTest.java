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

package org.netbeans.modules.java.source.usages;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Lahoda
 */
public class ExecutableFilesIndexTest extends NbTestCase {
    
    public ExecutableFilesIndexTest(String testName) {
        super(testName);
    }            

    public void testWrapUnWrap() throws Exception {
        Set<String> strings = new HashSet<String>();
        
        strings.add("test");
        strings.add(":::");
        strings.add("\\\\\\\\\\\\");
        
        assertEquals(strings, ExecutableFilesIndex.unwrap(ExecutableFilesIndex.wrap(strings)));
        assertTrue(ExecutableFilesIndex.unwrap(ExecutableFilesIndex.wrap(Collections.<String>emptySet())).isEmpty());
    }
    
    public void testListenersGCAble() throws Exception {
        URL u = new URL("file:/A");
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {}
        };
        ExecutableFilesIndex.DEFAULT.addChangeListener(u, l);
        
        Reference<ChangeListener> rl = new WeakReference<ChangeListener>(l);
        
        boolean notGCAble = false;
        
        try {
            assertGC("", rl);
        } catch (AssertionFailedError e) {
            notGCAble = true;
        }
        
        assertTrue(notGCAble);
        
        l = null;
        
        assertGC("", rl);
    }
    
}
