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

package org.openide.util;

import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RequestProcessorParallelTest extends NbTestCase {
    public RequestProcessorParallelTest(String s) {
        super(s);
    }

    @RandomlyFails // NB-Core-Build #8322: There shall be a warning about parallel execution(len=0): ''
    public void testParallelExecutionOnDefaultRequestProcessorReported() {
        final RequestProcessor rp = RequestProcessor.getDefault();
        Para p = new Para(rp, 3);

        CharSequence log = Log.enable("org.openide.util.RequestProcessor", Level.WARNING);
        rp.post(p).waitFinished();
        if (log.length() == 0) {
            fail("There shall be a warning about parallel execution(len=" + log.length() + "):\n'" + log + "'");
        }
    }

    public void testParallelExecutionOnOwnRequestProcessorAllowed() {
        final RequestProcessor rp = new RequestProcessor("Mine", 32);
        Para p = new Para(rp, 28);

        CharSequence log = Log.enable("org.openide.util.RequestProcessor", Level.WARNING);
        rp.post(p).waitFinished();
        if (log.length() > 0) {
            fail("There shall be no warnings:\n" + log);
        }
    }

    private static final class Para implements Runnable {
        final RequestProcessor rp;
        final int cnt;

        public Para(RequestProcessor rp, int cnt) {
            this.rp = rp;
            this.cnt = cnt;
        }

        @Override
        public void run() {
            if (cnt > 0) {
                RequestProcessor.Task t = rp.post(new Para(rp, cnt - 1));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                t.waitFinished();
            }
        }
    }
}
