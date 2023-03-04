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

package org.netbeans.modules.web.debug;

import java.io.*;
import javax.swing.JEditorPane;
import javax.swing.text.*;

import org.openide.cookies.EditorCookie;
import org.openide.text.*;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.loaders.DataObject;

public class JspToolTipAnnotation extends Annotation implements Runnable {
    
    private String toolTipText = null;

    private StyledDocument doc;
    
    private RequestProcessor rp = new RequestProcessor("JspToolTipAnnotation", 1);

    public String getShortDescription() {
        Utils.log("JspTooltip: getShortDescription");
        
        toolTipText = null;
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) return null;
        JPDADebugger d = (JPDADebugger) currentEngine.lookupFirst (null, JPDADebugger.class);
        if (d == null) return null;

        Line.Part lp = (Line.Part) getAttachedAnnotatable();
        if (lp != null) {
            Line line = lp.getLine ();
            DataObject dob = DataEditorSupport.findDataObject(line);
            EditorCookie ec = (EditorCookie) dob.getCookie(EditorCookie.class);

            if (ec != null) { // Only for editable dataobjects
                try {
                    doc = ec.openDocument ();                    
                    rp.post(this);                    
                } catch (IOException e) {
                }
            }
        }
        return toolTipText;
    }

    public void run () {

        Utils.log("JspTooltip: run");

        //1) get tooltip text
        Line.Part lp = (Line.Part)getAttachedAnnotatable();
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        String textForTooltip = "";
        
        if ((lp == null) || (ep == null)) {
            return;
        }
        
        //first try EL
        String text = Utils.getELIdentifier(doc, ep,NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn());
        Utils.log("JspTooltip: ELIdentifier = " + text);

        boolean isScriptlet = Utils.isScriptlet(
            doc, ep, NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn()
        );
        Utils.log("isScriptlet: " + isScriptlet);
        
        //if not, try Java
        if ((text == null) && (isScriptlet)) {
            text = Utils.getJavaIdentifier(
                doc, ep, NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn()
            );
            textForTooltip = text;
            Utils.log("JspTooltip: javaIdentifier = " + text);
            if (text == null) {
                return;
            }
        } else {
            if (text == null) {
                return;
            }
            textForTooltip = text;
            String textEscaped = text.replace("\"", "\\\"");
            text = "pageContext.getExpressionEvaluator().evaluate(\"" + textEscaped + "\", "+
                        "java.lang.String.class, "+
                        "((javax.servlet.jsp.PageContext) pageContext).getVariableResolver(), "+
                        "null)";
        }
        
        Utils.log("JspTooltip: fullWatch = " + text);
        
        //3) obtain text representation of value of watch
        String old = toolTipText;
        toolTipText = null;
        
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) return;
        JPDADebugger d = (JPDADebugger) currentEngine.lookupFirst (null, JPDADebugger.class);
        if (d == null) return;
        
        try {
            Variable v = d.evaluate(text);
            if (v instanceof ObjectVariable) {
                toolTipText = textForTooltip + " = (" + v.getType() + ")" + ((ObjectVariable)v).getToStringValue();
            } else {
                toolTipText = textForTooltip + " = (" + v.getType() + ")" + v.getValue();
            }
        } catch (InvalidExpressionException e) {
            toolTipText = text + " = >" + e.getMessage() + "<";
        }
        Utils.log("JspTooltip: " + toolTipText);
        firePropertyChange (PROP_SHORT_DESCRIPTION, old, toolTipText);       
    }

    public String getAnnotationType () {
        return null;
    }
    
}
