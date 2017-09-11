/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
