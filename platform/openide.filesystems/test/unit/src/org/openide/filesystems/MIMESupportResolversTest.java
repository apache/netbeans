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
package org.openide.filesystems;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.*;
import java.util.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class MIMESupportResolversTest extends TestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupportResolversTest$Lkp");
        Logger.getLogger("").addHandler(new ErrMgr());
        Logger.getLogger("").setLevel(Level.ALL);
    }
    
    
    public MIMESupportResolversTest (String testName) {
        super (testName);
    }

    public void testWrongImplOfGetResolvers() throws Exception {
        MIMEResolver[] all = MIMESupport.getResolvers();
        assertTrue("Error manager race condition activated", ErrMgr.switchDone);
        MIMESupportHid.assertNonDeclarativeResolver("c1 is there", Lkp.c1, all);
        
        all = MIMESupport.getResolvers();
        MIMESupportHid.assertNonDeclarativeResolver("c2 is there", Lkp.c2, all);
    }
    
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private ErrMgr err = new ErrMgr();
        private org.openide.util.lookup.InstanceContent ic;
        static MIMEResolver c1 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            
            public String toString() {
                return "C1";
            }
        };
        static MIMEResolver c2 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            public String toString() {
                return "C2";
            }
        };
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            this.ic = ic;
            
            turn(c1);
        }
        
        public void turn (MIMEResolver c) {
            ArrayList<Object> l = new ArrayList<Object>();
            l.add(err);
            l.add(c);
            ic.set (l, null);
        }
    }
    
    
    private static class ErrMgr extends Handler {
        public static boolean switchDone;

        public ErrMgr() {
            setLevel(Level.ALL);
        }

        public void publish(LogRecord r) {
            String s = r.getMessage();

            if (s.startsWith ("Resolvers computed")) {
                switchDone = true;
                Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault ();
                lkp.turn (Lkp.c2);
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }

    }
    
}
