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

package org.netbeans.modules.cnd.spi.model.services;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CsmReferenceStorage {
    private static final CsmReferenceStorage DEFAULT = new Default();

    protected CsmReferenceStorage() {
    }

    /** Static method to obtain the Repository.
     * @return the Repository
     */
    public static CsmReferenceStorage getDefault() {
        /*no need for sync synchronized access*/
        return DEFAULT;
    }

    public abstract boolean put(CsmReference ref, CsmObject referencedObject);

    public abstract CsmReference get(CsmOffsetable ref);
    
    /**
     * Implementation of the default selector
     */
    private static final class Default extends CsmReferenceStorage {
        private final Lookup.Result<CsmReferenceStorage> res;
        private static final boolean FIX_SERVICE = true;
        private CsmReferenceStorage fixedStorage;
        Default() {
            res = Lookup.getDefault().lookupResult(CsmReferenceStorage.class);
        }

        private CsmReferenceStorage getService(){
            CsmReferenceStorage service = fixedStorage;
            if (service == null) {
                for (CsmReferenceStorage selector : res.allInstances()) {
                    service = selector;
                    break;
                }
                if (FIX_SERVICE && service != null) {
                    // I see that it is ugly solution, but NB core cannot fix performance of FolderInstance.waitFinished()
                    // Fixed service gives about 3% performance improvement.
                    // I assume that exactly one service implementor exists.
                    fixedStorage = service;
                }
            }
            return service;
        }

        @Override
        public boolean put(CsmReference ref, CsmObject referencedObject) {
            CsmReferenceStorage storage = getService();
            if (storage != null) {
                return storage.put(ref, referencedObject);
            }
            return false;
        }

        @Override
        public CsmReference get(CsmOffsetable ref) {
            CsmReferenceStorage storage = getService();
            if (storage != null) {
                return storage.get(ref);
            }
            return null;
        }
    }
}
