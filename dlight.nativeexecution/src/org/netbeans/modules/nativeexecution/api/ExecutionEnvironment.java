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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api;

import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;

/**
 * The configuration of the environment for a {@link NativeProcess} execution.
 * ExecutionEnvirenment is about "<b>where</b>" to start a native proccess.
 */
public interface ExecutionEnvironment {

    /**
     * Returns host identification string.
     * @return the same host name/ip string that was used for this
     * <tt>ExecutionEnvironment</tt> creation.
     */
    public String getHost();

    /**
     * Returns host address to be used to initiate connection with this
     * ExecutionEnvironment.
     * This method always returns VALID address. Method could be slow because it
     * validates the address.
     * @return up-to-date host address to be used to initiate connection with
     * the ExecutionEnvironment
     */
    public String getHostAddress();

    /**
     * Gets a string representation of the environment to show in the UI
     * @return a string representation of the environment for showing in UI
     */
    public String getDisplayName();

    /**
     * Returns string representation of this <tt>ExecutionEnvironment</tt> in
     * form <tt>user@host[:port]</tt>.
     * @return string representation of this <tt>ExecutionEnvironment</tt> in
     *         form user@host[:port]
     */
    @Override
    public String toString();

    /**
     * Returns username that is used for ssh connection.
     * @return username for ssh connection establishment.
     */
    public String getUser();

    /**
     * Returns port number that is used for ssh connection.
     * @return port that is used for ssh connection in this environment. 
     * <tt>0</tt> means that no ssh connection is required for this environment.
     */
    public int getSSHPort();

    /**
     * Returns true if ssh connection is required for this environment.
     *
     * Generally, this means that host itself could be a localhost, but if
     * sshPort is set, it will be treated as a remote one.
     *
     * @return true if ssh connection is required for this environment.
     * @see #isLocal() 
     *
     */
    public boolean isRemote();

    /**
     * Returns true if no ssh connection is required to start execution in this
     * environment. I.e. it returns <tt>true</tt> if host is the localhost and
     * no sshPort is specified for this environment.
     * @return true if no ssh connection is required for this environment.
     * @see #isRemote() 
     */
    public boolean isLocal();

    /**
     * Returns true if <tt>obj</tt> represents the same
     * <tt>ExecutionEnvironment</tt>. Two execution environments are equal if
     * and only if <tt>host</tt>, <tt>user</tt> and <tt>sshPort</tt> are all
     * equal. If <tt>host</tt> refers to the localhost in both environments but
     * different host identification strings were used while creation
     * (i.e. <tt>localhost</tt>; <tt>127.0.0.1</tt>; hostname or it's real IP 
     * address) <tt>host</tt>s are still treated as to be equal.
     *
     * @param obj object to compare with
     * @return <tt>true</tt> if this <tt>ExecutionEnvironment</tt> equals to
     * <tt>obj</tt> or not.
     */
    @Override
    public boolean equals(Object obj);

    /**
     *
     * @throws IOException
     * @throws CancellationException
     */
    public void prepareForConnection() throws IOException, CancellationException;
}
