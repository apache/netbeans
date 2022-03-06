/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.startup;

import java.io.PrintWriter;
import java.io.StringWriter;
import junit.framework.Assert;
import org.netbeans.agent.hooks.api.TrackingHooks;
import org.netbeans.agent.hooks.api.TrackingHooks.Hooks;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
final class CountingSecurityManager extends TrackingHooks {
    private static int cnt;
    private static StringWriter msgs;
    private static PrintWriter pw;
    private static String prefix;
    private static CountingSecurityManager instance;
    
    public static void initialize(String prefix) {
        if (instance == null) {
            TrackingHooks.register(instance = new CountingSecurityManager(), 0, Hooks.IO);
        }
        cnt = 0;
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
        CountingSecurityManager.prefix = prefix;
    }
    
    public static void assertCounts(String msg, int expectedCnt) {
        Assert.assertEquals(msg + "\n" + msgs, expectedCnt, cnt);
        cnt = 0;
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
    }

    @Override
    public void checkFileRead(String file) {
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkRead: " + file);
            new Exception().printStackTrace(pw);
        }
    }

    @Override
    public void checkFileWrite(String file) {
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkWrite: " + file);
        }
    }

}
