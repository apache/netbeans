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
package org.netbeans.modules.netbinox;

import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/** Checks race condition in the NetbinoxHooks class.
 * IZ #201538
 * @author Jaroslav Tulach
 */
public class HookRaceTest extends NbTestCase {

    public HookRaceTest(String name) {
        super(name);
    }

    public void testSlowInit() throws InterruptedException {
        MockServices.setServices(SlowH.class);
        SlowH inst = Lookup.getDefault().lookup(SlowH.class);
        assertNotNull("SlowH registered", inst);
        Task task = RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                MockServices.setServices(H1.class, SlowH.class);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                SlowH.goOn();
            }
        }, 1000);
        NetbinoxHooks h = new NetbinoxHooks();
        final HookRegistry registry = new HookRegistry(null);
        h.addHooks(registry);
        task.waitFinished();
        
        assertEquals("H1 notified", 1, H1.waitCnt());
        
        SlowH last = Lookup.getDefault().lookup(SlowH.class);
        assertEquals("No change in instances", inst, last);
        
        assertEquals("Slow notified just once", 1, SlowH.cnt);
    }
    
    
    
    public static final class H1 implements HookConfigurator {
        private static int cnt;
        
        @Override
        public void addHooks(HookRegistry hr) {
            synchronized (H1.class) {
                cnt++;
                H1.class.notifyAll();
            }
        }
        
        public static synchronized int waitCnt() throws InterruptedException {
            while (cnt == 0) {
                H1.class.wait();
            }
            return cnt;
        }
    }

    public static final class SlowH implements HookConfigurator {
        private static final Object LOCK = new Object();
        private static boolean proceed;
        static volatile int cnt;
        
        @Override
        public void addHooks(HookRegistry hr) {
            synchronized (LOCK) {
                if (cnt++ == 0) {
                    while (!proceed) {
                        try {
                            LOCK.wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        
        public static void goOn() {
            synchronized (LOCK) {
                proceed = true;
                LOCK.notifyAll();
            }
        }
    }
}
