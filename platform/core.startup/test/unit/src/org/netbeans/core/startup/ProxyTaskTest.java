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
package org.netbeans.core.startup;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProxyTaskTest {
    
    public ProxyTaskTest() {
    }

    @Test
    public void testSomeMethod() throws Exception {
        T t1 = new T();
        T t2 = new T();
        
        List<Task> l = new ArrayList<Task>();
        l.add(t1);
        l.add(Task.EMPTY);
        l.add(t2);
        
        Task p = new ProxyTask(l);
        
        assertFalse("Not finished yet", p.waitFinished(100));
        
        t2.done();
        
        assertFalse("Still not finished yet", p.waitFinished(100));
        
        t1.done();
        p.waitFinished();
    }
    
    class T extends Task {
        public T() {
            notifyRunning();
        }
        public void done() {
            notifyFinished();
        }
    }
    
}
