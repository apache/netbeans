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

package org.netbeans.modules.editor.errorstripe.caret;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public class CaretMarkProvider extends MarkProvider implements CaretListener {
    
    private static final RequestProcessor RP = new RequestProcessor("CaretMarkProvider");
    
    private List<Mark> marks;
    private JTextComponent component;
    
    /** Creates a new instance of AnnotationMarkProvider */
    public CaretMarkProvider(JTextComponent component) {
        this.component = component;
        component.addCaretListener(this);
        marks = createMarks();
    }

    private List<Mark> createMarks() {
        Document doc = component.getDocument();
        if(!(doc instanceof StyledDocument)) {
            return Collections.singletonList((Mark)new CaretMark(0));
        }
        List<Mark> lines = new LinkedList<>();
        Caret caret = component.getCaret();
        if(caret instanceof EditorCaret) {
            EditorCaret editorCaret = (EditorCaret) caret;
            for (CaretInfo caretInfo : editorCaret.getCarets()) {
                int offset = caretInfo.getDot();
                int line = NbDocument.findLineNumber((StyledDocument) doc, offset);
                lines.add(new CaretMark(line));
            }
        } else {
            int offset = component.getCaretPosition(); //TODO: AWT?
            int line = NbDocument.findLineNumber((StyledDocument) doc, offset);
            lines.add(new CaretMark(line));
        }
        return lines;
    }
    
    @Override
    public synchronized List<Mark> getMarks() {
        return Collections.unmodifiableList(marks);
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        final List<Mark> old = getMarks();
        
        marks = createMarks();
        
        final List<Mark> nue = getMarks();
        
        //Do not fire this event under the document's write lock
        //may deadlock with other providers:
        RP.post(new Runnable() {
            @Override
            public void run() {
                firePropertyChange(PROP_MARKS, old, nue);
            }
        });
    }
    
}
