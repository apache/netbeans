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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
