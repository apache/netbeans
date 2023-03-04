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

import java.util.EventListener;


/**
 * An implementation of EventListener allowing to listen o changes of the DocumentModel.
 * This listner is very similar to the {@link DocumentElementListener} but in contrast with it
 * it allows to listen on the entire model, not only on a particullar element.
 *<br>
 * Allows to listen on following changes:
 * <ul>
 * <li>A new element has been added into the model
 * <li>An element has been removed from the model
 * <li>Content of an element has been changed
 * <li>Attributes of an element has changed
 * </ul>
 *
 * @author Marek Fukala
 * @version 1.0
 *
 * @see DocumentElement
 * @see DocumentElementEvent
 * @see DocumentElementListener
 *
 */
public interface DocumentModelListener extends EventListener {
    
    /** fired when a new element has been added into the model. */
    public void documentElementAdded(DocumentElement de);
    
    /** fired when an existing element has been removed from the model. */
    public void documentElementRemoved(DocumentElement de);
    
    /** fired when an element's text content has been changed. */
    public void documentElementChanged(DocumentElement de);
    
    /** fired when attributes of an element have been changed (removed/added/value changed) */
    public void documentElementAttributesChanged(DocumentElement de);
    
}
