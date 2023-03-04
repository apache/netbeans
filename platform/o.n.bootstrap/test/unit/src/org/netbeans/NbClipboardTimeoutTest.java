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
package org.netbeans;

import java.awt.EventQueue;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbClipboardTimeoutTest extends NbTestCase {
    private Clipboard sys;
    private NbClipboard clip;

    public NbClipboardTimeoutTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected void setUp() throws Exception {
        sys = new Clipboard(getName()) {

            @Override
            public synchronized Transferable getContents(Object requestor) {
                try {
                    // every call to this clipboard is slow. Make sure
                    // getContents from clipboardChanged does not call this
                    // method at all.
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return super.getContents(requestor);
            }

        };
        clip = new NbClipboard(sys);
        makeSureSystemClipboardContainsString(sys, clip);
    }
    
    public void testListenerTriesToGetString() throws Throwable {
        class CL implements ClipboardListener {
            String value;
            int cnt;
            IllegalStateException prev;
            
            @Override
            public synchronized void clipboardChanged(ClipboardEvent ev) {
                try {
                    Transferable in = ev.getClipboard().getContents(this);
                    value = (String) in.getTransferData(DataFlavor.stringFlavor);
                    cnt++;
                    if (prev == null) {
                        prev = new IllegalStateException("First modification");
                    } else {
                        prev = new IllegalStateException("Second modification", prev);
                        throw prev;
                    }
                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            synchronized void assertChange(String value) throws Throwable {
                if (cnt != 1) {
                    throw prev;
                }
                
                assertEquals("Expected change", 1, cnt);
                assertEquals("Value is OK", value, this.value);
                cnt = 0;
                prev = null;
            }
        }
        CL cl = new CL();
        clip.addClipboardListener(cl);
        
        Random r = new Random();
        for (int i = 1; i <= 1000; i++) {
            final String value = "value" + i;
            StringSelection ss = new StringSelection(value);
            clip.setContents(ss, ss);
            Thread.sleep(r.nextInt(10) + 1);
            cl.assertChange(value);
        }
    }

    private static void makeSureSystemClipboardContainsString(
        Clipboard sys, NbClipboard clip
    ) throws InterruptedException {
        final CountDownLatch wait = new CountDownLatch(1);
        class FL implements FlavorListener {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                wait.countDown();
            }
        }
        FL fl = new FL();
        sys.addFlavorListener(fl);
        StringSelection ss = new StringSelection("empty");
        clip.setContents(ss, ss);
        wait.await();
    }
    
}
