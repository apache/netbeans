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

package org.netbeans.modules.javascript2.debug.ui.tooltip;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.debugger.ui.ToolTipUI;
import org.netbeans.spi.debugger.ui.ViewFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import static org.openide.text.Annotation.PROP_SHORT_DESCRIPTION;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public abstract class AbstractJSToolTipAnnotation extends Annotation {
    
    private static final Set<String> JS_KEYWORDS = new HashSet<>(Arrays.asList(new String[] {
        "break",    "case",     "catch",    "class",    "continue",
        "debugger", "default",  "delete",   "do",       "else",
        "enum",     "export",   "extends",  "finally",  "for",
        "function", "if",       "implements","import",  "in",
        "instanceof","interface","let",     "new",      "return",
        "package",  "private",  "protected","public",   "static",
        "super",    "switch",   /*"this",*/ "throw",    "try",
        "typeof",   "var",      "void",     "while",    "with",
        "yield",
    }));
    private static final int MAX_TOOLTIP_TEXT = 100000;
    
    private static final RequestProcessor RP = new RequestProcessor(AbstractJSToolTipAnnotation.class);
    
    @Override
    public String getShortDescription () {
        final Session session = DebuggerManager.getDebuggerManager ().getCurrentSession();
        if (session == null) {
            return null;
        }
        final DebuggerEngine engine = session.getCurrentEngine();
        if (engine == null) {
            return null;
        }

        final Line.Part lp = (Line.Part) getAttachedAnnotatable();
        if (lp == null) {
            return null;
        }
        Line line = lp.getLine ();
        DataObject dob = DataEditorSupport.findDataObject (line);
        if (dob == null) {
            return null;
        }
        final EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            return null;
            // Only for editable dataobjects
        }

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                evaluate(session, engine/*, dbg*/, lp, ec);
            }
        };
        RequestProcessor rp = engine.lookupFirst(null, RequestProcessor.class);
        if (rp == null) {
            // Debugger is likely finishing...
            rp = RP;
        }
        rp.post(runnable);
        return null;
    }
    
    protected abstract void handleToolTipClose(DebuggerEngine engine, ToolTipSupport tts);
    
    protected abstract Pair<String, Object> evaluate(String expression, DebuggerEngine engine) throws CancellationException;

    @Override
    public String getAnnotationType() {
        return null;
    }

    private void evaluate(Session session, final DebuggerEngine engine, //final Debugger dbg,
                          Line.Part lp, EditorCookie ec) {
        final Line line = lp.getLine();
        if (line == null) {
            return;
        }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return ;
        }
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor ();
        if (ep == null || ep.getDocument() != doc) {
            return ;
        }

        int lineNo = lp.getLine().getLineNumber();
        int column = lp.getColumn();
        final int offset = NbDocument.findLineOffset(doc, lineNo) + column;
        boolean[] isFunctionPtr = new boolean[] { false };
        final String expression = getIdentifier (
            doc,
            ep,
            lineNo,
            column,
            offset,
            isFunctionPtr
        );
        if (expression == null) {
            return;
        }
        final FileObject fo = line.getLookup().lookup(FileObject.class);
        if (isFunctionPtr[0]) {
            //return ; // We do not call functions
        }
        
        Pair<String, Object> toolTipTextAndVar;
        try {
            toolTipTextAndVar = evaluate(expression, engine);
        } catch (CancellationException ex) {
            return ;
        }
        if (toolTipTextAndVar == null) {
            return ;
        }
        final String toolTip = truncateLongText(toolTipTextAndVar.first());
        final Object var = toolTipTextAndVar.second();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EditorUI eui = Utilities.getEditorUI(ep);
                if (eui == null) {
                    firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTip);
                    return ;
                }
                ToolTipUI.Expandable expandable = (var != null) ?
                        new ToolTipUI.Expandable(expression, var) :
                        null;
                ToolTipUI.Pinnable pinnable = new ToolTipUI.Pinnable(
                        expression,
                        line.getLineNumber(),
                        "org.netbeans.modules.javascript2.debug.PIN_VALUE_PROVIDER"); // NOI18N
                ToolTipUI toolTipUI = ViewFactory.getDefault().createToolTip(toolTip, expandable, pinnable);
                ToolTipSupport tts = toolTipUI.show(ep);
                if (tts != null) {
                    handleToolTipClose(engine, tts);
                }
            }
        });
        //TODO: review, replace the code depending on lexer.model - part I
    }
    
    private static String getIdentifier (
        StyledDocument doc,
        JEditorPane ep,
        int line, int column, int offset,
        boolean[] isFunctionPtr
    ) {
        // do always evaluation if the tooltip is invoked on a text selection
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        ) {
            t = ep.getSelectedText ();
        }
        if (t != null) {
            return t;
        }
        Element lineElem =
            NbDocument.findLineRootElement (doc).
            getElement (line);

        if (lineElem == null) {
            return null;
        }
        int lineStartOffset = lineElem.getStartOffset ();
        int lineLen = lineElem.getEndOffset() - lineStartOffset;
        try {
            t = doc.getText (lineStartOffset, lineLen);
        } catch (BadLocationException ble) {
            return null;
        }
        column = Math.min(column, t.length());
        int identStart = column;

        boolean wasDot = false;
        while (identStart > 0) {
            char c = t.charAt (identStart - 1);
            if (Character.isJavaIdentifierPart(c) ||
                (c == '.' && (wasDot = true)) ||        // Please note that '=' is intentional here.
                (wasDot && (c == ']' || c == '['))) {

                identStart--;
            } else {
                break;
            }
        }
        int identEnd = column;
        while (identEnd < lineLen &&
                Character.isJavaIdentifierPart(t.charAt(identEnd))) {
            identEnd++;
        }
        if (identStart == identEnd) {
            return null;
        }

        String ident = t.substring (identStart, identEnd).trim();
        if (JS_KEYWORDS.contains(ident)) {
            // Java keyword => Do not show anything
            return null;
        }

        while (identEnd < lineLen &&
               Character.isWhitespace(t.charAt(identEnd))
        ) {
            identEnd++;
        }
        if (identEnd < lineLen && t.charAt(identEnd) == '(') {
            // We're at a function call
            isFunctionPtr[0] = true;
        }
        return ident;
    }

    private static String truncateLongText(String text) {
        if (text.length() > MAX_TOOLTIP_TEXT) {
            text = text.substring(0, MAX_TOOLTIP_TEXT) + "...";
        }
        return text;
    }

}
