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

package org.openide;

import org.netbeans.junit.*;

/** Test for general ErrorManager functionality.
 *
 * @author Jaroslav Tulach
 */
public class ErrorManagerTest extends NbTestCase {

    public ErrorManagerTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected void setUp () {
        System.setProperty("org.openide.util.Lookup", "org.openide.ErrorManagerTest$Lkp");
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
        ErrManager.clear();
    }
    
    /** Test of getDefault method, of class org.openide.ErrorManager. */
    public void testGetDefault() {
        assertNotNull("There has to be a manager", ErrorManager.getDefault ());
    }
    
    /** Test of notify method, of class org.openide.ErrorManager. */
    public void testNotify() {
        Throwable t = new Throwable ();
        ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, t);
        ErrManager.assertNotify (ErrManager.INFORMATIONAL, t);
        t = new Throwable ();
        ErrorManager.getDefault ().notify (t);
        ErrManager.assertNotify (ErrManager.UNKNOWN, t);
    }
    
    /** Test of log method, of class org.openide.ErrorManager. */
    public void testLog() {
        ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "A text");
        ErrManager.assertLog (ErrorManager.INFORMATIONAL, "A text");
        ErrorManager.getDefault ().log ("Another text");
        ErrManager.assertLog (ErrorManager.INFORMATIONAL, "Another text");
    }
    
    /** Test of isLoggable method, of class org.openide.ErrorManager. */
    public void testIsLoggable() {
        ErrorManager.getDefault ().isLoggable(ErrorManager.INFORMATIONAL);
    }
    
    /** Test of annotate method, of class org.openide.ErrorManager. */
    public void testReturnValues () {
        Throwable t = new Throwable ();
        Throwable value = ErrorManager.getDefault ().annotate(t, ErrorManager.INFORMATIONAL, null, null, null, null);
        assertEquals ("Annotate must return the same exception", t, value);
        
        value = ErrorManager.getDefault ().copyAnnotation (t, new Throwable ());
        assertEquals ("copyAnnotation must return the same exception", t, value);
        
        value = ErrorManager.getDefault ().attachAnnotations(t, new ErrorManager.Annotation[0]);
        assertEquals ("attachAnnotations must return the same exception", t, value);
        
    }
    
    public void testGetInstanceIsInfluencedByChangesOfErrorManagersWeDelegateTo () {
        Lkp.turn (false);
        try {
            ErrorManager man = ErrorManager.getDefault ().getInstance ("hi");
            man.log ("Anything");
            ErrManager.assertLog (-1, null); // no logging because we are disabled
            Lkp.turn (true);
            man.log ("Something");
            ErrManager.assertLog (ErrorManager.INFORMATIONAL, "Something");
        } finally {
            Lkp.turn (true);
        }
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private ErrManager err = new ErrManager ();
        private org.openide.util.lookup.InstanceContent ic;
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (err);
            this.ic = ic;
        }
        
        public static void turn (boolean on) {
            Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault ();
            if (on) {
                lkp.ic.add (lkp.err);
            } else {
                lkp.ic.remove (lkp.err);
            }
        }
    }
    
    //
    // Manager to delegate to
    //
    public static final class ErrManager extends org.openide.ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();

        static void clear() {
            lastText = null;
            lastSeverity = -1;
        }
        
        private String prefix;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public static ErrManager get () {
            return (ErrManager)org.openide.util.Lookup.getDefault ().lookup (ErrManager.class);
        }
        
        @Override
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        @Override
        public Throwable attachAnnotations (Throwable t, org.openide.ErrorManager.Annotation[] arr) {
            return t;
        }
        
        @Override
        public org.openide.ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        @Override
        public org.openide.ErrorManager getInstance (String name) {
            if (
                name.startsWith ("org.netbeans.core.AutomountSupport") ||
                name.startsWith ("org.openide.loaders.FolderList") ||
                name.startsWith ("org.openide.loaders.FolderInstance")
            ) {
                return new ErrManager ('[' + name + ']');
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        @Override
        public void log (int severity, String s) {
            lastSeverity = severity;
            lastText = s;
        }
        
        @Override
        public void notify (int severity, Throwable t) {
            lastThrowable = t;
            lastSeverity = severity;
        }
        private static int lastSeverity;
        private static Throwable lastThrowable;
        private static String lastText;

        public static void assertNotify (int sev, Throwable t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertSame ("Throwable is the same", t, lastThrowable);
            clear();
        }
        
        public static void assertLog (int sev, String t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertEquals ("Text is the same", t, lastText);
            clear();
        }
        
    } 
    
}
