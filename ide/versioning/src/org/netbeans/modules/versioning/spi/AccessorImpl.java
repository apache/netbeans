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
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.Accessor;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.MessageEditProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.ParentProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.RevisionProvider;

/**
 * Make it possible to hide contructors and factory methods in VCSContext.
 * 
 * @author Maros Sandor
 */
final class AccessorImpl extends Accessor {

    @Override
    public VCSContext createVCSContext(org.netbeans.modules.versioning.core.spi.VCSContext delegate) {
        return new VCSContext(delegate);
    }

    @Override
    public RevisionProvider getRevisionProvider(HistoryEntry entry) {
        return entry.getRevisionProvier();
    }

    @Override
    public MessageEditProvider getMessageEditProvider(HistoryEntry entry) {
        return entry.getMessageEditProvider();
    }

    @Override
    public ParentProvider getParentProvider(HistoryEntry entry) {
        return entry.getParentProvider();
    }
    
    
}
