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
package org.openide;

import java.security.Permission;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LifecycleManagerTest extends NbTestCase {
    static {
        System.setSecurityManager(new ChokeOnExit());
        System.setProperty("netbeans.full.hack", "true");
    }
    
    
    public LifecycleManagerTest(String n) {
        super(n);
    }

    public void testCallingExitShouldNotReturn() throws Exception {
        try {
            ChokeOnExit.expectedExitCode = 33;
            LifecycleManager.getDefault().exit(33);
            assertEquals(
                "This place should never be reached, before System.exit is called", 
                -1, ChokeOnExit.expectedExitCode
            );
        } catch (Exit ex) {
            assertEquals("The right exit code", 33, ex.exitCode);
        }
    }
    
    private static final class ChokeOnExit extends SecurityManager {
        static int expectedExitCode;
        
        @Override
        public void checkExit(int i) {
            if (expectedExitCode != -1) {
                assertEquals("The right exit code used", expectedExitCode, i);
                expectedExitCode = -1;
                throw new Exit(i);
            }
        }

        @Override
        public void checkPermission(Permission perm) {
        }
    }
    
    private static final class Exit extends SecurityException {
        final int exitCode;

        public Exit(int exitCode) {
            this.exitCode = exitCode;
        }
    }
}
