/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api;

import java.io.IOException;

/**
 * A {@link NativeProcessBuilder} starts a system process and returns an
 * instance of the {@link NativeProcess} which is a subclass of the
 * {@link Process java.lang.Process}.
 * The differentiator is that this implementation can represent as local as well
 * as remote process, has information about process' PID and about it's
 * {@link NativeProcess.State state}.
 */
public abstract class NativeProcess extends Process {

    /**
     * Returns execution environment of the process.
     *
     * @return execution environment of the process.
     */
    public abstract ExecutionEnvironment getExecutionEnvironment();

    /**
     * Returns PID of the underlaying system process.<br>
     * @return PID of the underlaying system process.
     * @throws IllegalStateException if no PID was obtained prior to method
     *         invocation.
     */
    public abstract int getPID() throws IOException;

    /**
     * Returns the current {@link NativeProcess.State state} of the process.
     * @return current state of the process.
     */
    public abstract State getState();

    /**
     * Returns ProcessInfo (if available), or null
     * @return ProcessInfo
     */
    public abstract ProcessInfo getProcessInfo();

    /**
     * Returns an extended information of the process exit status.
     *
     * Could be null.
     *
     * @return an extended information of the process exit status.
     * @exception IllegalThreadStateException if the subprocess represented by
     * this <code>Process</code> object has not yet terminated.
     */
    public abstract ProcessStatusEx getExitStatusEx();

    /**
     * Enumerates possible states of the {@link NativeProcess}.
     */
    public static enum State {

        /**
         * Native process is in an Initial state. This means that it has not been
         * started yet.
         */
        INITIAL,
        /**
         * Native process is starting. This means that it has been submitted,
         * but no PID is recieved so far.
         */
        STARTING,
        /**
         * Native process runs. This means that process successfully started and
         * it's PID is already known.
         */
        RUNNING,
        /**
         * Native process exited, but exit status is not available yet.
         */
        FINISHING,
        /**
         * Native process exited.
         */
        FINISHED,
        /**
         * Native process submission failed due to some exception.
         */
        ERROR,
        /**
         * Native process forcibly terminated.
         */
        CANCELLED
    }
}
