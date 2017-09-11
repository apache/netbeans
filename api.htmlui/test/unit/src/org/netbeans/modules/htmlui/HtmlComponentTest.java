/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
 */
package org.netbeans.modules.htmlui;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
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

    @BeforeClass public static void initializeContext() throws Exception {
        final HtmlComponent tc = new HtmlComponent();
        final URL u = HtmlComponent.class.getResource("/org/netbeans/api/htmlui/empty.html");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tc.loadFX(u, HtmlComponentTest.class, "onLoad");
            }
        });
        lkp = tc.getLookup();
    }
    
    public static Object onLoad() {
        ctx = BrwsrCtx.findDefault(HtmlComponentTest.class);
        CheckContext cc = new CheckContext();
        net.java.html.json.Models.applyBindings(cc);
        return cc;
    }
    
    @Test public void updateContext() throws Exception {
        CheckContext cc = assertContext();
        
        assertNull(lkp.lookup(DefCnstr.class), "No instance yet");
        cc.addContext(DefCnstr.class.getName());
        waitFX();
        assertNotNull(lkp.lookup(DefCnstr.class), "Instance added");
        cc.clearContext();
        waitFX();
        assertNull(lkp.lookup(DefCnstr.class), "Disappeared again");
    }

    @Test public void closedWhenRemoved() throws Exception {
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

    @Test public void updateContextWithNonDefaultCnstr() throws Exception {
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
