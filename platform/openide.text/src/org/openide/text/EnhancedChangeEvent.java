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
package org.openide.text;

import javax.swing.event.ChangeEvent;
import javax.swing.text.StyledDocument;


/** Extension of ChangeEvent with additional information
 * about document and the state of the document. This
 * event is fired from CloneableEditorSupport
 *
 * @author  David Konecny, Jaroslav Tulach
 */
final class EnhancedChangeEvent extends ChangeEvent {
    /** Whether document is being closed */
    private boolean closing;

    /** Reference to document */
    private StyledDocument doc;

    public EnhancedChangeEvent(Object source, StyledDocument doc, boolean closing) {
        super(source);
        this.doc = doc;
        this.closing = closing;
    }

    /** Whether document is being closed */
    public boolean isClosingDocument() {
        return closing;
    }

    /** Getter for document */
    public StyledDocument getDocument() {
        return doc;
    }
}
