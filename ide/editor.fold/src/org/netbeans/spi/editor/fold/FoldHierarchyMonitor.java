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
package org.netbeans.spi.editor.fold;

import org.netbeans.api.editor.fold.FoldHierarchy;

/**
 * Allows to initialize fold-based supports when folding is provided for
 * a document or text component. 
 * The Hierarchy always forms around a specific JTextComponent 
 * (see {@link FoldHierarchy#getComponent}. 
 * 
 * @author sdedic
 */
public interface FoldHierarchyMonitor {
    /**
     * Informs that a FoldHierarchy was activated.
     * Note that the notification may be called even multiple times for the same
     * hierarchy and component.
     * 
     * @param h the fold hierarchy
     */
    public void foldsAttached(FoldHierarchy h);
}
