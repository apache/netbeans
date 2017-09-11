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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.execution.ExecutorTask;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/** Support for Executor beans and for their SysProcess subclasses.
*
* @author Ales Novak
*/
final class DefaultSysProcess extends ExecutorTask {

    /** reference count of instances */
    static int processCount;
    /** reference to SysProcess ThreadGroup */
    private final TaskThreadGroup group;
    /** flag deciding whether is the process destroyed or not */
    private boolean destroyed = false;
    /** InputOutput for this Context */
    private final InputOutput io;
    /** Name */
    private final String name;

    /**
    * @param grp is a ThreadGroup of this process
    */
    public DefaultSysProcess(Runnable run, TaskThreadGroup grp, InputOutput io, String name) {
        super(run);
        group = grp;
        this.io = io;
        this.name = name;
    }

    /** terminates the process by killing all its thread (ThreadGroup) */
    @SuppressWarnings("deprecation")
    public synchronized void stop() {

        if (destroyed) return;
        destroyed = true;
        try {
            group.interrupt();
            RunClassThread runClassThread = group.getRunClassThread();
            if (runClassThread != null) {
                runClassThread.waitForEnd(3000);
            }
        } catch (InterruptedException e) {
            // XXX #209652: thrown consistently when Maven processes finish running; why?
            Logger.getLogger(DefaultSysProcess.class.getName()).log(Level.FINE, null, e);
        } finally {
            group.setRunClassThread(null);
        }
        ExecutionEngine.closeGroup(group);
        group.kill();  // force RunClass thread get out - end of exec is fired
        notifyFinished();
    }

    /** waits for this process is done
    * @return 0
    */
    public int result() {
        // called by an instance of RunClass thread - kill() in previous stop() forces calling thread
        // return from waitFor()
        try {
            group.waitFor();
        } catch (InterruptedException e) {
            return 4; // EINTR
        }
        notifyFinished();
        return 0;
    }

    /** @return an InputOutput */
    public InputOutput getInputOutput() {
        return io;
    }

    public void run() {
    }

    public String getName() {
        return name;
    }
    
    /** destroy the thread group this process was handled from. Not that simple
     * as it seems, since the ThreadGroup can't be destroyed from inside.
     */
    void destroyThreadGroup(ThreadGroup base) {
        new Thread(base, new Destroyer(group)).start();
    }
    private static class Destroyer implements Runnable {
        private final ThreadGroup group;
        Destroyer(ThreadGroup group) {
            this.group = group;
        }
        @Override public void run() {
            try {
                while (group.activeCount() > 0) {
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
            if (!group.isDestroyed()) {
                try {
                    group.destroy();
                } catch (IllegalThreadStateException x) {
                    // #165302: destroyed some other way?
                }
            }
        }
    }

}
