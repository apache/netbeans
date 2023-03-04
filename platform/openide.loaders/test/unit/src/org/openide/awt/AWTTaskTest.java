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

package org.openide.awt;

import org.netbeans.modules.openide.loaders.AWTTask;
import java.awt.EventQueue;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AWTTaskTest extends NbTestCase {

    public AWTTaskTest(String name) {
        super(name);
    }

    @Override protected Level logLevel() {
        return Level.INFO; // just do not print the stack trace to stderr
    }

    public void testRun() throws Exception {
        class R implements Runnable {
            @Override public void run() {
                throw new IllegalStateException();
            }
        }
        R run = new R();

        CharSequence log = Log.enable("org.openide.awt", Level.WARNING);
        AWTTask instance = new AWTTask(run, null);

        waitEQ();
        assertTrue("Finished", instance.isFinished());

        if (!log.toString().contains("IllegalStateException")) {
            fail("There should be IllegalStateException:\n" + log);
        }
    }

    static void waitEQ() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override public void run() {
            }
        });
    }

}
