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
package org.netbeans.api.java.classpath;

import java.util.EventListener;

/**
 * Event listener interface for being notified of changes in the set of
 * available paths.
 */
public interface GlobalPathRegistryListener extends EventListener {

    /**
     * Called when some paths are added.
     * Only applies to the first copy of a path that is added.
     * @param event an event giving details
     */
    public void pathsAdded(GlobalPathRegistryEvent event);

    /**
     * Called when some paths are removed.
     * Only applies to the last copy of a path that is removed.
     * @param event an event giving details
     */
    public void pathsRemoved(GlobalPathRegistryEvent event);

}
