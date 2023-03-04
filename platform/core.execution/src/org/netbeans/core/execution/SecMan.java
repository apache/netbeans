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
    public void checkPermission(Permission perm) {
        checkPermission(perm, null);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if ("showWindowWithoutWarningBanner".equals(perm.getName())) { // NOI18N
            checkTopLevelWindow(context);
        }
        if (context instanceof AccessControlContext) {
            super.checkPermission(perm, context);
        }
    }

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
