/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
