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

package org.netbeans.core.execution;

import java.security.*;

import org.openide.execution.NbClassLoader;

/**
 * A security manager for execution.
 * @author Jesse Glick
 */
public class SecMan extends SecurityManager {

    public static SecurityManager DEFAULT = new SecMan();

    private static Class nbClassLoaderClass = NbClassLoader.class;

    /** thread group of executed classes */
    private ThreadGroup base;

    public SecMan() {
        base = ExecutionEngine.base;
        PrivilegedCheck.init();
        AccController.init();
    }

    @Override
    public void checkExit(int status) throws SecurityException {
        PrivilegedCheck.checkExit(status, this);
    }
    
    final void checkExitImpl(int status, AccessControlContext acc) throws SecurityException {
        IOPermissionCollection iopc;
        iopc = AccController.getIOPermissionCollection(acc);

        if (iopc != null && iopc.grp != null) {
            ExecutionEngine.getTaskIOs().free(iopc.grp, iopc.getIO()); // closes output
            ExecutionEngine.closeGroup(iopc.grp);
            stopTaskThreadGroup(iopc.grp);
            throw new ExitSecurityException("Exit from within execution engine, normal"); // NOI18N
        }

        ThreadGroup g = Thread.currentThread().getThreadGroup ();
        if (g instanceof TaskThreadGroup) {
            throw new ExitSecurityException("Exit from within execution engine, normal"); // NOI18N
        }
        
        if (isNbClassLoader()) {
            throw new ExitSecurityException("Exit from within user-loaded code"); // NOI18N
        }
    }
    
    @SuppressWarnings("deprecation")
    private void stopTaskThreadGroup(TaskThreadGroup old) {
        synchronized(old) {
            int count = old.activeCount();
            int icurrent = -1;
            Thread current = Thread.currentThread();
            Thread[] thrs = new Thread[count];
            old.enumerate(thrs, true);
            for (int i = 0; i < thrs.length; i++) {
                if (thrs[i] == null) break;
                if (thrs[i] == current) icurrent = i;
                else thrs[i].stop();
            }
            if (icurrent != -1) thrs[icurrent].stop();
            //throw new ExitSecurityException();
        }
    }
    
    @Override
    public boolean checkTopLevelWindow(Object window) {
        IOPermissionCollection iopc = AccController.getIOPermissionCollection();
        if (iopc != null && iopc.grp != null && (window instanceof java.awt.Window)) {
            ExecutionEngine.putWindow((java.awt.Window) window, iopc.grp);
        }
        return true;
    }

    /** @return true iff an instance of the NbClassLoader class is on the stack
    */
    protected boolean isNbClassLoader() {
        Class[] ctx = getClassContext();
        ClassLoader cloader;

        for (int i = 0; i < ctx.length; i++) {
            if ((nbClassLoaderClass.isInstance(ctx[i].getClassLoader())) &&
                (ctx[i].getProtectionDomain().getCodeSource() != null)) {
                return true;
            }
        }
        return false;
    }
    
    /** mostly copied from TopSecurityManager */
    private static final class PrivilegedCheck implements PrivilegedExceptionAction<Object> {
        private final int action;
        private final SecMan sm;
        
        // exit
        private int status;
        private AccessControlContext acc;

        public PrivilegedCheck(int action, SecMan sm) {
            this.action = action;
            this.sm = sm;
            
            if (action == 0) {
                acc = AccessController.getContext();
            }
        }

        /** Just to preresolve the class and get it into the start class cache */
        static void init() {
        }
        
        public Object run() throws Exception {
            switch (action) {
                case 0 : 
                    sm.checkExitImpl(status, acc);
                    break;
                default :
            }
            return null;
        }
        
        static void checkExit(int status, SecMan sm) {
            PrivilegedCheck pea = new PrivilegedCheck(0, sm);
            pea.status = status;
            check(pea);
        }
        
        private static void check(PrivilegedCheck action) {
            try {
                AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                Exception orig = e.getException();
                if (orig instanceof RuntimeException) {
                    throw ((RuntimeException) orig);
                }
                orig.printStackTrace();
            }
        }
    }
    
}
