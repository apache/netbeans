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
package org.netbeans.modules.refactoring.spi;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.impl.CannotRedoRefactoring;
import org.netbeans.modules.refactoring.api.impl.CannotUndoRefactoring;
import org.netbeans.modules.refactoring.api.impl.ProgressSupport;
import org.netbeans.modules.refactoring.spi.BackupFacility2.Handle;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * Default implementation of {@link Transaction}
 * @author Jan Becicka
 * @since 1.23
 */
 public final class RefactoringCommit implements Transaction, ProgressProvider {
 
     private static final Logger LOG = Logger.getLogger(RefactoringCommit.class.getName());
     private ProgressSupport progressSupport;
 
     /**
      * FileObjects modified by this Transaction
      *
      * @return collection of FileObjects
      */
     @NonNull Collection<? extends FileObject> getModifiedFiles() {
         ArrayList<FileObject> result = new ArrayList<>();
         for (ModificationResult modification:results) {
             result.addAll(modification.getModifiedFileObjects());
         }
         return result;
     }
     
 
    List<BackupFacility2.Handle> ids = new ArrayList<BackupFacility2.Handle>();
    private boolean commited = false;
    Collection<? extends ModificationResult> results;
    
    /**
     * RefactoringCommit is just collection of ModificationResults
     * @param results 
     */
    public RefactoringCommit(Collection<? extends ModificationResult> results) {
        this.results = results;
    }
    
     void check(boolean undo) {
         if (!commited) {
             return;
         }
         for (BackupFacility2.Handle id : ids) {
             try {
                 Collection<String> checkChecksum = id.checkChecksum(undo);
                 if (!checkChecksum.isEmpty()) {
                     throw undo ? new CannotUndoRefactoring(checkChecksum) : new CannotRedoRefactoring(checkChecksum);
                 };
             } catch (IOException ex) {
                 Exceptions.printStackTrace(ex);
             }
         }
     }
     
     void sum() {
         if (!commited) {
             return;
         }
         for (BackupFacility2.Handle id : ids) {
            try {
                id.storeChecksum();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
         }
     }
     
    
    @Override
    public void commit() {
        fireProgressListenerStart(ProgressEvent.START, results.size());
        try {
            if (commited) {
                for (BackupFacility2.Handle id:ids) {
                    try {
                        id.restore();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                commited = true;
                for (ModificationResult result : results) {
                    Handle backupid = BackupFacility2.getDefault().backup(result.getModifiedFileObjects());
                    ids.add(backupid);
                    Handle backupid2 = null;
                    if (!result.getNewFiles().isEmpty()) {
                        backupid2 = BackupFacility2.getDefault().backup(result.getNewFiles().toArray(new File[result.getNewFiles().size()]));
                        ids.add(backupid2);
                    }
                    result.commit();
                    backupid.storeChecksum();
                    if (backupid2!=null) {
                        backupid2.storeChecksum();
                    }

                    openNewFiles(result.getNewFiles());
                    fireProgressListenerStep();
                }
            }
        
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        fireProgressListenerStop();
    }
    
     private boolean newFilesStored = false;

    @Override
     public void rollback() {
         for (BackupFacility2.Handle id : ids) {
             try {
                 id.restore();
             } catch (IOException ex) {
                 throw new RuntimeException(ex);
             }
         }
     }

    private static void openNewFiles(Collection<? extends File> newFiles) {
        if (newFiles == null) {
            return;
        }
        for (File file : newFiles) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie editor = dobj.getLookup().lookup(EditorCookie.class);
                    if (editor != null) {
                        editor.open();
                    }
                } catch (DataObjectNotFoundException ex) {
                    // not harmful
                    LOG.log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }
    }

     /**
      * Registers ProgressListener to receive events.
      *
      * @param listener The listener to register.
      * @since 1.33
      */
     @Override
     public synchronized void addProgressListener(ProgressListener listener) {
         if (progressSupport == null) {
             progressSupport = new ProgressSupport();
         }
         progressSupport.addProgressListener(listener);
     }

     /**
      * Removes ProgressListener from the list of listeners.
      *
      * @param listener The listener to remove.
      * @since 1.33
      */
     @Override
     public synchronized void removeProgressListener(ProgressListener listener) {
         if (progressSupport != null) {
             progressSupport.removeProgressListener(listener);
         }
     }
    
    private void fireProgressListenerStart(int type, int count) {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStart(this, type, count);
        }
    }
    
    private void fireProgressListenerStep() {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStep(this);
        }
    }
    
    private void fireProgressListenerStop() {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStop(this);
        }
    }
}
            
