/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.openide.util.NbBundle;
import static org.netbeans.modules.javafx2.editor.completion.impl.Bundle.*;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
final class FxInstructionItem extends AbstractCompletionItem {
    private static final String ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/instruction.png"; // NOI18N

    private String instruction;
    private Callable<String>  fxNamespaceDecl;
    
    public FxInstructionItem(String instruction, CompletionContext ctx, 
            String text, Callable<String> declarator) {
        super(ctx, text);
        this.instruction = instruction;
        this.fxNamespaceDecl = declarator;
    }

    @NbBundle.Messages({
        "# {0} - instruction / special element name",
        "FMT_fxmlInstructionItem=<b><font color='#000099'>{0}</font></b>"
    })
    @Override
    protected String getLeftHtmlText() {
        return FMT_fxmlInstructionItem(instruction);
    }

    @Override
    protected int getCaretShift(Document d) {
        int index = getSubstituteText().indexOf('"');
        if (index == -1) {
            return super.getCaretShift(d);
        } else {
            return index + 1;
        }
    }

    @Override
    protected void doSubstituteText(JTextComponent c, BaseDocument d, String text) throws BadLocationException {
        String prefix = "fx";
        if (fxNamespaceDecl != null) {
            try {
                prefix = fxNamespaceDecl.call();
            } catch (Exception ex) {
                throw new BadLocationException("", 0);
            }
        }
        if (!"fx".equals(prefix)) {
            text = text.replace("fx:", prefix + ":");
        }
        super.doSubstituteText(c, d, text);
    }
    
    private static ImageIcon ICON;

    @Override
    protected ImageIcon getIcon() {
        if (ICON == null) {
            ICON = ImageUtilities.loadImageIcon(ICON_RESOURCE, false);
        }
        return ICON;
    }
    
    public String toString() {
        return "instruction[" + instruction + "]";
    }
}
