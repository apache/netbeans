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
package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.ParentProvider;


/**
 * Make it possible to hide contructors and factory methods in VCSContext.
 * 
 * @author Maros Sandor
 */
public abstract class Accessor {
    
    public static Accessor IMPL;
    public static Accessor VersioningSystemAccessor;
    
    static {
        // invokes static initializer of VCSContext.class
        // that will assign value to the DEFAULT field above
        Class c = VCSContext.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public abstract VCSContext createVCSContext(org.netbeans.modules.versioning.core.spi.VCSContext delegate);
    
    public abstract VCSHistoryProvider.RevisionProvider getRevisionProvider(VCSHistoryProvider.HistoryEntry entry);
    public abstract VCSHistoryProvider.MessageEditProvider getMessageEditProvider(VCSHistoryProvider.HistoryEntry entry);
    public abstract ParentProvider getParentProvider(HistoryEntry entry);
}
