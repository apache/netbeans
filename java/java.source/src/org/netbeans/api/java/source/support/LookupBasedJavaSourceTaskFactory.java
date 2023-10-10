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
package org.netbeans.api.java.source.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;

/**A {@link JavaSourceTaskFactory} that registers tasks to all files that are
 * found in the given {@link Lookup}.
 *
 * This factory searches for {@link FileObject}, {@link DataObject} and {@link Node}
 * in the lookup. If {@link Node}(s) are found, its/their lookup is searched for
 * {@link FileObject} and {@link DataObject}.
 *
 * @author Jan Lahoda
 */
public abstract class LookupBasedJavaSourceTaskFactory extends JavaSourceTaskFactory {

    private Result<FileObject> fileObjectResult;
    private Result<DataObject> dataObjectResult;
    private Result<Node> nodeResult;
    
    private List<FileObject> currentFiles;
    private LookupListener listener;
    
    private String[] supportedMimeTypes;

    /**Construct the LookupBasedJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    public LookupBasedJavaSourceTaskFactory(Phase phase, Priority priority) {
        this(phase, priority, (String[]) null);
    }
    
    /**Construct the LookupBasedJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @since 0.21
     */
    public LookupBasedJavaSourceTaskFactory(Phase phase, Priority priority, String... supportedMimeTypes) {
        super(phase, priority, TaskIndexingMode.DISALLOWED_DURING_SCAN);
        currentFiles = Collections.emptyList();
        listener = new LookupListenerImpl();
        this.supportedMimeTypes = supportedMimeTypes != null ? supportedMimeTypes.clone() : null;
    }
    
    /**Construct the LookupBasedJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param taskIndexingMode the awareness of indexing. For tasks which can run
     * during indexing use {@link TaskIndexingMode#ALLOWED_DURING_SCAN} for tasks
     * which cannot run during indexing use {@link TaskIndexingMode#DISALLOWED_DURING_SCAN}.
     * @param supportedMimeTypes a list of mime types on which the tasks created by this factory should be run,
     * empty array falls back to default text/x-java.
     * @since 0.94
     */
    public LookupBasedJavaSourceTaskFactory(
            @NonNull final Phase phase,
            @NonNull final Priority priority,
            @NonNull final TaskIndexingMode taskIndexingMode,
            @NonNull final String... supportedMimeTypes) {
        super(phase, priority, taskIndexingMode);
        Parameters.notNull("supportedMimeTypes", supportedMimeTypes);   //NOI18N
        currentFiles = Collections.emptyList();
        listener = new LookupListenerImpl();
        this.supportedMimeTypes = supportedMimeTypes.length > 0 ? supportedMimeTypes.clone() : null;
    }

    /**Sets a new {@link Lookup} to search.
     *
     * @param lookup new {@link Lookup}
     */
    protected final synchronized void setLookup(Lookup lookup) {
        if (fileObjectResult != null) {
            fileObjectResult.removeLookupListener(listener);
        }
        if (dataObjectResult != null) {
            dataObjectResult.removeLookupListener(listener);
        }
        if (nodeResult != null) {
            nodeResult.removeLookupListener(listener);
        }
        fileObjectResult = lookup.lookupResult(FileObject.class);
        dataObjectResult = lookup.lookupResult(DataObject.class);
        nodeResult = lookup.lookupResult(Node.class);

        fileObjectResult.addLookupListener(listener);
        dataObjectResult.addLookupListener(listener);
        nodeResult.addLookupListener(listener);

        updateCurrentFiles();
        fileObjectsChanged();
    }

    private synchronized void updateCurrentFiles() {
        Set<FileObject> newCurrentFiles = new HashSet<FileObject>();

        newCurrentFiles.addAll(fileObjectResult.allInstances());

        for (DataObject d : dataObjectResult.allInstances()) {
            newCurrentFiles.add(d.getPrimaryFile());
        }

        for (Node n : nodeResult.allInstances()) {
            newCurrentFiles.addAll(n.getLookup().lookupAll(FileObject.class));

            for (DataObject d : n.getLookup().lookupAll(DataObject.class)) {
                newCurrentFiles.add(d.getPrimaryFile());
            }
        }

        List<FileObject> newCurrentFilesFiltered = OpenedEditors.filterSupportedMIMETypes(new LinkedList<FileObject>(newCurrentFiles), supportedMimeTypes);
        
        if (!currentFiles.equals(newCurrentFilesFiltered)) {
            currentFiles = newCurrentFilesFiltered;
            lookupContentChanged();
        }
    }
    
    /**{@inheritDoc}*/
    public synchronized List<FileObject> getFileObjects() {
        return currentFiles;
    }

    /**This method is called when the provided Lookup's content changed.
     * Subclasses may override this method in order to be notified about such change.
     */
    protected void lookupContentChanged() {
    }

    private class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            updateCurrentFiles();
            fileObjectsChanged();
        }
    }

}
