/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
                loadTasks( roots.toArray( new FileObject[roots.size()] ), null );
            } else {
                Iterator<FileObject> it = scope.iterator();
                while( it.hasNext() && !cancelled ) {
                    FileObject fo = it.next();
                    List<FileObject> roots = new ArrayList<FileObject>( QuerySupport.findRoots(fo, null, null, null) );
                    loadTasks( roots.toArray( new FileObject[roots.size()] ), fo );
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
