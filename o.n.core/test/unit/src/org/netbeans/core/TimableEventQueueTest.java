/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core;

import java.awt.EventQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class TimableEventQueueTest extends NbTestCase {
    static {
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "300");
        System.setProperty("org.netbeans.core.TimeableEventQueue.pause", "10");
        System.setProperty("org.netbeans.core.TimeableEventQueue.report", "600");
        TimableEventQueue.initialize(null, false);
    }
    private CharSequence log;
    
    
    public TimableEventQueueTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        log = Log.enable(TimableEventQueue.class.getName(), Level.FINE);
    }

    public void testDispatchEvent() throws Exception {
        class Slow implements Runnable {
            private int ok;
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                ok++;
            }
        }
        Slow slow = new Slow();
        
        EventQueue.invokeAndWait(slow);
        EventQueue.invokeAndWait(slow);
        TimableEventQueue.RP.shutdown();
        TimableEventQueue.RP.awaitTermination(3, TimeUnit.SECONDS);
        
        assertEquals("called", 2, slow.ok);

        if (!log.toString().contains("too much time in AWT thread")) {
            fail("There shall be warning about too much time in AWT thread:\n" + log);
        }
    }
}
