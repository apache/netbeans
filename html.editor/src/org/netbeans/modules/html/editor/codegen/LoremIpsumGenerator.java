/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class LoremIpsumGenerator implements CodeGenerator {

    JTextComponent textComp;

    /**
     *
     * @param context containing JTextComponent and possibly other items registered by {@link CodeGeneratorContextProvider}
     */
    private LoremIpsumGenerator(Lookup context) { // Good practice is not to save Lookup outside ctor
        textComp = context.lookup(JTextComponent.class);
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new LoremIpsumGenerator(context));
        }
    }

    /**
     * The name which will be inserted inside Insert Code dialog
     */
    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_lorem_ipsum"); //NOI18N
    }

    private static List<String> completeParagraphList() {
        List<String> paragraphs = new ArrayList<>();
        for (int paragraphNumber = 1; paragraphNumber <= 10; ++paragraphNumber) {
            paragraphs.add(NbBundle.getMessage(LoremIpsumGenerator.class, "lorem_ipsum_paragraph_" + paragraphNumber)); //NOI18N
        }
        return paragraphs;
    }

    /**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     */
    @Override
    public void invoke() {
        final int caretOffset = textComp.getCaretPosition();
        final LoremIpsumPanel panel = new LoremIpsumPanel(completeParagraphList());
        String title = NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_generate_lorem_ipsum"); //NOI18N
        DialogDescriptor dialogDescriptor = createDialogDescriptor(panel, title);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            insertLoremIpsumText((BaseDocument) textComp.getDocument(),
                    panel.getParagraphs(),
                    panel.getTag(),
                    caretOffset);
        }
    }

    static void insertLoremIpsumText(final BaseDocument document, final List<String> paragraphs, final String tag, final int offset) {
        final Reformat reformat = Reformat.get(document);
        reformat.lock();
        try {
            document.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        StringBuilder litext = getLoremIpsumText(paragraphs, tag);
                        if(!Utilities.isRowWhite(document, offset)) {
                            //generate the li text at a new line if the current one is not empty
                            litext.insert(0, '\n');
                        }
                        document.insertString(offset, litext.toString(), null);
                        reformat.reformat(offset, offset + litext.length());
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } finally {
            reformat.unlock();
        }
    }

    /**
     * Generates lorem ipsum text. The inserted text is indented with respect
     * to the current line.  Each paragraph is wrapped in the given HTML element.
     * The HTML element is expected to be an open element that will be closed
     * so thegenerated text is XHTML-compliant.
     *
     * @param paragraphs list of paragraphs of lorem ipsum to insert.  Must not be null.
     * @param tag to wrap paragraphs in.
     */
    private static StringBuilder getLoremIpsumText(List<String> paragraphs, String tag) {
        StringBuilder insertText = new StringBuilder();
        String closeTag = tag.replaceFirst("<", "</");
        for (String paragraph : paragraphs) {
            insertText.append(tag).append("\n");
            insertText.append(paragraph).append("\n");
            insertText.append(closeTag).append("\n");
        }
        return insertText;
    }


    private static DialogDescriptor createDialogDescriptor(JComponent content, String label) {
        JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_generate_button"));//NOI18N
        buttons[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LoremIpsumGenerator.class, "A11Y_Generate"));//NOI18N
        buttons[1] = new JButton(NbBundle.getMessage(LoremIpsumGenerator.class, "LBL_cancel_button"));//NOI18N
        return new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
    }
}
