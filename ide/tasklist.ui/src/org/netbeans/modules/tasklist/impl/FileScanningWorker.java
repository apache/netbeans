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

import org.netbeans.modules.tasklist.filter.TaskFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author S. Aubrecht
 */
public class FileScanningWorker implements Runnable {
    
    private TaskList taskList;
    private boolean isCancel = false;
    
    private Set<FileTaskScanner> preparedScanners = new HashSet<FileTaskScanner>();
    
    private Iterator<FileObject> resourceIterator;
    private Queue<FileObject> priorityResourceIterator = new LinkedList<FileObject>();
    private Map<FileObject, Collection<FileTaskScanner>> priorityResource2scanner 
            = new HashMap<FileObject, Collection<FileTaskScanner>>();
    
    private TaskFilter filter;
    
    private final Object SCAN_LOCK = new Object();
    private final Object SLEEP_LOCK = new Object();
    
    /** Creates a new instance of Scanner */
    public FileScanningWorker( TaskList taskList, TaskFilter filter ) {
        this.taskList = taskList;
        this.filter = filter;
    }

    public void scan( Iterator<FileObject> resources, TaskFilter filter ) {
        abort();
        
        synchronized( SLEEP_LOCK ) {
            
            this.filter = filter;
        
            List<? extends FileTaskScanner> providers = ScannerList.getFileScannerList().getScanners();
            for( FileTaskScanner ts : providers ) {
                if( filter.isEnabled( ts ) && !preparedScanners.contains( ts ) ) {
                    ts.notifyPrepare();
                    preparedScanners.add( ts );
                }
            }
            this.resourceIterator = resources;
            
            wakeup();
        }
    }
    
    public void priorityScan( FileTaskScanner scanner, FileObject... res ) {
        boolean wakeupNeeded = false;
        synchronized( SCAN_LOCK ) {

            wakeupNeeded = isCancel || !hasNext();

            if( filter.isEnabled( scanner ) ) {
                if( !preparedScanners.contains( scanner ) ) {
                    scanner.notifyPrepare();
                    preparedScanners.add( scanner );
                }
                for( FileObject rc : res ) {
                    Collection<FileTaskScanner> scanners = priorityResource2scanner.get( rc );
                    if( null == scanners ) {
                        scanners = new ArrayList<FileTaskScanner>( 10 );
                        priorityResource2scanner.put( rc, scanners );
                    }
                    if( !priorityResourceIterator.contains( rc ) ) {
                        priorityResourceIterator.offer( rc );
                    }
                }
            }

        }
        
        if( wakeupNeeded ) {
            wakeup();
        }
    }
    
    public void priorityScan( FileObject... res ) {
        boolean wakeupNeeded = false;
        synchronized( SCAN_LOCK ) {

            wakeupNeeded = isCancel || !hasNext();

            List<? extends FileTaskScanner> scanners = ScannerList.getFileScannerList().getScanners();
            for( FileTaskScanner ts : scanners ) {
                if( filter.isEnabled( ts ) && !preparedScanners.contains( ts ) ) {
                    ts.notifyPrepare();
                    preparedScanners.add( ts );
                }
            }

            for( FileObject rc : res ) {
                priorityResource2scanner.remove( rc );
                if( !priorityResourceIterator.contains( rc ) ) {
                    priorityResourceIterator.offer( rc );
                }
            }

        }
        
        if( wakeupNeeded ) {
            wakeup();
        }
    }
    
    public void run() {
        synchronized( SLEEP_LOCK ) {
            while( true ) {
                try {

                    if( killed ) {
                        return;
                    }

                    Set<FileTaskScanner> scannersToNotify = null;
                    ScanItem item = new ScanItem();
                    ScanMonitor monitor = ScanMonitor.getDefault();
                    while( true ) {
                        monitor.waitEnabled();
                        synchronized( SCAN_LOCK ) {
                            if( getNext( item ) ) {
                                if( !scan( item ) ) {
                                    isCancel = true;
                                }
                            } else {
                                isCancel = true;
                            }
                            if( isCancel ) {
                                scannersToNotify = new HashSet<FileTaskScanner>( preparedScanners );
                            }
                        }

                        if( isCancel ) {
                            break;
                        }
                    }

                    cleanUp( scannersToNotify );

                    try {
                        SLEEP_LOCK.wait();
                    } catch( InterruptedException e ) {
                        //ignore
                    }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        
    }
    
    private void wakeup() {
        synchronized( SLEEP_LOCK ) {
            isCancel = false;
            SLEEP_LOCK.notifyAll();
        }
    }
    
    void abort() {
        isCancel = true;
    }
    
    private boolean killed = false;
    void kill() {
        abort();
        killed = true;
        wakeup();
    }
    
    private List<Task> scannedTasks = new LinkedList<Task>();
    
    private boolean scan( ScanItem item ) {
        if( isCancel )
            return false;

        boolean atLeastOneProviderIsActive = false;

        for( FileTaskScanner scanner : item.scanners ) {
            //check filter for enabled providers
            if( !filter.isEnabled( scanner ) )
                continue;

            //check filter for visible items limit
            if( filter.isTaskCountLimitReached( taskList.countTasks( scanner ) ) )
                continue;

            atLeastOneProviderIsActive = true;
            scannedTasks.clear();

                List<? extends Task> newTasks = null;
                try {
                    if( item.resource.isValid() )
                        newTasks = scanner.scan( item.resource );
                } catch( Throwable e ) {
                    //don't let uncaught exceptions break the thread synchronization
                    Exceptions.printStackTrace( e );
                }
                if (newTasks != null) {
                    scannedTasks.addAll( newTasks );
                }

            if( isCancel ) {
                return false;
            }
            taskList.update( scanner, item.resource, scannedTasks, filter );
        }
        return atLeastOneProviderIsActive;
    }
    
    private void cleanUp( Set<FileTaskScanner> scannersToNotify ) {
        
        synchronized( SCAN_LOCK ) {
            resourceIterator = null;
            priorityResourceIterator.clear();
            priorityResource2scanner.clear();
        }
        notifyFinished( scannersToNotify );
    }
    
    private void notifyFinished( Set<FileTaskScanner> scannersToNotify ) {
        if( null != scannersToNotify ) {
            for( FileTaskScanner ts : scannersToNotify ) {
                ts.notifyFinish();
            }
        }
        preparedScanners.clear();
    }
    
    private boolean getNext( ScanItem item ) {
        item.resource = priorityResourceIterator.poll();
        item.scanners = preparedScanners;

        if( null != item.resource ) {
            item.scanners = priorityResource2scanner.get( item.resource );
            if( null == item.scanners )
                item.scanners = preparedScanners;
        } else if( null != resourceIterator && resourceIterator.hasNext() ) {
            item.resource = resourceIterator.next();
        }
        return null != item.resource;
    }
    
    private boolean hasNext() {
        return priorityResourceIterator.size() > 0
            || (null != resourceIterator && resourceIterator.hasNext() );
    }
    
    private static class ScanItem {
        FileObject resource;
        Collection<FileTaskScanner> scanners;
    }
}
