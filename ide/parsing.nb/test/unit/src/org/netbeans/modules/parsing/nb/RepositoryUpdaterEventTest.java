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

package org.netbeans.modules.parsing.nb;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import junit.framework.TestSuite;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdaterTest;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
public class RepositoryUpdaterEventTest extends RepositoryUpdaterTest {

    public RepositoryUpdaterEventTest(String name) {
        super(name);
    }
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite();
        s.addTest(new RepositoryUpdaterEventTest("testAWTIndexAndWaitDeadlock"));
        
        return s;
    }
    
    public void testAWTIndexAndWaitDeadlock() throws Exception {
        final Class<EventSupport.EditorRegistryListener> erlc = EventSupport.EditorRegistryListener.class;        
        final Field k24Field = erlc.getDeclaredField("k24");   //NOI18N
        assertNotNull (k24Field);
        k24Field.setAccessible(true);
        final AtomicBoolean cond = (AtomicBoolean) k24Field.get(null);
        
        final Source source = Source.create(f3);
        assertNotNull(source);

        Runnable action = new Runnable() {
            public void run() {
                try {
                    TaskProcessor.resetState(source, false, true);
                    cond.set(true);
                } catch (/*ReflectiveOperation*/Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        }
        else {
            SwingUtilities.invokeAndWait(action);
        }

        action = new Runnable() {
            public void run() {
                try {
                    IndexingManager.getDefault().refreshIndexAndWait(srcRootWithFiles1.getURL(), null);
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        }
        else {
            SwingUtilities.invokeAndWait(action);
        }


        action = new Runnable() {
            public void run() {
                try {
                    cond.set(false);
                    TaskProcessor.resetStateImpl(source);
                } catch (/*ReflectiveOperation*/Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        }
        else {
            SwingUtilities.invokeAndWait(action);
        }

    }

}
