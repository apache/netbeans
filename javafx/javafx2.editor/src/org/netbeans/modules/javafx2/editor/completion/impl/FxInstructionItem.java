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
