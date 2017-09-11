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

package org.netbeans.core.startup;

import java.lang.Thread.UncaughtExceptionHandler;
import org.netbeans.TopSecurityManager;
import org.openide.util.Exceptions;

/** The ThreadGroup for catching uncaught exceptions in Corona.
*
* @author   Ian Formanek
*/
final class TopThreadGroup extends ThreadGroup implements Runnable {
    /** The command line args */
    private String[] args;
    /** flag that indicates whether the main thread had finished or not */
    private boolean finished;

    /** Constructs a new thread group. The parent of this new group is
    * the thread group of the currently running thread.
    *
    * @param name the name of the new thread group.
    */
    public TopThreadGroup(String name, String[] args) {
        super(name);
        this.args = args;
    }

    /** Creates a new thread group. The parent of this new group is the
    * specified thread group.
    * <p>
    * The <code>checkAccess</code> method of the parent thread group is
    * called with no arguments; this may result in a security exception.
    *
    * @param parent the parent thread group.
    * @param name the name of the new thread group.
    * @exception  NullPointerException  if the thread group argument is
    *             <code>null</code>.
    * @exception  SecurityException  if the current thread cannot create a
    *             thread in the specified thread group.
    * @see java.lang.SecurityException
    * @see java.lang.ThreadGroup#checkAccess()
    */
    public TopThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!(e instanceof ThreadDeath)) {
            UncaughtExceptionHandler h = Thread.getDefaultUncaughtExceptionHandler();
            if (h != null) {
                h.uncaughtException(t, e);
                return;
            }
            
            if (e instanceof VirtualMachineError) {
                // Try as hard as possible to get a stack trace from e.g. StackOverflowError
                e.printStackTrace();
            }
            System.err.flush();
            Exceptions.printStackTrace(e);
        }
        else {
            super.uncaughtException(t, e);
        }
    }
    
    public synchronized void start () throws InterruptedException {
        Thread t = new Thread (this, this, "main"); // NOI18N
        t.start ();
        
        while (!finished) {
            wait ();
        }
    }

    @Override
    public void run() {
        try {
            Main.start (args);
        } catch (Throwable t) {
            t.printStackTrace();
            // System is probably broken, so don't just sit there with the splash screen open.
            try {
                Thread.sleep(10000);
            } catch (InterruptedException x) {
                Exceptions.printStackTrace(x);
            }
            TopSecurityManager.exit(2);
        } finally {
            synchronized (this) {
                finished = true;
                notify ();
            }
        }
    }
}
