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

package org.netbeans.modules.uihandler;

import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class BugTriggersTest extends NbTestCase {
    private static Installer o;
    
    public BugTriggersTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.FINE;
    }
    
    protected void setUp() throws Exception {
        Installer.findObject(Installer.class, true).restored();
    }

    protected void tearDown() throws Exception {
        Installer.findObject(Installer.class, true).uninstalled();
    }
    
    public void testRootLoggerHasHandler() throws Exception {
        for (Handler h : Logger.getLogger("").getHandlers()) {
            if (h instanceof Callable) {
                return;
            }
        }
        fail("No handler which implements Callable");
    }
 }
