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

package org.netbeans.lib.editor.util.random;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.undo.UndoManager;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Testing document implementation.
 * <br/>
 * It populates modification text into the document event which is useful for the lexer.
 *
 * @author mmetelka
 */
public class TestDocument extends PlainDocument {
    
    public TestDocument() {
        UndoManager undoManager = new UndoManager();
        addUndoableEditListener(undoManager);
        putProperty(UndoManager.class, undoManager);
        super.addDocumentListener(org.netbeans.lib.editor.util.swing.DocumentUtilities.initPriorityListening(this));
    }

    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        super.insertUpdate(chng, attr);
        DocumentUtilities.addEventPropertyStorage(chng);
        try {
            DocumentUtilities.putEventProperty(chng, String.class,
                    getText(chng.getOffset(), chng.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());
        }
    }

    @Override
    protected void removeUpdate(DefaultDocumentEvent chng) {
        super.removeUpdate(chng);
        DocumentUtilities.addEventPropertyStorage(chng);
        try {
            DocumentUtilities.putEventProperty(chng, String.class,
                    getText(chng.getOffset(), chng.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());
        }
    }

}
