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
package org.netbeans.modules.nativeexecution.api.util;

import java.security.acl.NotOwnerException;
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
}
