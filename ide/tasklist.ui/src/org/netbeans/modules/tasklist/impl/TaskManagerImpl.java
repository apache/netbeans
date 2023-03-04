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

import java.util.List;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Default implementation of Task Manager
 *
 * @author S. Aubrecht
 */
public class TaskManagerImpl extends TaskManager {
    
    public static final String PROP_SCOPE = "taskScanningScope"; //NOI18N
    
    public static final String PROP_FILTER = "filter"; //NOI18N

    public static final String PROP_WORKING_STATUS = "workingStatus"; //NOI18N
    
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private TaskList taskList = new TaskList();
    private TaskScanningScope scope = Accessor.getEmptyScope();
    private TaskFilter filter = TaskFilter.EMPTY;
    
    private static TaskManagerImpl theInstance;
    
    private final Set<PushTaskScanner> workingScanners = new HashSet<PushTaskScanner>(10);
    private boolean isLoadingFromCache = false;
    private boolean workingStatus = false;
    private Loader loader;

    public static final RequestProcessor RP = new RequestProcessor("TaskList"); //NOI18N

    public static TaskManagerImpl getInstance() {
        if( null == theInstance )
            theInstance = new TaskManagerImpl();
        return theInstance;
    }

    public void observe( final TaskScanningScope newScope, final TaskFilter newFilter ) {
        RP.post( new Runnable() {
            @Override
            public void run() {
                doObserve( newScope, newFilter );
            }
        });
    }
    
    private void doObserve( TaskScanningScope newScope, TaskFilter newFilter ) {
        TaskScanningScope oldScope = scope;
        TaskFilter oldFilter = filter;
        synchronized( this ) {
            if( null == newScope || Accessor.getEmptyScope().equals( newScope ) ) {
                scope.attach( null );
                //turn off
                stopLoading();
                
                workingScanners.clear();
                isLoadingFromCache = false;
                
                //detach simple/file scanners
                for( PushTaskScanner scanner : ScannerList.getPushScannerList().getScanners() ) {
                    scanner.setScope( null, null );
                }
                for( FileTaskScanner scanner : ScannerList.getFileScannerList().getScanners() ) {
                    scanner.attach( null );
                }
                scope = Accessor.getEmptyScope();
                filter = TaskFilter.EMPTY;

                taskList.clear();

                setWorkingStatus(false);
            } else {
                boolean dirtyCache = NbPreferences.forModule(TaskManagerImpl.class).getBoolean("dirtyCache", false);
                NbPreferences.forModule(TaskManagerImpl.class).putBoolean("dirtyCache", false);
                
                //turn on or switch scope/filter
                if( null == newFilter )
                    newFilter = TaskFilter.EMPTY;
                
                if( !scope.equals(newScope) || !filter.equals(newFilter) ) {
                    taskList.clear();
                    
                    if( !newScope.equals( scope ) ) {
                        scope.attach( null );
                        newScope.attach( Accessor.createCallback( this, newScope ) );
                    }
                    
                    workingScanners.clear();
                    isLoadingFromCache = false;
                    setWorkingStatus( false );

                    boolean scannersHaveChanged = compareScanners( filter, newFilter );
                
                    scope = newScope;
                    filter = newFilter;
                    
                    attachFileScanners( newFilter );
                    attachPushScanners( newScope, newFilter );

                    if( scannersHaveChanged || dirtyCache ) {
                        clearCache();
                    } else {
                        startLoading();
                    }
                }
            }
        }
        propertySupport.firePropertyChange( PROP_SCOPE, oldScope, newScope );
        propertySupport.firePropertyChange( PROP_FILTER, oldFilter, newFilter );
    }

    private boolean compareScanners(TaskFilter oldFilter, TaskFilter newFilter) {
        if( null == oldFilter || oldFilter == TaskFilter.EMPTY )
            return false;
        List<FileTaskScanner> oldScanners = ScannerList.getFileScanners(oldFilter);
        List<FileTaskScanner> newScanners = ScannerList.getFileScanners(newFilter);
        if( oldScanners.size() > 0 && oldScanners.size() != newScanners.size() )
            return true;

        for( FileTaskScanner scanner : oldScanners ) {
            if( !newScanners.contains(scanner) )
                return true;
        }
        return false;
    }
    
