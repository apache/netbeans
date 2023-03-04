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


package org.netbeans.core.windows;


import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;

import java.util.Iterator;
import java.util.Set;


/**
 * Class representing group of TopComponents. Those TopComponents belong together
 * in the sense they can be opened or closed at one step.
 *
 * @author  Peter Zavadsky
 */
public class TopComponentGroupImpl implements TopComponentGroup {


    /** Creates a new instance of TopComponentGroup */
    public TopComponentGroupImpl(String name) {
        this(name, false);
    }
    
    public TopComponentGroupImpl(String name, boolean opened) {
        getCentral().createGroupModel(this, name, opened);
    }
    

    public void open() {
        WindowManagerImpl.assertEventDispatchThread();
        
        getCentral().openGroup(this);
    }
    
    public void close() {
        WindowManagerImpl.assertEventDispatchThread();
        
        getCentral().closeGroup(this);
    }
    
    public Set<TopComponent> getTopComponents() {
        return getCentral().getGroupTopComponents(this);
    }

    
    public String getName() {
        return getCentral().getGroupName(this);
    }
    
    public boolean isOpened() {
        return getCentral().isGroupOpened(this);
    }
    
    public Set<TopComponent> getOpeningSet() {
        return getCentral().getGroupOpeningTopComponents(this);
    }
    
    public Set getClosingSet() {
        return getCentral().getGroupClosingTopComponents(this);
    }

    public boolean addUnloadedTopComponent(String tcID) {
        return getCentral().addGroupUnloadedTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedTopComponent(this, tcID);
    }
    
    public boolean addUnloadedOpeningTopComponent(String tcID) {
        return getCentral().addGroupUnloadedOpeningTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedOpeningTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedOpeningTopComponent(this, tcID);
    }
    
    public boolean addUnloadedClosingTopComponent(String tcID) {
        return getCentral().addGroupUnloadedClosingTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedClosingTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedClosingTopComponent(this, tcID);
    }
    
    // XXX
    /** Just for persistence management. */
    public boolean addGroupUnloadedOpenedTopComponent(String tcID) {
        return getCentral().addGroupUnloadedOpenedTopComponent(this, tcID);
    }
    
    public Set getGroupOpenedTopComponents() {
        return getCentral().getGroupOpenedTopComponents(this);
    }
    
    // XXX>>
    public Set<String> getTopComponentsIDs() {
        return getCentral().getGroupTopComponentsIDs(this);
    }
    
    public Set<String> getOpeningSetIDs() {
        return getCentral().getGroupOpeningSetIDs(this);
    }
    
    public Set<String> getClosingSetIDs() {
        return getCentral().getGroupClosingSetIDs(this);
    }
    
    public Set<String> getGroupOpenedTopComponentsIDs() {
        return getCentral().getGroupOpenedTopComponentsIDs(this);
    }
    // XXX<<
    
    private Central getCentral() {
        return WindowManagerImpl.getInstance().getCentral();
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        for(Iterator it = getTopComponents().iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            buff.append("\n\t" + tc.getClass().getName() + "@" + Integer.toHexString(tc.hashCode()) // NOI18N
                + "[name=" + tc.getName() // NOI18N
                + ", openFlag=" + getOpeningSet().contains(tc) // NOI18N
                + ", closeFlag=" + getClosingSet().contains(tc) + "]"); // NOI18N
        }
        
        return super.toString() + "[topComponents=[" + buff.toString() + "\n]]"; // NOI18N
    }

}
