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
package org.netbeans.modules.csl.hints.infrastructure;

import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.EditList;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.util.NbBundle;

import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.text.NbDocument;

/**
 * Offer a preview for hints (that support it)
 * 
 * @author Tor Norbye
 */
final class PreviewHintFix implements EnhancedFix {

    private ParserResult info;
    private PreviewableFix fix;
    private final String sortText;

    PreviewHintFix(ParserResult info, PreviewableFix fix, String sortText) {
        this.info = info;
        this.fix = fix;
        this.sortText = sortText;
    }

    public String getText() {
        // Indent Preview entries. I can't put the whitespace in the bundle file
        // because strings seem to get trimmed by the NbBundle call.
        return "    " + NbBundle.getMessage(PreviewHintFix.class, "PreviewHint");
    }

    public ChangeInfo implement() throws Exception {
        EditList edits = fix.getEditList();

        Document oldDoc = info.getSnapshot().getSource().getDocument(true);
        //OffsetRange range = edits.getRange();
        OffsetRange range = new OffsetRange(0, oldDoc.getLength());
        String oldSource = oldDoc.getText(range.getStart(), range.getEnd());

        String mimeType = (String) oldDoc.getProperty("mimeType"); //NOI18N
        BaseDocument newDoc = new BaseDocument(false, mimeType);

        Language language = (Language) oldDoc.getProperty(Language.class);
        newDoc.putProperty(Language.class, language);
        newDoc.insertString(0, oldSource, null);
        edits.applyToDocument(newDoc);
        String newSource = newDoc.getText(0, newDoc.getLength());

        String oldTitle = NbBundle.getMessage(PreviewHintFix.class, "CurrentSource");
        String newTitle = NbBundle.getMessage(PreviewHintFix.class, "FixedSource");

        final DiffController diffView = DiffController.create(
                new DiffSource(oldSource, oldTitle),
                new DiffSource(newSource, newTitle));


        JComponent jc = diffView.getJComponent();

        jc.setPreferredSize(new Dimension(800, 600));

        // Warp view to a particular diff?
        // I can't just always jump to difference number 0, because when a hint
        // has changed only the whitespace (such as the fix which moves =begin entries to column 0)
        // there are no diffs, even though I want to jump to the relevant line.
        final int index = 0;
        final int firstLine = diffView.getDifferenceCount() == 0 ? NbDocument.findLineNumber((StyledDocument) oldDoc, edits.getRange().
                getStart()) : -1;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (firstLine != -1) {
                    diffView.setLocation(DiffController.DiffPane.Base,
                            DiffController.LocationType.LineNumber, firstLine);
                } else if (diffView.getDifferenceCount() > 0) {
                    diffView.setLocation(DiffController.DiffPane.Base,
                            DiffController.LocationType.DifferenceIndex, index);
                }
            }
        });

        JButton apply = new JButton(NbBundle.getMessage(PreviewHintFix.class, "Apply"));
        JButton ok = new JButton(NbBundle.getMessage(PreviewHintFix.class, "Ok"));
        JButton cancel = new JButton(NbBundle.getMessage(PreviewHintFix.class, "Cancel"));
        String dialogTitle = NbBundle.getMessage(PreviewHintFix.class, "PreviewTitle",
                fix.getDescription());

        DialogDescriptor descriptor =
                new DialogDescriptor(jc, dialogTitle, true,
                new Object[]{apply, ok, cancel}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null,
                true);
        Dialog dlg = null;

        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
            if (descriptor.getValue() == apply) {
                fix.implement();
            }
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }

        return null;
    }
    
    public CharSequence getSortText() {
        return sortText;
    }
    
    private class DiffSource extends StreamSource {

        private String source;
        private String title;

        private DiffSource(String source, String title) {
            this.source = source;
            this.title = title;
        }

        @Override
        public String getName() {
            return "?"; // unused?
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMIMEType() {
            return info.getSnapshot().getMimeType();
        }

        @Override
        public Reader createReader() throws IOException {
            return new StringReader(source);
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isEditable() {
            return false;
        }
    }
}
