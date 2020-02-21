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

package org.netbeans.modules.cnd.api.model.services;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Project provider for single files.
 *
 * This provider create project for FileObject which is not in the model
 * and return CsmFile for it. This 'dummy' project could be removed
 * if corresponding file is included in the model as a part of any usual project.
 * 
 */
public abstract class CsmStandaloneFileProvider {

    private static final boolean DISABLED = Boolean.getBoolean("cnd.disable.standalone.files");
    /** default instance */
    private static CsmStandaloneFileProvider defaultProvider;

    /** Static method to obtain the provider.
     * @return the provider
     */
    public static synchronized CsmStandaloneFileProvider getDefault() {
        if (defaultProvider == null) {
            if (!DISABLED) {
                defaultProvider = Lookup.getDefault().lookup(CsmStandaloneFileProvider.class);
            }
            if (defaultProvider == null) {
                defaultProvider = new Empty();
            }
        }
        return defaultProvider;
    }

    /**
     *  This method returns CsmFile for this FileObject. The new project will
     *  be created for this file if it is not in the model.
     * @param file FileObject for which CsmFile should be created
     * @return CsmFile for given file or null if it could not be created in the model
     */

    public abstract CsmFile getCsmFile(FileObject file);

    /**
     * This method notifies provider that the editor tab for given file is closed
     *  and file should be removed from model. The provider remoeves this file from
     *  the model.
     * @param file The file which should be removed from model
     */
    public abstract void notifyClosed(CsmFile file);
    
    /**
     * this method checks if file is standalone
     * @param file
     * @return true if passed file is standalone
     */
    public abstract boolean isStandalone(CsmFile file);

    /**
     * A dummy provider that never returns any results.
     */
    private static final class Empty extends CsmStandaloneFileProvider {
        Empty() {
        }

        @Override
        public CsmFile getCsmFile(FileObject file) {
            return null;
        }

        @Override
        public void notifyClosed(CsmFile file) {
        
        }

        @Override
        public boolean isStandalone(CsmFile file) {
            return false;
        }
    } 
}
