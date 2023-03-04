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
