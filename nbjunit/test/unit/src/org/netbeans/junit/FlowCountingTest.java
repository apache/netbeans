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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.junit;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import junit.framework.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class FlowCountingTest extends NbTestCase {
    Logger LOG;
    CharSequence MSG;

    public FlowCountingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger(getName());
        LOG.setLevel(Level.FINE);
        MSG = Log.enable("", Level.WARNING);
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    public void testFirstPrints5ThenSecond2AndThenFirst6() throws Exception {
        org.netbeans.junit.Log.controlFlow(LOG, Logger.getLogger("global"),
            "THREAD: 1st MSG: cnt: 5" +
            "THREAD: 2nd MSG: cnt: 2" +
            "THREAD: 2nd MSG: cnt: 3" +
            "THREAD: 1st MSG: cnt: 6",
            5000
            );
        Parael.doCount(LOG);

        String msg = MSG.toString();
        // the reason why we check for cnt: 4 here is because of order of Handlers
        // the thread that does the logging of cnt: 5 is blocked sooner than
        // its messages gets into the MSG char sequence...
        int index1 = msg.indexOf("THREAD: 1st MSG: cnt: 4");
        int index2 = msg.indexOf("THREAD: 2nd MSG: cnt: 2");
        int index3 = msg.indexOf("THREAD: 1st MSG: cnt: 6");

        if (index1 == -1) fail("index1 is -1 in: " + msg);
        if (index2 == -1) fail("index2 is -1 in: " + msg);
        if (index3 == -1) fail("index3 is -1 in: " + msg);

        if (index2 < index1) fail("index2[" + index2 + "] < index1[" + index1 + "]: " + msg);
        if (index3 < index2) fail("index3[" + index3 + "] < index2[" + index2 + "]: " + msg);
    }
    
    private static class Parael implements Runnable {
        private Logger log;

        public Parael(Logger log) {
            this.log = log;
        }

        public void run() {
            Random r = new Random();
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(r.nextInt(100));
                } catch (InterruptedException ex) {}
                log.log(Level.WARNING, "cnt: {0}", new Integer(i));
            }
        }
        public static void doCount(Logger log) throws InterruptedException {
            Parael p = new Parael(log);
            Thread t1 = new Thread(p, "1st");
            Thread t2 = new Thread(p, "2nd");
            t1.start(); t2.start();
            t1.join(); t2.join();
        }
    }
    
}