    private void attachFileScanners( TaskFilter newFilter ) {
        for( FileTaskScanner scanner : getFileScanners() ) {
            if( !newFilter.isEnabled( scanner ) )
                scanner.attach( null );
            else if( newFilter.isEnabled( scanner ) )
                scanner.attach( Accessor.createCallback( this, scanner ) );
        }
    }
    
    private void attachPushScanners( TaskScanningScope newScope, TaskFilter newFilter ) {
        for( PushTaskScanner scanner : getPushScanners() ) {
            if( !newFilter.isEnabled( scanner ) ){
                scanner.setScope( null, null );
            }else if( newFilter.isEnabled( scanner ) ){
                scanner.setScope( newScope, Accessor.createCallback( this, scanner ) );
            }
        }
    }
    
    Iterable<? extends FileTaskScanner> getFileScanners() {
        return ScannerList.getFileScannerList().getScanners();
    }
    
    Iterable<? extends PushTaskScanner> getPushScanners() {
        return ScannerList.getPushScannerList().getScanners();
    }

    public void abort() {
        RP.post( new Runnable() {
            @Override
            public void run() {
                doAbort();
            }
        });
    }

    private void doAbort() {
        stopLoading();

        for( PushTaskScanner scanner : ScannerList.getPushScannerList().getScanners() ) {
            scanner.setScope( null, null );
        }

        workingScanners.clear();
        setWorkingStatus( false );
    }
    
    @Override
    public boolean isObserved() {
        return !Accessor.getEmptyScope().equals( getScope() );
    }

    @Override
    public boolean isCurrentEditorScope(){
        return scope instanceof CurrentEditorScanningScope;
    }
    
    public TaskScanningScope getScope() {
        return scope;
    }
    
    public TaskList getTasks() {
        return taskList;
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        propertySupport.addPropertyChangeListener( listener );
    }
    
    public void addPropertyChangeListener( String propName, PropertyChangeListener listener ) {
        propertySupport.addPropertyChangeListener( propName, listener );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        propertySupport.removePropertyChangeListener( listener );
    }
    
    public void removePropertyChangeListener( String propName, PropertyChangeListener listener ) {
        propertySupport.removePropertyChangeListener( propName, listener );
    }
    
    private void startLoading() {
        if( null != loader )
            loader.cancel();

        loader = new Loader( scope, filter, taskList );
        RP.post(loader);
    }
    
    private void stopLoading() {
        if( null != loader )
            loader.cancel();
        loader = null;
        isLoadingFromCache = false;
        setWorkingStatus(isWorking());
    }
    
    public TaskFilter getFilter() {
        return filter;
    }

    @Override
    public void refresh( final FileTaskScanner scanner, final FileObject... resources) {
        try {
            synchronized( this ) {
                taskList.clear( scanner, resources );
            }
            ArrayList<URL> toRefresh = new ArrayList<URL>(1);
            for( FileObject fo : resources ) {
                toRefresh.clear();
                toRefresh.add(fo.toURL());
                Collection<FileObject> roots = QuerySupport.findRoots(fo, null, null, null);
                for( FileObject root : roots ) {
                    IndexingManager.getDefault().refreshIndex(root.toURL(), toRefresh);
                }
            }
        } catch( IOException ioE ) {
            getLogger().log(Level.INFO, "Error while refreshing files.", ioE);
        }
    }
    
    @Override
    public void refresh( FileTaskScanner scanner ) {
        synchronized( this ) {
            taskList.clear( scanner );
            clearCache();
        }
    }

    public void clearCache() {
        IndexingManager.getDefault().refreshAllIndices(TaskIndexerFactory.INDEXER_NAME);
    }

    void makeCacheDirty() {
        synchronized( this ) {
            NbPreferences.forModule(TaskManagerImpl.class).putBoolean("dirtyCache", true);
        }
    }

    @Override
    public void refresh( final TaskScanningScope scopeToRefresh ) {
        if( this.scope.equals( scopeToRefresh ) ) {
            if (null != loader) {
                loader.cancel();
            }
            RP.post( new Runnable() {
                @Override
                public void run() {
                    doRefresh( scopeToRefresh );
                }
            });
        }
    }
    
