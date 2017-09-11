/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        private final static Object LOCK = new Object();
        private static boolean proceed;
        volatile static int cnt;
        
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
