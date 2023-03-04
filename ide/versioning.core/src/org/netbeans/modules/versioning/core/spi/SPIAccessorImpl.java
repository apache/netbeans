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
package org.netbeans.modules.versioning.core.spi;

import java.util.*;
import org.netbeans.modules.versioning.core.SPIAccessor;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry;
import org.openide.filesystems.FileObject;

/**
 * Make it possible to hide contructors and factory methods in VCSContext.
 * 
 * @author Maros Sandor
 */
final class SPIAccessorImpl extends SPIAccessor {

    @Override
    public VCSContext createContextForFiles(Set<VCSFileProxy> files, Set<? extends FileObject> originalFiles) {
        return VCSContext.forFiles(files, originalFiles);
    }
    
    @Override
    public void setLookupObjects(HistoryEntry entry, Object[] lookupObjects) {
        entry.setLookupObjects(lookupObjects);
    }

    @Override
    public Object[] getLookupObjects(HistoryEntry entry) {
        return entry.getLookupObjects();
    }

    @Override
    public void flushCachedContext () {
        VCSContext.flushCached();
    }
}
