/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
         ArrayList<FileObject> result = new ArrayList();
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
            
