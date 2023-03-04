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
