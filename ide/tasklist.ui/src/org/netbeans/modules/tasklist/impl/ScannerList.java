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
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author S. Aubrecht
 */
public final class ScannerList <T>  {
    
    public static final String PROP_TASK_SCANNERS = "TaskScannerList"; //NOI18N
    
    private static final String SCANNER_LIST_PATH = "TaskList/Scanners"; //NOI18N

    private static ScannerList<FileTaskScanner> fileInstance;
    private static ScannerList<PushTaskScanner> pushInstance;
    
    private PropertyChangeSupport propertySupport;
    
    private Lookup.Result<T> lkpResult;
    
    private List<T> scanners;
    private Class<T> clazz;
    
    /** Creates a new instance of ProviderList */
    private ScannerList( Class<T> clazz ) {
        this.clazz = clazz;
    }

    public static ScannerList<FileTaskScanner> getFileScannerList() {
        if( null == fileInstance ) {
            fileInstance = new ScannerList<FileTaskScanner>( FileTaskScanner.class );
        }
        return fileInstance;
    }

    public static List<FileTaskScanner> getFileScanners(TaskFilter filter) {
        if( null == fileInstance ) {
            fileInstance = new ScannerList<FileTaskScanner>( FileTaskScanner.class );
        }
        if(fileInstance.scanners == null){
           fileInstance.init();
        }
        ArrayList<FileTaskScanner> result = new ArrayList<FileTaskScanner>(fileInstance.scanners.size());
        for( FileTaskScanner scanner : fileInstance.getScanners() ) {
            if( filter.isEnabled(scanner) ) {
                result.add( scanner );
            }
        }
        return result;
    }
    
    public static ScannerList<PushTaskScanner> getPushScannerList() {
        if( null == pushInstance ) {
            pushInstance = new ScannerList<PushTaskScanner>( PushTaskScanner.class );
        }
        return pushInstance;
    }

    public List<? extends T> getScanners() {
        init();
        return scanners;
    }

//    private Class<? extends T> getCheckedClass() {
//        return T.class;
//    }
    
    private void init() {
        if( null == scanners ) {
            if( null == lkpResult ) {
                lkpResult = initLookup();
                lkpResult.addLookupListener( new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        scanners = null;
                        firePropertyChange();
                    }
                });
            }
            scanners = new ArrayList<T>( lkpResult.allInstances() );
        }
    }
    
    public void addPropertyChangeListener( PropertyChangeListener pcl ) {
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.addPropertyChangeListener( pcl );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener pcl ) {
        if( null != propertySupport )
            propertySupport.removePropertyChangeListener( pcl );
    }
    
    private void firePropertyChange() {
        if( null != propertySupport ) {
            propertySupport.firePropertyChange( PROP_TASK_SCANNERS, null, getScanners() );
        }
    }

    private Lookup.Result<T> initLookup() {
        Lookup lkp = Lookups.forPath( SCANNER_LIST_PATH );
        
        Lookup.Template<T> template = new Lookup.Template<T>( clazz );
        Lookup.Result<T> res = lkp.lookup( template );
        return res;
    }
}
