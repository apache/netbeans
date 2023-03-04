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

package org.netbeans.api.java.source;

import java.util.EventListener;

/**
 * Listener for changes in {@link ClassIndex}.
* <P>
* When attached to a {ClassIndex} it listens for addition,
* removal and modification of declared types.
* <P>
*
* @see ClassIndex#addClassIndexListener
 * @author Tomas Zezula
 */
public interface ClassIndexListener extends EventListener {
    
    /**
     * Called when the new declared types are added
     * into the {@link ClassIndex}
     * @param event specifying the added types
     */
    public void typesAdded (TypesEvent event);
    
    /**
     * Called when declared types are removed
     * from the {@link ClassIndex}
     * @param event specifying the removed types
     */
    public void typesRemoved (TypesEvent event);
        
    /**
     * Called when some declared types are changed.
     * @param event specifying the changed types
     */
    public void typesChanged (TypesEvent event);
    
    /**
     * Called when new roots are added
     * into the {@link ClassPath} for which the {@link ClassIndex}
     * was created.
     * @param event specifying the added roots
     */
    public void rootsAdded (RootsEvent event);
    
    /**
     * Called when root are removed
     * from the {@link ClassPath} for which the {@link ClassIndex}
     * was created.
     * @param event specifying the removed roots
     */
    public void rootsRemoved (RootsEvent event);
    
}
