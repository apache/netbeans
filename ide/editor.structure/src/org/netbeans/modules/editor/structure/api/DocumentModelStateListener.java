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

package org.netbeans.modules.editor.structure.api;

/**
 * Implementations of this interface may register itself into the DocumentModel 
 * and then listen to changes of the model state.
 * 
 *<br>
 * Allows to listen on following changes of the model state:
 * <ul>
 * <li>The underlaying document has changed.
 * <li>DocumentModel started to scan the underlying document for changes. 
 * (The old model data are available until next step is reached.)
 * <li>The document model update started. 
 * Model is locked for reading since this event. 
 * <li>The document model update finished. 
 * New model data are accessible now.
 * </ul>
 *
 * @author Marek Fukala
 * @version 1.0
 * @since 1.14
 *
 * @see DocumentModel
 * @see DocumentModelListener
 * @see DocumentElement
 * @see DocumentElementListener
 * 
 */
public interface DocumentModelStateListener {

     /** Called when the underlying javax.swing.Document has changed. */
    public void sourceChanged();
    
    /** Indicates the model started to scan the underlying document for changes 
     * happened since last scan and update of the model.
     * The old model elements can be still accessed.
     */
    public void scanningStarted();
    
    /** Called when the DocumentModel update has started. 
     * The model elements are locked for reading until the updateFinished() method 
     * notifies that the model update finished.
     */
    public void updateStarted();
    
    /** Called when the DocumentModel update has finished. 
     * New model data are available now.
     */
    public void updateFinished();
    
}
