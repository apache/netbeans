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

package org.netbeans.modules.remote.api;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;

/**
 *
 */
public final class ServerRecord implements Lookup.Provider {

    public  static final String PROP_DISPLAY_NAME = "DISPLAY_NAME"; //NOI18N
    private final Lookup lookup;
    private String displayName;

    /*package*/ ServerRecord(ExecutionEnvironment env) {
        lookup = create(env);
        displayName = env.getDisplayName();
    }

    /** 
     * Same as getLookup.lookup(ExecutionEnvironment.class)
     */
    public ExecutionEnvironment getExecutionEnvironment() {
        return getLookup().lookup(ExecutionEnvironment.class);
    }

    /** Use lookup to get additional properties */
    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /** Gets this record user-definable display name */
    public String getDisplayName() {
        return displayName;
    }

    /*package*/ void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Adds property change listener
     * Note that property change listeners will be called in the same thread setDisplayName is called,
     * it can be UI thread -
     * so never spend much time in PropertyChangeListener.propertyChange method
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new NotImplementedException();
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new NotImplementedException();
    }

    private Lookup create(ExecutionEnvironment env) {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }
    
//    /**
//     * Determines whether the record is set up
//     * (record can be not set up, for example, if I clean user dir, but host
//     * is stored somewhere - in project, etc).
//     *
//     * It should work fast.
//     *
//     * It should be called before working with this record.
//     * In the case it returns false, setUp should be called before working this record.
//     * If it returns true, then record can be selected, otherewise it can not.
//     *
//     * @return true in the case record is correctly set up, otherwise false.
//     */
//    public boolean isSetUp() {
//        throw new NotImplementedException();
//    }
//
//    /**
//     * Should be called in the case isSetUp() returned false.
//     * In this case client should call setUp and check return value;
//     * if it returns true, record can be selected, otherewise it can not.
//     *
//     * Setup can take a while; however it's natural to call this method from UI thread.
//     * Implementor carries a responsibility of displaying a modal message dialog
//     * and giving user ability to cancel (in which case the function should return false)
//     *
//     * @return true in the case the record was set up successfully, otherwise false
//     */
//    public void setUp() throws RemoteException, IOException, CancellationException {
//        throw new NotImplementedException();
//    }
}
