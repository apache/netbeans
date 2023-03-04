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

package org.netbeans.modules.web.core.palette.items;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class ForEach implements ActiveEditorDrop {

    private String variable = "";
    private String collection = "";
    private boolean fixed = false;
    private String begin = "";
    private String end = "";
    private String step = "";

    public ForEach() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        ForEachCustomizer c = new ForEachCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String prefix = JspPaletteUtilities.findJstlPrefix(targetComponent);
            String body = createBody(prefix);
            try {
                JspPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        return accept;
    }

    private String createBody(String prefix) {
        if (variable.equals("")) {  // NOI18N
            variable = JspPaletteUtilities.CARET;
        } else if (collection.equals("")) {  // NOI18N
            collection = JspPaletteUtilities.CARET;
        }
        String strVariable = " var=\"" + variable + "\""; // NOI18N
        String strCollection = " items=\"" + collection + "\""; // NOI18N
        String strBegin = ""; // NOI18N
        String strEnd = ""; // NOI18N
        String strStep = ""; // NOI18N
        if (fixed) {
            if (begin.length() > 0) {
                strBegin = " begin=\"" + begin + "\""; // NOI18N
            }
            if (end.length() > 0) {
                strEnd = " end=\"" + end + "\""; // NOI18N
            }
            if (step.length() > 0) {
                strStep = " step=\"" + step + "\""; // NOI18N
            }
        }

        return "<"+prefix+":forEach" + strVariable + strCollection + strBegin + strEnd + strStep + ">\n" // NOI18N
                + "</"+prefix+":forEach>"; // NOI18N
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
