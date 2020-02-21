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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.fortran;

import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.cplusplus.CCKit;
import org.netbeans.modules.cnd.editor.cplusplus.CKit;
import org.netbeans.modules.cnd.editor.cplusplus.CppDTIFactory;
import org.netbeans.modules.cnd.editor.cplusplus.CppTBIFactory;
import org.netbeans.modules.cnd.editor.cplusplus.CppTTIFactory;
import org.netbeans.modules.cnd.editor.cplusplus.HKit;
import org.netbeans.modules.cnd.editor.fortran.indent.FortranHotCharIndent;
import org.netbeans.modules.cnd.editor.fortran.indent.FortranIndentTask;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranReformatter;
import org.netbeans.modules.cnd.editor.indent.CppIndentFactory;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.netbeans.modules.cnd.test.base.BaseDocumentUnitTestCase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.util.Exceptions;

/**
 *
 */
public class FortranEditorBase extends BaseDocumentUnitTestCase {

    public FortranEditorBase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected EditorKit createEditorKit() {
        return new FKit();
    }

    protected void setDefaultsOptions(boolean isFreeFormat){
        BaseDocument bd = getDocument();
        FortranCodeStyle codeStyle = FortranCodeStyle.get(bd);
        codeStyle.setAutoFormatDetection(false);
        codeStyle.setFreeFormatFortran(isFreeFormat);
        codeStyle.setupLexerAttributes(bd);
    }
    
    private MimePath mimePathCpp;
    private final MimePath[] embeddingsPathCpp = new MimePath[4];
    private MimePath mimePathHeader;
    private final MimePath[] embeddingsPathHeader = new MimePath[4];
    private MimePath mimePathC;
    private final MimePath[] embeddingsPathC = new MimePath[4];
    private MimePath mimePathFortran;
    private MimePath mimePathAsm;

    @Override
    protected void setUpMime() {
        mimePathCpp = MimePath.parse(MIMENames.CPLUSPLUS_MIME_TYPE);
        setUpWithEmbeddings(mimePathCpp, embeddingsPathCpp, new CCKit());
        mimePathHeader = MimePath.parse(MIMENames.HEADER_MIME_TYPE);
        setUpWithEmbeddings(mimePathHeader, embeddingsPathHeader, new HKit());
        mimePathC = MimePath.parse(MIMENames.C_MIME_TYPE);
        setUpWithEmbeddings(mimePathC, embeddingsPathC, new CKit());
        mimePathFortran = MimePath.parse(MIMENames.FORTRAN_MIME_TYPE);
        MockMimeLookup.setInstances(mimePathFortran, new FKit(), new FortranReformatter.Factory(), new FortranTTIFactory());
        mimePathAsm = MimePath.parse(MIMENames.ASM_MIME_TYPE);
        // TODO: add needed dependency in all dependant test cases to use real asm editor kit
        //MockMimeLookup.setInstances(mimePath5, new AsmEditorKit());
        MockMimeLookup.setInstances(mimePathAsm, new AsmStub());
    }

    private void setUpWithEmbeddings(MimePath mimePath, MimePath[] embeddings, NbEditorKit kit) {
        MockMimeLookup.setInstances(mimePath, kit, new Reformatter.Factory(), new CppIndentFactory(),
                new CppDTIFactory(), new CppTBIFactory(), new CppTTIFactory());
        embeddings[0] = MimePath.get(mimePath, MIMENames.DOXYGEN_MIME_TYPE);
        embeddings[1] = MimePath.get(mimePath, MIMENames.STRING_DOUBLE_MIME_TYPE);
        embeddings[2] = MimePath.get(mimePath, MIMENames.STRING_SINGLE_MIME_TYPE);
        embeddings[3] = MimePath.get(mimePath, MIMENames.PREPROC_MIME_TYPE);
        for (MimePath embMP : embeddings) {
            MockMimeLookup.setInstances(embMP, kit, new Reformatter.Factory(), new CppIndentFactory(),
                    new CppDTIFactory(), new CppTBIFactory(), new CppTTIFactory());
        }
    }

    private static final class AsmStub extends NbEditorKit {
        private AsmStub(){
        }
    }

    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected void indentLine() {
        final int offset = getCaretOffset();
        final FortranIndentTask task = new FortranIndentTask(getDocument());
        getDocument().runAtomic(new Runnable() {
            @Override
            public void run() {
                task.indentLock();
                try {
                    task.reindent(offset);
                } catch (BadLocationException e) {
                    e.printStackTrace(getLog());
                    fail(e.getMessage());
                } finally {
                    task.indentLock();
                }
            }
        });
    }
    /**
     * Perform new-line insertion followed by indenting of the new line
     * by the formatter.
     * The caret position should be marked in the document text by '|'.
     */
    protected void indentNewLine() {
        final int offset = getCaretOffset();
        final FortranIndentTask task = new FortranIndentTask(getDocument());
        getDocument().runAtomic(new Runnable() {
            @Override
            public void run() {
                task.indentLock();
                try {
                    getDocument().insertString(offset, "\n", null); // NOI18N
                    task.reindent(offset + 1);
                } catch (BadLocationException e) {
                    e.printStackTrace(getLog());
                    fail(e.getMessage());
                } finally {
                    task.indentLock();
                }
            }
        });
    }

    /**
     * Perform reformatting of the whole document's text.
     */
    protected void reformat() {
        final FortranReformatter f = new FortranReformatter(getDocument(), FortranCodeStyle.get(getDocument()));
        getDocument().runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    f.reformat();
                } catch (BadLocationException e) {
                    e.printStackTrace(getLog());
                    fail(e.getMessage());
                }
            }
        });
    }

    protected void typeChar(char ch, boolean indentNewLine) {
        try {
            int pos = getCaretOffset();
            getDocument().insertString(pos, String.valueOf(ch), null);
            if (FortranHotCharIndent.INSTANCE.getKeywordBasedReformatBlock(getDocument(), pos, ""+ch)) {
                indentLine();
                indentNewLine = false;
            }
            if (indentNewLine) {
                indentNewLine();
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected void typeCharactersInText(String origTextWithPipe, String typedText, String resultTextWithPipe) {
        Context context = new Context(getEditorKit(), origTextWithPipe);
        context.typeText(typedText);
        context.assertDocumentTextEquals(resultTextWithPipe);
    }
}
