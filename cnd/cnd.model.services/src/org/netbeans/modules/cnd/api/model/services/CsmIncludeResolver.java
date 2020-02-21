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
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.util.Lookup;

/**
 * Service that checks visibility of CSM object and finds include that makes CSM object visible
 * 
 */
public abstract class CsmIncludeResolver {

    /** A dummy resolver that never returns any results.
     */
    private static final CsmIncludeResolver EMPTY = new Empty();
    /** default instance */
    private static CsmIncludeResolver defaultResolver;
    
    protected CsmIncludeResolver() {
    }

    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmIncludeResolver getDefault() {
        /*no need for sync synchronized access*/
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmIncludeResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }

    /**
     * Checks visibility of CSM object
     * 
     * @param currentFile - current file
     * @param item - CSM object (file as well)
     * @return - visibility of CSM object
     */
    public abstract boolean isObjectVisible(CsmFile currentFile, CsmObject item);

    /**
     * Get compilation unit close to header
     * 
     * @param file - header
     * @return - correspondent source file
     */
    public abstract CsmFile getCloseTopParentFile(CsmFile file);
    
    /**
     * Finds best include directive for CSM object in format 
     * #include "file.h" or #include <file.h> or #include_next <file.h>
     * 
     * @param currentFile - current file
     * @param item - CSM object
     * @return - include directive string
     */
    public abstract String getIncludeDirective(CsmFile currentFile, CsmObject item);

    /**
     * Finds best include directive for CSM object in format 
     * #include "file.h"
     * 
     * @param path - current file path
     * @param item - CSM object
     * @return - include directive string
     */
    public abstract String getLocalIncludeDerectiveByFilePath(FSPath path, CsmObject item);

    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmIncludeResolver {

        Empty() {
        }

        @Override
        public String getIncludeDirective(CsmFile currentFile, CsmObject item) {
            return "";
        }

        @Override
        public boolean isObjectVisible(CsmFile currentFile, CsmObject item) {
            return false;
        }

        @Override
        public String getLocalIncludeDerectiveByFilePath(FSPath path, CsmObject item) {
            return "";
        }

        @Override
        public CsmFile getCloseTopParentFile(CsmFile currentFile) {
            return null;
        }
    }
}
