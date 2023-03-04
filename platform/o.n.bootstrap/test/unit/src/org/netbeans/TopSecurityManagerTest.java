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

package org.netbeans;

import junit.framework.TestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class TopSecurityManagerTest extends TestCase {
    
    public TopSecurityManagerTest(String testName) {
        super(testName);
        System.err.println("TopSecurityManagerTest: " + testName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TopSecurityManager.uninstall();
    }
    
    
    public void testCanInstallTwice() {
        TopSecurityManager.install();
        TopSecurityManager.install();
    }
    public void testSecondDirectCallFails() {
        TopSecurityManager.install();
        try {
            System.setSecurityManager(new SecMan());
        } catch (SecurityException ex) {
            // ok
            return;
        }
        fail("Associating own security manager when one is already installed shall not be allowed");
    }

    /* Reenable when assert checkLogger(perm) is added back:
    public void testLoggerCannotBeReset() {
        if (true) return;
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            TopSecurityManager.install();
            SecurityException ex = null;
            try {
                LogManager.getLogManager().reset();
            } catch (SecurityException e) {
                ex = e;
            }
            assertNotNull ("LogManager.reset() should throw a SecurityException",
                    ex);
        }
    }

    public void testLoggerCannotBeReconfigured() throws IOException {
        if (true) return;
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            TopSecurityManager.install();
            SecurityException ex = null;
            try {
                LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(new byte[256]));
            } catch (SecurityException e) {
                ex = e;
            }
            assertNotNull ("LogManager.readConfiguration() should throw a SecurityException",
                    ex);
        }
    }
     */


    private static final class SecMan extends SecurityManager {
    }
}
