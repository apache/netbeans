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
package org.netbeans.modules.notifications.linux;

import com.sun.jna.Platform;
import static junit.framework.TestCase.assertNotNull;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * Test LinuxNotificationShutdown
 * @author Hector Espert
 */
public class LinuxNotificationShutdownTest extends NbTestCase {

    public LinuxNotificationShutdownTest(String name) {
        super(name);
    }
    
    @Override
    public boolean canRun() {
        return super.canRun() && Platform.LINUX == Platform.getOSType();
    }
    
    @Test
    public void testRun() {
        LinuxNotificationDisplayer linuxNotificationDisplayer = Lookup.getDefault().lookup(LinuxNotificationDisplayer.class);
        assertNotNull(linuxNotificationDisplayer);
        
        if (linuxNotificationDisplayer.notLoaded()) {
            linuxNotificationDisplayer.load();
        }
        
        if (linuxNotificationDisplayer.notStarted()) {
            linuxNotificationDisplayer.start();
        }
        
        assertTrue(linuxNotificationDisplayer.isLoaded());
        assertTrue(linuxNotificationDisplayer.isStarted());
        
        LinuxNotificationShutdown linuxNotificationShutdown = new LinuxNotificationShutdown();
        linuxNotificationShutdown.run();
        
        assertTrue(linuxNotificationDisplayer.isLoaded());
        assertFalse(linuxNotificationDisplayer.isStarted());
    }
    
}
