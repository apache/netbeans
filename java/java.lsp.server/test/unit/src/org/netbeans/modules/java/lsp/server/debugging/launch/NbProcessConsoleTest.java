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
package org.netbeans.modules.java.lsp.server.debugging.launch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.Test;
import org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider;
import org.netbeans.modules.java.lsp.server.ui.LspIOAccessor;
import org.openide.util.Lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author sdedic
 */
public class NbProcessConsoleTest {
    NbProcessConsole console = new NbProcessConsole(new Consumer<NbProcessConsole.ConsoleMessage>() {
        @Override
        public void accept(NbProcessConsole.ConsoleMessage t) {
        }
    });
    
    @Test
    public void testConsoleClose() throws Exception {
        InputStream sin = console.getStdIn();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(sin, StandardCharsets.UTF_8));
        assertReaderClosed(rdr);
    }
    
    void assertReaderClosed(BufferedReader rdr) throws IOException {
        InputStream sin = console.getStdIn();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean read1 = new AtomicBoolean(false);
        
        // do not wait > 5 secs, abort
        scheduler.schedule(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (!read1.get()) {
                    System.err.println("CLOSED!");
                    sin.close();
                }
                return null;
            }
        }, 5, TimeUnit.SECONDS);

        scheduler.schedule(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                console.stdIn("Hello, world!");
                return null;
            }
        }, 100, TimeUnit.MILLISECONDS);

        assertEquals("Hello, world!", rdr.readLine());
        read1.set(true);
        
        console.stdIn("Still there");
        assertEquals("Still there", rdr.readLine());
        
        // do not wait > 5 secs, abort
        scheduler.schedule(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // close asynchronously
                sin.close();
                return null;
            }
        }, 300, TimeUnit.MILLISECONDS);

        long millis = System.currentTimeMillis();
        assertNull(rdr.readLine());
        long millis2 = System.currentTimeMillis();
        
        assertTrue("Close should be delayed.", millis2 - millis >= 300);
    }
    
    @Test
    public void testCloseLspIOContextInput() throws Exception {
        AbstractLspInputOutputProvider.LspIO io = LspIOAccessor.createIO("test", console, Lookup.EMPTY);
        
        Reader r = LspIOAccessor.reader(io);
        BufferedReader rdr = new BufferedReader(r);
        assertReaderClosed(rdr);
    }
}
