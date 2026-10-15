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
package org.openide.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.*;
import java.util.*;
import javax.swing.JButton;

/**
 *
 * @author Jaroslav Tulach
 */
public class ImageUtilitiesGetLoaderTest extends TestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.util.ImageUtilitiesGetLoaderTest$Lkp");
        
        JButton ignore = new javax.swing.JButton();
        Logger l = Logger.getLogger("");
        Handler[] arr = l.getHandlers();
        for (int i = 0; i < arr.length; i++) {
            l.removeHandler(arr[i]);
        }
        l.addHandler(new ErrMgr());
        l.setLevel(Level.ALL);
        assertEquals(Level.ALL, l.getLevel());
        assertNotNull(ignore);
    }
    
    
    public ImageUtilitiesGetLoaderTest (String testName) {
        super (testName);
    }

    public void testWrongImplOfGetLoaderIssue62194() throws Exception {
        ClassLoader l = ImageUtilities.getClassLoader ();
        assertTrue("Error manager race condition activated", ErrMgr.switchDone);
        assertEquals("c1 the original one", Lkp.c1, l);
        
        ClassLoader n = ImageUtilities.getClassLoader ();
        assertEquals("c2 the new one", Lkp.c2, n);
    }
    
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private org.openide.util.lookup.InstanceContent ic;
        static ClassLoader c1 = new URLClassLoader(new URL[0]);
        static ClassLoader c2 = new URLClassLoader(new URL[0]);
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            this.ic = ic;
            
            turn(c1);
        }
        
        public void turn (ClassLoader c) {
            ArrayList l = new ArrayList();
            l.add(c);
            ic.set (l, null);
        }
    }
    
    
    private static class ErrMgr extends Handler {
        public static boolean switchDone;
        
        public void log (String s) {
            if (s == null) return;

            if (s.startsWith ("Loader computed")) {
                switchDone = true;
                Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault ();
                lkp.turn (Lkp.c2);
            }
        }

        public void publish(LogRecord record) {
            log(record.getMessage());
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
        
    }
    
}
