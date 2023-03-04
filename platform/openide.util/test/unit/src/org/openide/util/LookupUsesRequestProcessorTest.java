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

package org.openide.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LookupUsesRequestProcessorTest extends NbTestCase
implements LookupListener {
    int cnt;

    public LookupUsesRequestProcessorTest(String s) {
        super(s);
    }

    public void testMetaInfLookupDeliversEventsInRPThread() throws InterruptedException {
        ClassLoader l = new MyCL();
        Lookup lkp = Lookups.metaInfServices(l);
        Lookup.Result<Runnable> result = lkp.lookupResult(Runnable.class);
        result.addLookupListener(this);

        assertNull("No runnables found", lkp.lookup(Runnable.class));
        assertNotNull("Thread found", lkp.lookup(Thread.class));
        assertNotNull("Now runnable found", lkp.lookup(Runnable.class));
        synchronized (this) {
            int retry = 5;
            while (cnt == 0 && retry-- > 0) {
                wait(1000);
            }
        }    
        assertEquals("Count is now 1", 1, cnt);
    }

    @Override
    public synchronized void resultChanged(LookupEvent unused) {
        if (Thread.currentThread().getName().contains("request-processor")) {
            cnt++;
            notifyAll();
            return;
        }
        fail("Changes shall be delivered in request processor thread. But was: " + Thread.currentThread().getName());
    }


    private static final class MyCL extends ClassLoader {

        @Override
        protected Enumeration<URL> findResources(String path) throws IOException {
            if (path.equals("META-INF/services/java.lang.Thread")) {
                return Collections.enumeration(
                    Collections.singleton(
                        LookupUsesRequestProcessorTest.class.getResource(LookupUsesRequestProcessorTest.class.getSimpleName() + ".resource")
                    )
                );
            }
            return super.findResources(path);
        }

    }
}
