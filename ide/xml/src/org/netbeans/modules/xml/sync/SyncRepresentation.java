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
package org.netbeans.modules.xml.sync;

/**
 * Every SynchronizableRepresentation has access to owner
 * Synchronizator.
 *
 * @author  Petr Kuzel
 * @version
 */
public abstract class SyncRepresentation implements Representation {

    private final Synchronizator sync;

    /** Creates new AbstractRepresentation */
    public SyncRepresentation(Synchronizator sync) {
        this.sync = sync;
    }

    protected final Synchronizator getSynchronizator() {
        return sync;
    }

    /**
     * This representation has changed propagate to all others
     * immediatelly.
     */
    protected final void changed(Class klass) {
        sync.representationChanged(klass);
    }
    
    /**
     * @return true, subclasses must handle accordindly.
     */
    public boolean isValid() {
        return true;
    }
}
