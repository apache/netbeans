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

package org.netbeans.junit.internal;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;

@RandomlyFails // NB-Core-Build #3539
public class NbModuleLogHandlerTest extends NbTestCase {

    public NbModuleLogHandlerTest(String n) {
        super(n);
    }

    public static Test suite() {
        // XXX maybe simpler to disable org.netbeans.core hence NotifyExcPanel, but enableClasspathModules(false) does nothing
        // (perhaps these properties should be set automatically by NbModuleSuite and/or NbTestCase?)
        System.setProperty("netbeans.exception.alert.min.level", "999999");
        System.setProperty("netbeans.exception.report.min.level", "999999");
        return NbModuleSuite.createConfiguration(NbModuleLogHandlerTest.class).gui(false).failOnException(Level.WARNING).suite();
    }

    public void testIgnoreOutOfMemoryErrorFromAssertGC() throws Exception {
        new Thread("background") {
            public @Override void run() {
                // try to get OOME reported
                while (true) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    try {
                        Object o = new Object[999999];
                    } catch (Throwable t) {
                        Logger.getLogger(NbModuleLogHandlerTest.class.getName()).log(Level.WARNING, null, t);
                    }
                }
            }
        }.start();
        try {
            assertGC("this will fail", new WeakReference<Object>("interned"));
            throw new IllegalStateException("should not succeed");
        } catch (AssertionFailedError x) {
            assertNotNull("Expected exception", x);
        }
    }

}
