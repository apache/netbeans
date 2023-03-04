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
package org.netbeans.core.startup.preferences;

import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author martin
 */
public class TestNbPreferencesThreading extends NbTestCase {
    
    public TestNbPreferencesThreading(String name) {
        super(name);
    }
    
    public void testThreading() throws Exception {
        Preferences prefs = org.openide.util.NbPreferences.forModule(NbPreferences.class);
        final boolean [] fileEventReceived = new boolean[] { false };
        final boolean [] fileEventBlock1 = new boolean[] { false };
        final boolean [] fileEventBlock2 = new boolean[] { true };
        
        PropertiesStorage.TEST_FILE_EVENT = new Runnable() {
            @Override
            public void run() {
                synchronized (fileEventReceived) {
                    fileEventReceived[0] = true;
                    fileEventReceived.notifyAll();
                }
                try {
                    synchronized (fileEventBlock1) {
                        if (!fileEventBlock1[0]) {
                            fileEventBlock1.wait();
                        }
                    }
                    synchronized (fileEventBlock2) {
                        if (fileEventBlock2[0]) {
                            fileEventBlock2.wait();
                        }
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        prefs.putBoolean("Guest", false);
        assertFalse(prefs.getBoolean("Guest", true));
        synchronized (fileEventReceived) {
            if (!fileEventReceived[0]) {
                fileEventReceived.wait();
            }
            fileEventReceived[0] = false;
        }
        prefs.putBoolean("Guest", true);
        
        assertTrue(prefs.getBoolean("Guest", false));
        
        { // Let process the file event
            synchronized (fileEventBlock1) {
                fileEventBlock1[0] = true;
                fileEventBlock1.notifyAll();
            }
            synchronized (fileEventBlock2) {
                fileEventBlock2[0] = false;
                fileEventBlock2.notifyAll();
            }
            synchronized (fileEventReceived) {
                if (!fileEventReceived[0]) {
                    fileEventReceived.wait();
                }
            }
        } // when done, do the same test again
        
        assertTrue(prefs.getBoolean("Guest", false));
    }
}
