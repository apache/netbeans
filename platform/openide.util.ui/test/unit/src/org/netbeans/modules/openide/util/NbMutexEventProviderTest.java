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
package org.netbeans.modules.openide.util;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class NbMutexEventProviderTest {

    public NbMutexEventProviderTest() {
    }

    @Test
    public void lookupPropagationReadAccessRunnable() throws Exception {
        performTest(Mutex.EVENT::readAccess);
    }

    @Test
    public void lookupPropagationReadAccessAction() throws Exception {
        performTest((r) -> {
            Mutex.EVENT.readAccess((Action) () -> {
                r.run();
                return null;
            });
        });
    }

    @Test
    public void lookupPropagationWriteAccessRunnable() throws Exception {
        performTest(Mutex.EVENT::writeAccess);
    }

    @Test
    public void lookupPropagationWriteAccessAction() throws Exception {
        performTest((r) -> {
            Mutex.EVENT.writeAccess((Action) () -> {
                r.run();
                return null;
            });
        });
    }

    @Test
    public void lookupPropagationPostReadRequest() throws Exception {
        performTest(Mutex.EVENT::postReadRequest);
    }

    @Test
    public void lookupPropagationPostWriteRequest() throws Exception {
        performTest(Mutex.EVENT::postWriteRequest);
    }

    private void performTest(Consumer<Runnable> enterEventQueue) throws InterruptedException {
        Token token = new Token();
        CountDownLatch cdl = new CountDownLatch(1);
        Error[] observedError = { null };
        assertLookupGlobalToken("No token yet", null);
        final Lookup compound = new ProxyLookup(Lookups.singleton(token), Lookup.getDefault());
        Lookups.executeWith(compound, () -> {
            assertLookupGlobalToken("Set now", token);
            enterEventQueue.accept(() -> {
                try {
                    assertLookupGlobalToken("availableInReadAccess", token);
                } catch (Error err) {
                    observedError[0] = err;
                } finally {
                    cdl.countDown();
                }
            });
        });
        assertLookupGlobalToken("No token again", null);
        cdl.await();
        if (observedError[0] != null) {
            throw observedError[0];
        }
        assertLookupGlobalToken("No token still", null);
    }

    private void assertLookupGlobalToken(String msg, Token expected) {
        Token actual = Lookup.getDefault().lookup(Token.class);
        assertSame(msg, expected, actual);
    }

    private static final class Token {
    }
}
