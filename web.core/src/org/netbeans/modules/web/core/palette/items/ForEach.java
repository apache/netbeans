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
