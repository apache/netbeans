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

package org.netbeans;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Permission;
import org.junit.Assert;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
final class CountingSecurityManager extends SecurityManager {
    private static int cnt;
    private static StringWriter msgs;
    private static PrintWriter pw;
    private static String prefix;
    
    public static void initialize(String prefix) {
        if (System.getSecurityManager() instanceof CountingSecurityManager) {
            // ok
        } else {
            System.setSecurityManager(new CountingSecurityManager());
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
    public void checkRead(String file) {
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkRead: " + file);
            new Exception().printStackTrace(pw);
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkRead2: " + file);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        cnt++;
        pw.println("Fd: " + fd);
    }

    @Override
    public void checkWrite(String file) {
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkWrite: " + file);
        }
    }

    @Override
    public void checkPermission(Permission perm) {
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }
}
