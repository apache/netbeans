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
package org.netbeans.modules.java.lsp.server.progress;

import java.util.EventObject;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.openide.util.Lookup;

/**
 * Contains information on the handle operation. Allows access to some runtime
 * properties of the handle.
 * 
 * @author sdedic
 */
public final class ProgressOperationEvent extends EventObject {
    private final LspInternalHandle progressHandle;

    ProgressOperationEvent(LspInternalHandle progressHandle, Object source) {
        super(source);
        this.progressHandle = progressHandle;
    }

    /**
     * @return the handle instance.
     */
    public InternalHandle getProgressHandle() {
        return progressHandle;
    }
    
    /**
     * Identifies origin of the operation. Returns snapshot of the stacktrace
     * from handle's creation time.
     * @return 
     */
    public StackTraceElement[] getOperationOrigin() {
        return progressHandle.getCreatorTrace();
    }
    
    /**
     * Provides access to the operation's Lookup. The Lookup is saved at the time when the
     * Handle was created. The Lookup contents may help to select the appropriate
     * handle from the active ones.
     * @return 
     */
    public Lookup getOperationLookup() {
        return progressHandle.getOperationLookup();
    }
}
