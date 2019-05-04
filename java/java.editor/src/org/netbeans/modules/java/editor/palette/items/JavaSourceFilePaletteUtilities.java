/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.editor.palette.items;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.openide.text.IndentEngine;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author geertjan
 */
public class JavaSourceFilePaletteUtilities {
    
    public static void insert(final String s, final JTextComponent target) throws BadLocationException {

        final StyledDocument doc = (StyledDocument) target.getDocument();
        if (doc == null) {
            return;
        }

        class InsertFormatedText implements Runnable {
            @Override
            public void run() {
                try {
                    insertFormated(s, target, doc);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        InsertFormatedText insert = new InsertFormatedText();

        //This starts the run() in the Runnable above:
        NbDocument.runAtomicAsUser(doc, insert);
        
    }
    
    private static int insertFormated(String s, JTextComponent target, Document doc) throws BadLocationException {

        int start = -1;
        
        try {
            
            //Find the location in the editor,
            //and if it is a selection, remove it,
            //to be replaced by the dropped item:
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
        
            start = caret.getDot();
            
            //Insert the string in the document,
            //using the indentation engine
            //to create the correct indentation:
            IndentEngine engine = IndentEngine.find(doc);
            StringWriter textWriter = new StringWriter();
            try (Writer indentWriter = engine.createWriter(doc, start, textWriter)) {
                indentWriter.write(s);
            }

            doc.insertString(start, textWriter.toString(), null);
            
        } catch (IOException | BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return start;
    }
    
}
