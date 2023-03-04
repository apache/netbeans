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
package org.netbeans.modules.nativeexecution.spi.support;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

public interface JSchAccess {

    public String getServerVersion() throws JSchException;

    public Channel openChannel(String type) throws JSchException, InterruptedException, JSchException;

    public void releaseChannel(Channel channel) throws JSchException;

    public void setPortForwardingR(String bind_address, int rport, String host, int lport) throws JSchException;

    public int setPortForwardingL(int lport, String host, int rport) throws JSchException;

    public void delPortForwardingR(int rport) throws JSchException;

    public void delPortForwardingL(int lport) throws JSchException ;

    public String getConfig(String key);
}
