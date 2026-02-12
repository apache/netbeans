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

import java.security.Permission;
import org.junit.Assert;

/**
 * Counts the number of File.isDirectory() calls.
 * 
 * @author Pavel Flaska
 */
public class IsDirCntSecurityManager extends SecurityManager {

    private static int cnt;
    private static StringBuffer sb;

    public static void initialize() {
        System.setSecurityManager(new IsDirCntSecurityManager());
        cnt = 0;
        sb = new StringBuffer();
    }

    public static void assertCounts(String msg, int minCount, int maxCount) {
        sb.insert(0, msg);
        sb.append("\n\n\nlimits = <").append(minCount).append(',');
        sb.append(maxCount).append('>').append("; count# = ");
        sb.append(cnt).append(".");
        Assert.assertTrue(sb.toString(), cnt >= minCount && cnt <= maxCount);
        cnt = 0;
    }

    @Override
    public void checkRead(String file) {
        String java = System.getProperty("java.home");
        if (file.startsWith(java)) {
            return;
        }
        
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stack.length - 1; i++) {
            if (stack[i].getClassName().equals(IsDirCntSecurityManager.class.getName())) {
                if (
                    "isDirectory".equals(stack[i + 1].getMethodName()) &&
                    "File.java".equals(stack[i + 1].getFileName())
                ) {
                    // File.isDirectory() has been called? If so, count it in.
                    cnt++;
                    sb.append('\n').append("touch ").append(file);
                    break;
                }
            }
        }
    }

    @Override
    public void checkPermission(Permission perm) {
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }
}
