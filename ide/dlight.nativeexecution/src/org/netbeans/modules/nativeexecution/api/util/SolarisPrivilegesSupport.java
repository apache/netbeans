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
package org.netbeans.modules.nativeexecution.api.util;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;

/**
 * Supporting class to provide functionality of requesting additional
 * process privileges (see privileges(5) to an execution session.
 * <br>
 * Execution session is either an ssh connection to a remote host or the
 * Runtime.getRuntime() for a localhost.
 * <br>
 * In case of localhost privileges will be granted to the current JVM process;
 * In case of remote - to the remote sshd process.
 * <br>
 * So, once execution session got needed privileges, any submitted task whithin
 * this session will inherit them.
 * <br>
 * To grant requested privileges a root password is needed. Password is prompted
 * but is never stored. So the password is asked for every new execution session.
 *
 */
@Deprecated(forRemoval = true)
public interface SolarisPrivilegesSupport {

    /**
     * Retrieves a list of currently effective execution privileges in the
     * <tt>ExecutionEnvironment</tt>
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to get privileges list from
     * @return a list of currently effective execution privileges
     */
    public List<String> getExecutionPrivileges();

    public void requestPrivileges(
            Collection<String> requestedPrivileges,
            boolean askForPassword) throws NotOwnerException, InterruptedException, CancellationException;

    public boolean requestPrivileges(
            Collection<String> requestedPrivs,
            String user, char[] passwd) throws NotOwnerException, InterruptedException, CancellationException;

    /**
     * Tests whether the <tt>ExecutionEnvironment</tt> has all needed
     * execution privileges.
     * @param execEnv - <tt>ExecutionEnvironment</tt> to be tested
     * @param privs - list of priveleges to be tested
     * @return true if <tt>execEnv</tt> has all execution privileges listed in
     *         <tt>privs</tt>
     */
    public boolean hasPrivileges(Collection<String> privs);

    /**
     * Returns {@link Action javax.swing.Action} that can be invoked in order
     * to request needed execution privileges
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> where to request privileges
     * @param requestedPrivileges a list of execution privileges to request
     * @param onPrivilegesGranted Runnable that is executed on successful
     *        privileges gain
     * @return <tt>Action</tt> that can be invoked in order
     *        to request needed execution privileges
     */
    public AsynchronousAction getRequestPrivilegesAction(
            Collection<String> requestedPrivileges, Runnable onPrivilegesGranted);

    /**
     *  This method is invoked when connection to the ExecutionEnviroment is lost
     */
    public void invalidate();

    public class NotOwnerException extends Exception {
    }
}
