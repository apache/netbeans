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

package org.openide.util.datatransfer;

import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import junit.framework.TestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExClipboardTest extends TestCase {
    private ExClipboard clipboard;

    private ExClipboard.Convertor[] convertors = new ExClipboard.Convertor[0];

    public ExClipboardTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        clipboard = new ExClipboard ("test clipboard") {
            protected ExClipboard.Convertor[] getConvertors () {
                return convertors;
            }
        };
    }

    public void testAddRemoveClipboardListener () {
        
        class L implements ClipboardListener, FlavorListener {
            public int cnt;
            public ClipboardEvent ev;
            public int flavorCnt;
            @Override
            public void clipboardChanged (ClipboardEvent ev) {
                assertFalse("Don't hold locks", Thread.holdsLock(clipboard));
                cnt++;
                this.ev = ev;
            }

            @Override
            public void flavorsChanged(FlavorEvent e) {
                assertFalse("Don't hold locks", Thread.holdsLock(clipboard));
                flavorCnt++;
            }
        }
        L listener = new L ();
        
        clipboard.addClipboardListener (listener);
        clipboard.addFlavorListener(listener);
        StringSelection ss = new StringSelection("");
        clipboard.setContents(ss, ss);
        assertEquals ("One event", 1, listener.cnt);
        assertEquals ("One flavor event", 1, listener.flavorCnt);
        assertNotNull ("An event", listener.ev);
        assertEquals ("source is right", clipboard, listener.ev.getSource ());
        
        clipboard.removeClipboardListener (listener);
        clipboard.fireClipboardChange ();
        
        assertEquals ("no new change", 1, listener.cnt);
    }

    public void testConvert () {
        class WillNotGetNull implements ExClipboard.Convertor {
            public Transferable convert (Transferable t) {
                assertNotNull ("Never get null", t);
                return null;
            }
        }
        
        convertors = new ExClipboard.Convertor[] {
            new WillNotGetNull (),
            new WillNotGetNull (),
            new WillNotGetNull (),
        };
        
        Transferable ret = clipboard.convert (new StringSelection ("Ahoj"));
        assertNull ("Correctly returned null", ret);
        assertNull ("Handle also null parameter", clipboard.convert (null));
    }

}
