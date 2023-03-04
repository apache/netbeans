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
package org.netbeans.modules.htmlui.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
import org.netbeans.modules.htmlui.PagesLookup;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Jaroslav Tulach
 */
@Model(className = "CheckContext", properties = {
    @Property(name = "context", type = String.class, array = true)
})
public class HtmlComponentTest {
    private static final CountDownLatch down = new CountDownLatch(1);
    private static Lookup lkp;
    private static BrwsrCtx ctx;

    public HtmlComponentTest() {
    }

    @BeforeClass(timeOut = 9000)
    public static void initializeContext() throws Exception {
        if (!EnsureJavaFXPresent.check()) {
            return;
        }

        final HtmlComponent tc = new HtmlComponent();
        final URL u = HtmlComponent.class.getResource("/org/netbeans/modules/htmlui/impl/empty.html");
        assertNotNull(u, "empty.html found");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tc.loadFX(HtmlComponentTest.class.getClassLoader(), u, HtmlComponentTest::onLoad);
            }
        });
        lkp = tc.getLookup();
    }

    public static Lookup onLoad() {
        ctx = BrwsrCtx.findDefault(HtmlComponentTest.class);
        CheckContext cc = new CheckContext();
        net.java.html.json.Models.applyBindings(cc);
        return new PagesLookup(cc.getClass().getClassLoader(), cc);
    }

    @Test(timeOut = 9000)
    public void updateContext() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        CheckContext cc = assertContext();

        assertNull(lkp.lookup(DefCnstr.class), "No instance yet");
        cc.addContext(DefCnstr.class.getName());
        waitFX();
        assertNotNull(lkp.lookup(DefCnstr.class), "Instance added");
        cc.clearContext();
        waitFX();
        assertNull(lkp.lookup(DefCnstr.class), "Disappeared again");
    }

    @Test(timeOut = 9000)
    public void closedWhenRemoved() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        CheckContext cc = assertContext();

        cc.addContext(ClsblCnstr.class.getName());
        waitFX();
        final ClsblCnstr inst = lkp.lookup(ClsblCnstr.class);
        assertNotNull(inst, "Instance added");
        assertFalse(inst.closed, "No close called yet");
        cc.clearContext();
        waitFX();
        assertNull(lkp.lookup(ClsblCnstr.class), "Disappeared");
        assertTrue(inst.closed, "Close has been called on removal");
    }

    @Test(timeOut = 9000)
    public void updateContextWithNonDefaultCnstr() throws Exception {
        EnsureJavaFXPresent.checkAndThrow();
        CheckContext cc = assertContext();

        assertNull(lkp.lookup(MdlCnstr.class), "No instance yet");
        cc.addContext(MdlCnstr.class.getName());
        waitFX();
        final MdlCnstr mdl = lkp.lookup(MdlCnstr.class);
        assertNotNull(mdl, "Instance added");
        assertEquals(mdl.cc, cc, "Context is passed into the constructor");
        cc.clearContext();
        waitFX();
        assertNull(lkp.lookup(MdlCnstr.class), "Disappeared again");
    }

    private CheckContext assertContext() throws InterruptedException {
        CheckContext cc = null;
        for (int i = 0; i < 100; i++) {
            cc = lkp.lookup(CheckContext.class);
            if (cc != null) {
                break;
            }
            Thread.sleep(100);
        }
        assertNotNull(cc, "Value returned from onLoad is in the lookup");
        return cc;
    }

    @ModelOperation static void addContext(CheckContext cc, String name) {
        cc.getContext().add(name);
    }

    @ModelOperation static void clearContext(CheckContext cc) {
        cc.getContext().clear();
    }

    private static void waitFX() throws Exception {
        final CountDownLatch down = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                down.countDown();
            }
        });
        down.await();
    }

    public static final class DefCnstr {
    }
    public static final class MdlCnstr {
        final CheckContext cc;

        public MdlCnstr(CheckContext cc) {
            this.cc = cc;
        }
    }
    public static final class ClsblCnstr implements Closeable {
        boolean closed;

        @Override
        public void close() throws IOException {
            this.closed = true;
        }
    }
}
