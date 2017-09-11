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

package org.netbeans.modules.xml.text.completion;

import java.awt.Color;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.xml.api.model.*;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.swing.plaf.LFCustoms;

/**
 * It represents attribute name (or namespace prefix).
 *
 * @author  sands
 * @author  Petr Kuzel
 */
class AttributeResultItem extends XMLResultItem {
    private static final Color COLOR = new Color(64, 64, 255);
    
    // we are requested to avoid appending extra data
    private boolean inline = false;
    
    private final GrammarResult res;

    public AttributeResultItem(int position, GrammarResult res, boolean inline){
        super(position, res.getNodeName());
        this.res = res;
        selectionForeground = foreground = Color.green.darker().darker();        
        this.inline = inline;
    }
    
    public String getReplacementText(int modifiers) {
        String extend = inline ? "" : "=\"\"";     // NOI18N
        return super.getReplacementText(modifiers) + extend;
    }
    
    public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
        boolean result = super.replaceText(c, getReplacementText(modifiers), offset, len);
        c.getCaret().setDot(c.getCaretPosition() - 1); //shift cursor into bracklets
        return result;
    }
    
    Color getPaintColor() { return LFCustoms.shiftColor(COLOR); }

    @Override
    public CompletionTask createDocumentationTask() {
        return doCreateDocumentationTask(res);
    }
}
