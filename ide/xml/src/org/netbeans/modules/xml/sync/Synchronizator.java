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
 * Synchronization manager methods.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface Synchronizator {

    /** Tree representation has changed, update text accordingly. */
    public void representationChanged(Class type);

    /** @return state of this sync interface, true if just syncing. */
    public boolean isInSync();

    /**
     * A new model appeared.
     * It may my change result returned by getRepresentations().
     */
    public void addRepresentation(Representation rep);
    
    /**
     * Some model disppeared.
     * It may my change result returned by getRepresentations().
     */
    public void removeRepresentation(Representation rep);
    
    /**
     * Prior creating new representation we should query for primary one
     * and create the new one from the primary one.
     */
    public Representation getPrimaryRepresentation();
    
    
    /**
     * Allow to perform a task that is treated as ordinary synchronization request
     */
    public void postRequest(Runnable request);

}