    private void doRefresh( TaskScanningScope scopeToRefresh ) {
        synchronized( this ) {
            if( this.scope.equals( scopeToRefresh ) ) {
                taskList.clear();
                if( isObserved() ) {
                    for( PushTaskScanner scanner : ScannerList.getPushScannerList().getScanners() ) {
                        scanner.setScope( null, null );
                        if( getFilter().isEnabled( scanner ) )
                            scanner.setScope( scopeToRefresh, Accessor.createCallback( this, scanner ) );
                    }
                    boolean dirtyCache = NbPreferences.forModule(TaskManagerImpl.class).getBoolean("dirtyCache", false);
                    if (dirtyCache && isCurrentEditorScope()) {
                        cacheCurrentEditorFile();
                    } else {
                        startLoading();
                    }
                }
            }
        }
    }

    @Override
    public void started(PushTaskScanner scanner) {
        synchronized( workingScanners ) {
            workingScanners.add( scanner );
            setWorkingStatus( true );
        }
    }

    @Override
    public void finished(PushTaskScanner scanner) {
        synchronized( workingScanners ) {
            workingScanners.remove( scanner );
            setWorkingStatus( isWorking() );
        }
    }

    @Override
    public void setTasks( PushTaskScanner scanner, FileObject resource, List<? extends Task> tasks ) {
        if( isObserved() && scope.isInScope( resource ) ) {
            try {
                taskList.setTasks(scanner, resource, tasks, filter);
            } catch( IOException ioE ) {
                getLogger().log(Level.INFO, "Error while updating tasks from " + Accessor.getDisplayName(scanner), ioE);
            }
        }
    }

    @Override
    public void setTasks( PushTaskScanner scanner, List<? extends Task> tasks ) {
        if( isObserved() ) {
            try {
                taskList.setTasks(scanner, null, tasks, filter);
            } catch( IOException ioE ) {
                getLogger().log(Level.INFO, "Error while updating tasks from " + Accessor.getDisplayName(scanner), ioE);
            }
        }
    }
    
    @Override
    public void clearAllTasks( PushTaskScanner scanner ) {
        taskList.clear( scanner );
    }
    
    private Logger getLogger() {
        return Logger.getLogger( TaskManagerImpl.class.getName() );
    }
    
    private boolean isWorking() {
        synchronized( workingScanners ) {
            return !workingScanners.isEmpty() || isLoadingFromCache;
        }
    }

    void setLoadingStatus( Loader loader, boolean isLoading ) {
        synchronized( this ) {
            if( this.loader != loader )
                return;
            isLoadingFromCache = isLoading;
        }
        setWorkingStatus(isWorking());
    }
    
    private void setWorkingStatus( boolean newStatus ) {
        synchronized( workingScanners ) {
            if( newStatus != workingStatus ) {
                boolean oldStatus = workingStatus;
                workingStatus = newStatus;
                Logger.getLogger("org.netbeans.log.startup").log(Level.FINE,  // NOI18N
                        newStatus ? "start" : "end", TaskManagerImpl.class.getName()); // NOI18N

                propertySupport.firePropertyChange( PROP_WORKING_STATUS, oldStatus, newStatus );
                //for unit testing
                if( !workingStatus ) {
                    workingScanners.notifyAll();
                }
            }
        }
    }
    /**
     * For unit testing only
     */
    void waitFinished() {
        synchronized( workingScanners ) {
            if( !isWorking() )
                return;
            _waitFinished();
        }
    }
    
    /**
     * For unit testing only
     */
    void _waitFinished() {
        synchronized( workingScanners ) {
            try {
                workingScanners.wait();
            }
            catch( InterruptedException e ) {
                Exceptions.printStackTrace( e );
            }
        }
    }

    private void cacheCurrentEditorFile() {
        Iterator<FileObject> it = scope.iterator();
        FileObject fo;

        if (it.hasNext()) {
            fo = it.next();
        } else {
            return;
        }

        ArrayList<URL> toRefresh = new ArrayList<URL>(1);
        toRefresh.add(fo.toURL());

        Collection<FileObject> roots = QuerySupport.findRoots(fo, null, null, null);
        for (FileObject root : roots) {
            IndexingManager.getDefault().refreshIndex(root.toURL(), toRefresh);
        }
    }
}
