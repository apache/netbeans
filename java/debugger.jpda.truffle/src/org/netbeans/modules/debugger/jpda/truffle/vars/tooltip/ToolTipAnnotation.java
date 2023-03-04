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

package org.netbeans.modules.debugger.jpda.truffle.vars.tooltip;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleEval;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleVariableImpl;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

public class ToolTipAnnotation extends Annotation implements Runnable {
    
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

    private Line.Part lp;
    private EditorCookie ec;
    private final RequestProcessor RP = new RequestProcessor(ToolTipAnnotation.class);

    @Override
    public String getShortDescription () {
        Session session = DebuggerManager.getDebuggerManager ().getCurrentSession();
        if (session == null) {
            return null;
        }
        DebuggerEngine engine = session.getCurrentEngine();
        if (engine != session.getEngineForLanguage(TruffleStrataProvider.TRUFFLE_STRATUM)) {
            return null;
        }
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return null;
        }

        Line.Part lp = (Line.Part) getAttachedAnnotatable();
        if (lp == null) {
            return null;
        }
        Line line = lp.getLine ();
        DataObject dob = DataEditorSupport.findDataObject (line);
        if (dob == null) {
            return null;
        }
        EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            return null;
            // Only for editable dataobjects
        }

        this.lp = lp;
        this.ec = ec;
        RP.post(this);
        return null;
    }

    @Override
    public String getAnnotationType() {
        return null;
    }

    @Override
    public void run() {
        ObjectVariable tooltipVariable = null;
        if (lp == null || ec == null) {
            return ;
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
        Session session = DebuggerManager.getDebuggerManager ().getCurrentSession();
        if (session == null) {
            return ;
        }
        DebuggerEngine engine = session.getCurrentEngine();
        if (engine != session.getEngineForLanguage(TruffleStrataProvider.TRUFFLE_STRATUM)) {
            return ;
        }
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return ;
        }
        JPDAThread thread = d.getCurrentThread();
        if (thread == null || TruffleAccess.getCurrentPCInfo(thread) == null) {
            return ;
        }

        int offset;
        boolean[] isFunctionPtr = new boolean[] { false };
        final String expression = getIdentifier (
            d,
            doc,
            ep,
            offset = NbDocument.findLineOffset (
                doc,
                lp.getLine ().getLineNumber ()
            ) + lp.getColumn (),
            isFunctionPtr
        );
        if (expression == null) {
            return;
        }

        String toolTipText;
        if (isFunctionPtr[0]) {
            //return ; // We do not call functions
        }
        try {
            Variable result = TruffleEval.evaluate(d, expression);
            if (result == null) {
                return ; // Something went wrong...
            }
            TruffleVariableImpl tv = TruffleVariableImpl.get(result);
            String displayVal;
            if (tv != null) {
                displayVal = tv.getDisplayValue();
            } else {
                displayVal = result.getValue();
            }
            toolTipText = expression + " = " + displayVal;
        } catch (InvalidExpressionException ex) {
            toolTipText = expression + " = >" + ex.getMessage () + "<";
        }
        toolTipText = truncateLongText(toolTipText);
        
        firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTipText);
    }
    
    private static String getIdentifier (
        JPDADebugger debugger,
        StyledDocument doc,
        JEditorPane ep,
        int offset,
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

            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 &&
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen &&
                   Character.isJavaIdentifierPart(t.charAt(identEnd))
            ) {
                identEnd++;
            }

            if (identStart == identEnd) {
                return null;
            }

            String ident = t.substring (identStart, identEnd);
            //if (JS_KEYWORDS.contains(ident)) {
                // JS keyword => Do not show anything
            //    return null;
            //}

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
        } catch (BadLocationException e) {
            return null;
        }
    }

    private static String truncateLongText(String text) {
        if (text.length() > MAX_TOOLTIP_TEXT) {
            text = text.substring(0, MAX_TOOLTIP_TEXT) + "...";
        }
        return text;
    }

}
