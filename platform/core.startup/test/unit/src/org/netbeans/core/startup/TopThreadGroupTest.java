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
import org.netbeans.junit.NbTestCase;

public class TopThreadGroupTest extends NbTestCase implements Thread.UncaughtExceptionHandler {
    List<Throwable> throwables;
    
    public TopThreadGroupTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        throwables = new ArrayList<Throwable>();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
    


    public void testSomeMethod() throws InterruptedException {
        TopThreadGroup ttg = new TopThreadGroup("test group", new String[0]);
        final IllegalStateException ise = new IllegalStateException();
        Thread t = new Thread((ThreadGroup)ttg, "throw exception") {
            @Override
            public void run() {
                throw ise;
            }
        };
        t.start();
        t.join();
        assertEquals("One exception: " + throwables, 1, throwables.size());
        assertEquals("It is our exception", ise, throwables.get(0));
        
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        throwables.add(e);
    }
}
