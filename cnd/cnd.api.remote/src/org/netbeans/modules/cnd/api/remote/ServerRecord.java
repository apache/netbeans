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

package org.netbeans.modules.cnd.api.remote;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public interface ServerRecord {
    
    public String getServerName();
    
    public String getUserName();

    public boolean isRememberPassword();

    public String getDisplayName();

    /**
     * Gets display name of this record server.
     * In the case display name is user-defined,
     * it returns this name; otherwise
     * it returns getExecutionEnvironment().getHost();
     * @return
     */
    public String getServerDisplayName();

    public ExecutionEnvironment getExecutionEnvironment();

    public boolean isRemote();
    
    public boolean isOnline();

    public boolean isOffline();

    public boolean isDeleted();

    /**
     * Determines whether the record is set up
     * (record can be not set up, for example, if I clean user dir, but host
     * is stored somewhere in project).
     *
     * It should work fast.
     *
     * It should be called before selecting host, say, in project properties.
     * In the case it returns false, setUp should be called before selecting this record.
     * If it returns true, then record can be selected, otherewise it can not.
     *
     * @return true in the case record is correctly set up, otherwise false.
     */
    public boolean isSetUp();

    /**
     * Should be called in the case isSetUp() returned false.
     * In this case client should call setUp and check return value;
     * if it returns true, record can be selected, otherewise it can not.
     *
     * Setup can take a while; however it's natural to call this method from UI thread.
     * Implementor carries a responsibility of displaying a modal message dialog
     * and giving user ability to cancel (in which case the function should return false)
     *
     * @return true in the case the record was set up successfully, otherwise false
     */
    public boolean setUp();
    
    public void validate(boolean force);
    
    /**
     * Setup tools for new restored host
     * 
     * @param task initialization of tools
     */
    public void checkSetupAfterConnection(Runnable task);

    public RemoteSyncFactory getSyncFactory();

    public boolean getX11Forwarding();
    
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);    
    
    public static final String PROP_STATE_CHANGED = "stateChanged"; // NOI18N    
    public static final String DISPLAY_NAME_CHANGED = "displayNameChanged"; // NOI18N    
}
