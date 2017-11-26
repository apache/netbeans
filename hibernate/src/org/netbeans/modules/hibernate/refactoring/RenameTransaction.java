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

package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.BackupFacility.Handle;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dongmei Cao
 */
public abstract class RenameTransaction implements Transaction {

    private final Set<FileObject> files;
    protected final String origName;
    protected final String newName;
    private final List<Handle> handles = new ArrayList<Handle>();
    private boolean committed;

    public RenameTransaction(Set<FileObject> fileObjects, String oldName, String newName) {
        this.files = fileObjects;
        this.origName = oldName;
        this.newName = newName;
    }
    
    public Set<FileObject> getToBeModifiedFiles() {
        return files;
    }
    
    public String getOriginalName() {
        return this.origName;
    }
    
    public String getNewName() {
        return this.newName;
    }

    public void commit() {
        if (committed) {
            restore();
        } else {
            try {
                handles.add(BackupFacility.getDefault().backup(files));
                doChanges();
                committed = true;
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public void rollback() {
        restore();
    }

    private void restore() {
        for (Handle handle : handles) {
            try {
                handle.restore();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    /**
     * Do the actual changes
     * 
     */
    public abstract void doChanges();
}
