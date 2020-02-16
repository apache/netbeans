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
package org.netbeans.modules.notifications.linux.jna;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 * Test libnotify integration
 * @author Hector Espert
 */
public class LibnotifyTest extends NbTestCase {
    
    private Libnotify libnotify;

    public LibnotifyTest(String name) {
        super(name);
    }

    @Override
    public boolean canRun() {
        return super.canRun() && Platform.LINUX == Platform.getOSType();
    }

    @Override
    protected void setUp() throws Exception {
        libnotify = Native.load("libnotify.so.4", Libnotify.class);
    }
    
    @Test
    public void testLibnotify() {
        assertFalse(libnotify.notify_is_initted());
        assertTrue(libnotify.notify_init("netbeans_test"));
        assertTrue(libnotify.notify_is_initted());
        
        Pointer notification = libnotify.notify_notification_new("Netbeans test notification", "Netbeans test notification body", null);
        assertNotNull(notification);
        
        assertTrue(libnotify.notify_notification_show(notification, null));
        
        libnotify.notify_uninit();
        assertFalse(libnotify.notify_is_initted());
    }

}
