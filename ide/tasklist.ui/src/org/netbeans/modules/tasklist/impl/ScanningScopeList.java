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

package org.netbeans.modules.tasklist.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author S. Aubrecht
 */
public final class ScanningScopeList {
    
    public static final String PROP_SCOPE_LIST = "scopeList"; //NOI18N
    
    private static final String SCOPE_LIST_PATH = "TaskList/ScanningScopes"; //NOI18N
    
    private static ScanningScopeList theInstance;
    
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private Lookup.Result<TaskScanningScope> lookupRes;
    
    /** Creates a new instance of ScanningScopeList */
    private ScanningScopeList() {
    }
    
    public static ScanningScopeList getDefault() {
        if( null == theInstance ) {
            theInstance = new ScanningScopeList();
        }
        return theInstance;
    }
    
    public void addPropertyChangeListener( PropertyChangeListener pcl ) {
        propertySupport.addPropertyChangeListener( pcl );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener pcl ) {
        propertySupport.removePropertyChangeListener( pcl );
    }
    
    public List<TaskScanningScope> getTaskScanningScopes() {
        if( null == lookupRes ) {
            lookupRes = initLookup();
            lookupRes.addLookupListener( new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    fireScopeListChange();
                }
            });
        }
        return new ArrayList<TaskScanningScope>( lookupRes.allInstances() );
    }
    
    public TaskScanningScope getDefaultScope() {
        List<TaskScanningScope> scopes = getTaskScanningScopes();
        for( TaskScanningScope ss : scopes ) {
            if( Accessor.isDefault( ss ) ) {
                return ss;
            }
        }
        return scopes.isEmpty() ? null : scopes.get( 0 );
    }
    
    private Lookup.Result<TaskScanningScope> initLookup() {
        Lookup lkp = Lookups.forPath( SCOPE_LIST_PATH );
        Lookup.Template<TaskScanningScope> template = new Lookup.Template<TaskScanningScope>( TaskScanningScope.class );
        Lookup.Result<TaskScanningScope> res = lkp.lookup( template );
        return res;
    }
    
    private void fireScopeListChange() {
        propertySupport.firePropertyChange( PROP_SCOPE_LIST, null, getTaskScanningScopes() );
    }
}
