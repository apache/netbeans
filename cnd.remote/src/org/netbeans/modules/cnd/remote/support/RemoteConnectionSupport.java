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

package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class RemoteConnectionSupport {

    protected final ExecutionEnvironment executionEnvironment;
    private int exit_status;
    private boolean cancelled = false;
    private boolean failed = false;
    private boolean connected = true;
    private String failureReason;

    public RemoteConnectionSupport(ExecutionEnvironment env) {
        this.executionEnvironment = env;
        exit_status = -1; // this is what JSch initializes it to...
        failureReason = "";
        RemoteUtil.LOGGER.log(Level.FINEST, "RCS<Init>: Starting {0} on {1}", new Object[]{getClass().getName(), executionEnvironment});

        if (!ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
            connected = false;
            try {
                ConnectionManager.getInstance().connectTo(executionEnvironment);
                connected = true;
            } catch (IOException ex) {
                RemoteUtil.LOGGER.log(Level.WARNING, "RCS<Init>: Got {0} [{1}]", new Object[]{ex.getClass().getSimpleName(), ex.getMessage()});
                RemoteUtil.LOGGER.log(Level.FINE, "Caused by:", ex);
            } catch (CancellationException ex) {
                cancelled = true;
            }
            if (!ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
                RemoteUtil.LOGGER.log(Level.FINE, "RCS<Init>: Connection failed on {0}", executionEnvironment);
            }
        }
    }

    public static String getMessage(IOException e) {
        String result;
        String reason = e.getMessage();
        if (e instanceof UnknownHostException) {
            result = NbBundle.getMessage(RemoteConnectionSupport.class, "REASON_UnknownHost", e.getMessage());
        } else if (reason.startsWith("Auth fail")) { // NOI18N
            result = NbBundle.getMessage(RemoteConnectionSupport.class, "REASON_AuthFailed");
        } else {
            result = reason;
        }
        return result;
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    //TODO (execution): ???
    public int getExitStatus() {
//        return !cancelled && channel != null ? channel.getExitStatus() : -1; // JSch initializes exit status to -1
        return exit_status;
    }
    
    //TODO (execution): ???
    public boolean isCancelled() {
        return cancelled;
    }
    
    public boolean isConnected(){
        return connected;
    }
    
    //TODO (execution): IMPLEMENT
    public String getFailureReason() {
        return failureReason;
    }
    
    //TODO (execution): IMPLEMENT
    public boolean isFailed() {
        return failed;
    }

    //TODO (execution): ???
    public boolean isFailedOrCancelled() {
        return failed || cancelled;
    }

    //TODO (execution): ???
    public void setFailed(String reason) {
        failed = true;
        failureReason = reason;
    }
    
    protected void setExitStatus(int exit_status) {
        this.exit_status = exit_status;
    }
    

    public String getUser() {
        return executionEnvironment.getUser();
    }

    public String getHost() {
        return executionEnvironment.getHost();
    }
}
