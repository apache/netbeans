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

package org.netbeans.modules.tomcat5.progress;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;


/** Implementation of DeploymentStatus
 *
 * @author  Radim Kubacki
 */
public class Status implements DeploymentStatus {

    /** Value of action type. */
    private ActionType at;

    /** Executed command. */
    private CommandType ct;

    /** Status message. */
    private String msg;
    
    /** Current state. */
    private StateType state;
    
    public Status (ActionType at, CommandType ct, String msg, StateType state) {
        this.at = at;
        this.ct = ct;
        int lastEx;
        if ((lastEx = msg.lastIndexOf ("Exception:")) > 0) {  //NOI18N
            this.msg = msg.substring (lastEx + "Exception:".length());  //NOI18N
        } else {
            this.msg = msg;
        }
        this.state = state;
    }
    
    @Override
    public ActionType getAction () {
        return at;
    }
    
    @Override
    public CommandType getCommand () {
        return ct;
    }
    
    @Override
    public String getMessage () {
        return msg;
    }
    
    @Override
    public StateType getState () {
        return state;
    }
    
    @Override
    public boolean isCompleted () {
        return StateType.COMPLETED.equals (state);
    }
    
    @Override
    public boolean isFailed () {
        return StateType.FAILED.equals (state);
    }
    
    @Override
    public boolean isRunning () {
        return StateType.RUNNING.equals (state);
    }
    
    @Override
    public String toString () {
        return "A="+getAction ()+" S="+getState ()+" "+getMessage ();   // NOI18N
    }
}
