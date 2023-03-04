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

package org.netbeans.modules.cpplite.debugger;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerEngine;

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.Line.Part;
import org.openide.util.RequestProcessor;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer;


public class ToolTipAnnotation extends Annotation implements Runnable {

    private Part lp;
    private EditorCookie ec;

    @Override
    public String getShortDescription () {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) return null;
        CPPLiteDebugger d = currentEngine.lookupFirst(null, CPPLiteDebugger.class);
        if (d == null) return null;

        Part lp = (Part)
            getAttachedAnnotatable();
        if (lp == null) return null;
        Line line = lp.getLine ();
        DataObject dob = DataEditorSupport.findDataObject (line);
        if (dob == null) return null;
        EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);

        if (ec == null) return null;
        this.lp = lp;
        this.ec = ec;
        RequestProcessor.getDefault ().post (this);
        return null;
    }

    @Override
    public void run () {
        //if (expression == null) return;
        if (lp == null || ec == null) return ;
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return ;
        }
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        if (ep == null) return ;
        String expression = getIdentifier (
            doc,
            ep,
            NbDocument.findLineOffset (
                doc,
                lp.getLine ().getLineNumber ()
            ) + lp.getColumn ()
        );
        if (expression == null) return ;
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) return;
        CPPLiteDebugger d = currentEngine.lookupFirst(null, CPPLiteDebugger.class);
        CPPFrame frame;
        if (d == null || (frame = d.getCurrentFrame()) == null) {
            return;
        }
        frame.evaluateAsync(expression).thenAccept(variable -> {
                               VariableDisplayer displayer = currentEngine.lookupFirst(null, VariableDisplayer.class);
                               if (displayer != null) {
                                   variable = displayer.displayed(variable)[0];
                               }
                               String value = variable.getValue();
                               if (!value.equals(expression)) {
                                   String toolTipText;
                                   String type = variable.getType();
                                   if (type != null) {
                                       toolTipText = expression + " = (" + type + ") " + value;
                                   } else {
                                       toolTipText = expression + " = " + value;
                                   }
                                   firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTipText);
                               }
                           }).exceptionally(exception -> {
                               String toolTipText = exception.getLocalizedMessage();
                               firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTipText);
                               return null;
                           });
    }

    @Override
    public String getAnnotationType () {
        return null; // Currently return null annotation type
    }

    private static String getIdentifier (
        StyledDocument doc, 
        JEditorPane ep, 
        int offset
    ) {
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        )   t = ep.getSelectedText ();
        if (t != null) return t;
        
        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            Element lineElem = 
                NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) return null;
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            lineLen = t.length ();
            int identStart = col;
            while (identStart > 0 &&
                   (Character.isJavaIdentifierPart(t.charAt (identStart - 1)) ||
                    (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen &&
                   Character.isJavaIdentifierPart(t.charAt(identEnd))) {
                identEnd++;
            }
            if (identStart == identEnd) {
                return null;
            }
            String ident = t.substring (identStart, identEnd);
            while (identEnd < lineLen &&
                   Character.isWhitespace(t.charAt(identEnd))) {
                identEnd++;
            }
            return ident;
        } catch (BadLocationException e) {
            return null;
        }
    }

}

