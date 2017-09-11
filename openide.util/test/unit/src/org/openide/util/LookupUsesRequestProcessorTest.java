/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
