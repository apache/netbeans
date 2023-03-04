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
