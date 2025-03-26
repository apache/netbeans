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

package org.netbeans.modules.tasklist.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;

/**
 * Load cached tasks for given scope and given filter.
 * 
 * @author S. Aubrecht
 */
public class Loader implements Runnable, Cancellable {
    
    private boolean cancelled = false;
    private final TaskScanningScope scope;
    private final TaskFilter filter;
    private final TaskList taskList;

    public Loader( TaskScanningScope scope, TaskFilter filter, TaskList taskList ) {
        this.scope = scope;
        this.filter = filter;
        this.taskList = taskList;
    }

    public void run() {
        TaskManagerImpl.getInstance().setLoadingStatus(this, true);
        try {
            Collection<? extends Project> projectsInScope = scope.getLookup().lookupAll(Project.class);
            if( !projectsInScope.isEmpty() ) {
                LinkedList<FileObject> roots = new LinkedList<FileObject>();
                for( Project p : projectsInScope ) {
                    if( cancelled )
                        return;
                    roots.addAll( QuerySupport.findRoots(p, null, null, null) );
                }
                loadTasks(roots.toArray(new FileObject[0]), null );
            } else {
                Iterator<FileObject> it = scope.iterator();
                while( it.hasNext() && !cancelled ) {
                    FileObject fo = it.next();
                    List<FileObject> roots = new ArrayList<FileObject>( QuerySupport.findRoots(fo, null, null, null) );
                    loadTasks(roots.toArray(new FileObject[0]), fo );
                }
            }
        } finally {
            TaskManagerImpl.getInstance().setLoadingStatus(this, false);
        }
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

    private void loadTasks( FileObject[] roots, FileObject resource ) {
        ArrayList<Task> loadedTasks = null;
        try {
            QuerySupport qs = QuerySupport.forRoots(TaskIndexerFactory.INDEXER_NAME,
                    TaskIndexerFactory.INDEXER_VERSION, roots);

            for( FileTaskScanner scanner : ScannerList.getFileScannerList().getScanners() ) {

                if( cancelled )
                    return;

                if( !filter.isEnabled(scanner) )
                    continue;

                String scannerId = ScannerDescriptor.getType( scanner );
                Collection<? extends IndexResult> cache = qs.query(TaskIndexer.KEY_SCANNER, scannerId, QuerySupport.Kind.EXACT, TaskIndexer.KEY_TASK);
                for( IndexResult ir : cache ) {
                    if( cancelled )
                        return;
                    FileObject fo = ir.getFile();
                    if( null == fo )
                        continue;
                    if( null != resource && !resource.equals(ir.getFile()) )
                        continue;
                    if( null != loadedTasks )
                        loadedTasks.clear();
                    String[] tasks = ir.getValues(TaskIndexer.KEY_TASK);
                    for( String encodedTask : tasks ) {
                        if( cancelled )
                            return;

                        Task t = TaskIndexer.decode(fo, encodedTask);
                        if( null == loadedTasks ) {
                            loadedTasks = new ArrayList<Task>(1000);
                        }
                        loadedTasks.add(t);
                    }
                    if( cancelled )
                        return;
                    if( null != loadedTasks && !loadedTasks.isEmpty() )
                        taskList.update(scanner, fo, loadedTasks, filter);
                }
            }
        } catch( IOException ioE ) {
            Logger.getLogger(TaskIndexer.class.getName()).log(Level.INFO,
                    "Error while loading tasks from cache", ioE);
        }
    }
}
