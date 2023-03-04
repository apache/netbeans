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

package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;


public class TestEditorDocument extends PlainDocument {
    
    LineRootElement lineElementRoot;

    public TestEditorDocument() {
        super(new EditorDocumentContent()); // Content to be tested
        ((EditorDocumentContent)getContent()).init(this);
        lineElementRoot = new LineRootElement(this);
    }

    EditorDocumentContent getDocumentContent() {
        return (EditorDocumentContent) getContent();
    }

    public Position createBackwardBiasPosition(int offset) throws BadLocationException {
        return getDocumentContent().createBackwardBiasPosition(offset);
    }

    @Override
    public synchronized Element getDefaultRootElement() {
        return lineElementRoot;
    }

    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        lineElementRoot.insertUpdate(chng, chng, attr);
    }

    @Override
    protected void removeUpdate(DefaultDocumentEvent chng) {
        lineElementRoot.removeUpdate(chng, chng);
    }
    
}
