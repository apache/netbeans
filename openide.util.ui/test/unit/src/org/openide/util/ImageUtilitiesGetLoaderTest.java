/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        ClassLoader l = ImageUtilities.getLoader ();
        assertTrue("Error manager race condition activated", ErrMgr.switchDone);
        assertEquals("c1 the original one", Lkp.c1, l);
        
        ClassLoader n = ImageUtilities.getLoader ();
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
