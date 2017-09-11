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

package org.netbeans.core.lookup;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.Action;
import junit.framework.AssertionFailedError;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.ModuleHistory;
import org.netbeans.SetupHid;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/** Test InstanceDataObject's behavior in conjunction with module
 * installation and uninstallation.
 * Run each test in its own VM, since otherwise tests can affect
 * their siblings (static vars are evil!).
 * @author Jesse Glick
 * @see issue #16327
 */
public abstract class InstanceDataObjectModuleTestHid extends NbTestCase {
    protected ErrorManager ERR;
    
    static {
        org.netbeans.core.startup.MainLookup.register (new ErrManager ());
    }
    
    protected InstanceDataObjectModuleTestHid(String name) {
        super(name);
    }
    
    private ModuleManager mgr;
    protected Module m1, m2;

    protected @Override void runTest() throws Throwable {
        ErrManager.messages.setLength (0);
        ErrManager.log = getLog ();
        try {
            super.runTest();
        } catch (Error err) {
            AssertionFailedError newErr = new AssertionFailedError (err.getMessage () + "\n" + ErrManager.messages);
            newErr.initCause (err);
            throw newErr;
        }
    }
    
    protected @Override void setUp() throws Exception {
        ERR = ErrManager.getDefault().getInstance("TEST-" + getName());
        
        mgr = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        File data = new File(getDataDir(), "lookup");
        File jars = getWorkDir();
        final File jar1 = SetupHid.createTestJAR(data, jars, "test1", null);
        final File jar2 = SetupHid.createTestJAR(data, jars, "test2", null);
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    m1 = mgr.create(jar1, new ModuleHistory(jar1.getAbsolutePath()), false, false, false);
                    if (!m1.getProblems().isEmpty()) throw new IllegalStateException("m1 is uninstallable: " + m1.getProblems());
                    m2 = mgr.create(jar2, new ModuleHistory(jar2.getAbsolutePath()), false, false, false);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
        
        ERR.log("setup finished");
    }
    
    protected @Override void tearDown() throws Exception {
        ERR.log("going to teardown");
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    del(m1);
                    del(m2);
                    return null;
                }
                private void del(Module m) throws Exception {
                    if (m.isEnabled()) {
                        // Test presumably failed halfway.
                        if (m.isAutoload() || m.isEager() || m.isFixed()) {
                            // Tough luck, can't get rid of it easily.
                            return;
                        }
                        mgr.disable(m);
                    }
                    mgr.delete(m);
                }
            });
        } catch (MutexException me) {
            Exception e = me.getException();
            throw e/*new Exception(e + " [Messages:" + ErrManager.messages + "]", e)*/;
        } catch (RuntimeException e) {
            // Debugging for #52689:
            throw e/*new Exception(e + " [Messages:" + ErrManager.messages + "]", e)*/;
        }
        m1 = null;
        m2 = null;
        mgr = null;
    }
    
    protected static final int TWIDDLE_ENABLE = 0;
    protected static final int TWIDDLE_DISABLE = 1;
    protected static final int TWIDDLE_RELOAD = 2;
    protected void twiddle(final Module m, final int action) throws Exception {
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    switch (action) {
                    case TWIDDLE_ENABLE:
                        mgr.enable(m);
                        break;
                    case TWIDDLE_DISABLE:
                        mgr.disable(m);
                        break;
                    case TWIDDLE_RELOAD:
                        mgr.disable(m);
                        mgr.enable(m);
                        break;
                    default:
                        throw new IllegalArgumentException("bad action: " + action);
                    }
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
    }
    
    protected boolean existsSomeAction(Class<?> c) {
        return existsSomeAction(c, Lookup.getDefault().lookupResult(c));
    }
    
    protected boolean existsSomeAction(Class c, Lookup.Result r) {
        assertTrue(Action.class.isAssignableFrom(c));
        boolean found = false;
        ERR.log("Begin iterate");
        Iterator it = r.allInstances().iterator();
        while (it.hasNext()) {
            Action a = (Action)it.next();
            ERR.log("First action read: " + a);
            assertTrue("Assignable to template class: c=" + c.getName() + " a.class=" + a.getClass().getName() +
                "  loaders: c1=" + c.getClassLoader() + " a.class=" + a.getClass().getClassLoader(),
                c.isInstance(a)
            );
            if ("SomeAction".equals(a.getValue(Action.NAME))) {
                found = true;
                ERR.log("Action with correct name found: " + a);
                break;
            }
        }
        ERR.log("existsSomeAction finished: " + found);
        return found;
    }
    
    protected DataObject findIt(String name) throws Exception {
        FileObject fo = FileUtil.getConfigFile(name);
        assertNotNull("Found " + name, fo);
        return DataObject.find(fo);
    }

    protected static void assertSameDataObject (String msg, DataObject obj1, DataObject obj2) {
        assertNotNull (msg, obj1);
        assertNotNull (msg, obj2);
        assertEquals ("They have the same primary file: " + msg, obj1.getPrimaryFile (), obj2.getPrimaryFile ());
        assertSame (msg, obj1, obj2);
    }
    
    protected static final class LookupL implements LookupListener {
        public int count = 0;
        public synchronized void resultChanged(LookupEvent ev) {
            count++;
            notifyAll();
        }
        public synchronized boolean gotSomething() throws InterruptedException {
            ErrorManager.getDefault().log("gotSomething: " + count);
            if (count > 0) return true;
            ErrorManager.getDefault().log("gotSomething: do wait");
            wait(23000);
            ErrorManager.getDefault().log("gotSomething: wait done: " + count);
            return count > 0;
        }
    }
    
    protected static void deleteRec(File f, boolean thistoo) throws IOException {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids == null) throw new IOException("Could not list: " + f);
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i], true);
            }
        }
        if (thistoo && !f.delete()) throw new IOException("Could not delete: " + f);
    }
    
    private static final class ErrManager extends org.openide.ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        public static java.io.PrintStream log = System.err;
        
        private String prefix;
        
        public ErrManager () {
            this ("");
        }
        private ErrManager (String s) {
            prefix = s;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, org.openide.ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance (String name) {
            return new ErrManager (prefix + name);
        }
        
        public void log (int severity, String s) {            
            if (prefix == null) {
                return;
            }
            
            if (messages.length () > 500000) {
                messages.delete (0,  250000);
            }
            messages.append ('['); log.print ('[');
            messages.append (prefix); log.print (prefix);
            messages.append (']'); log.print (']');
            messages.append (s); log.print (s);
            messages.append ('\n'); log.println ();
        }
        
        public void notify (int severity, Throwable t) {
            if (prefix == null) {
                return;
            }
            
            messages.append (t.getMessage ());
            t.printStackTrace (log);
        }
        
    } 
    
}
