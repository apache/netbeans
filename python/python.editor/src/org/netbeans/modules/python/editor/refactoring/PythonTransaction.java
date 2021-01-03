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
package org.netbeans.modules.python.editor.refactoring;
/*
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.Transaction;
*/
/* Probably remove completely ;-)
public class PythonTransaction implements Transaction {
    ArrayList<BackupFacility.Handle> ids = new ArrayList<BackupFacility.Handle>();
    private boolean commited = false;
    Collection<ModificationResult> results;

    public PythonTransaction(Collection<ModificationResult> results) {
        this.results = results;
    }

    public void commit() {
        try {
            if (commited) {
                for (BackupFacility.Handle id : ids) {
                    try {
                        id.restore();
                    } catch (IOException ex) {
                        throw (RuntimeException)new RuntimeException().initCause(ex);
                    }
                }
            } else {
                commited = true;
                for (ModificationResult result : results) {
                    ids.add(BackupFacility.getDefault().backup(result.getModifiedFileObjects()));
                    result.commit();
                }
            }

        } catch (IOException ex) {
            throw (RuntimeException)new RuntimeException().initCause(ex);
        }
    }

    public void rollback() {
        for (BackupFacility.Handle id : ids) {
            try {
                id.restore();
            } catch (IOException ex) {
                throw (RuntimeException)new RuntimeException().initCause(ex);
            }
        }
    }
}
*/
