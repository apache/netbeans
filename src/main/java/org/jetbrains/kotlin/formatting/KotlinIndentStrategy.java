/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.formatting;

import com.intellij.formatting.FormatterImpl;
import com.intellij.formatting.Indent;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.jetbrains.kotlin.idea.formatter.KotlinSpacingRulesKt;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.indent.spi.Context;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinIndentStrategy {

    private static final char OPENING_BRACE_CHAR = '{';
    private static final char CLOSING_BRACE_CHAR = '}';
    private static final String CLOSING_BRACE_STRING = Character.toString(CLOSING_BRACE_CHAR);

    private final Context context;
    private final StyledDocument doc;
    private final FileObject file;
    private int offset;

    public KotlinIndentStrategy(Context context) {
        new FormatterImpl();
        this.context = context;
        this.doc = (StyledDocument) context.document();
        this.file = ProjectUtils.getFileObjectForDocument(doc);
        offset = context.caretOffset();
    }

    public void addIndent() throws BadLocationException {
        if (offset == doc.getLength()) {
            offset--;
        }
        String text = doc.getText(0, doc.getLength());
        String commandText = String.valueOf((text).charAt(offset));
        if (isNewLine(commandText)) { // before new line
            autoEdit(text);
        } else if (CLOSING_BRACE_STRING.equals(commandText)) { // before closing brace
            System.out.println();
        }
    }
    
    private void autoEdit(String text) throws BadLocationException {
        offset--;
        String indent = getIndent(text, context.caretOffset());
        doc.insertString(context.caretOffset(), indent, null);
    }
    
//    private void autoEditAfterNewLine(String text) throws BadLocationException {
//        offset--;
//        
//        int lineNumber = NbDocument.findLineNumber(doc, offset);
//        int start = NbDocument.findLineOffset(doc, lineNumber );
//        int end = NbDocument.findLineOffset(doc, lineNumber + 1);

//        boolean afterOpenBrace = isAfterOpenBrace(text, offset - 1, start);
//        boolean beforeCloseBrace = isBeforeCloseBrace(text, offset, end);
//        int oldOffset = offset;
//        int newOffset = findEndOfWhiteSpace(text, offset - 1) + 1;
//        int length = 1;
//        if (newOffset > 0 && !IndenterUtil.isWhiteSpaceOrNewLine(text.charAt(newOffset - 1))) {
//            offset = newOffset;
//            length = oldOffset - newOffset;
//        }
//        
//        String indent = getIndent(text, context.caretOffset());
//        doc.insertString(context.caretOffset(), indent, null);
//    }
    
    private String getIndent(String text, int offset) throws BadLocationException {
        Project project = ProjectUtils.getKotlinProjectForFileObject(file);
        if (project == null) {
            return "";
        }
        KtPsiFactory psiFactory = KotlinFormatterUtils.createPsiFactory(project);
        KtFile ktFile = KotlinFormatterUtils.createKtFile(text, psiFactory, file.getName());
        
        CodeStyleSettings settings = KotlinFormatterUtils.getSettings();
        KotlinBlock rootBlock = new KotlinBlock(ktFile.getNode(),
                NodeAlignmentStrategy.getNullStrategy(),
                Indent.getNoneIndent(),
                null,
                settings,
                KotlinSpacingRulesKt.createSpacingBuilder(
                    settings, KotlinFormatter.KotlinSpacingBuilderUtilImpl.INSTANCE));
        KotlinFormatterUtils.adjustIndent(ktFile, rootBlock, settings, offset, text);
        String newText = NetBeansDocumentFormattingModel.getNewText().substring(offset);
        
        int endOfWhiteSpace = findEndOfWhiteSpaceAfter(newText, 0, newText.length());
        String toReturn = newText.substring(0, endOfWhiteSpace);
        
        return toReturn;
    }
    
    private static int findEndOfWhiteSpaceAfter(String document, int offset, int end) throws BadLocationException {
        while (offset < end) {
            if (!IndenterUtil.isWhiteSpaceChar(document.charAt(offset))) {
                return offset;
            }

            offset++;
        }

        return end;
    }

    private static int findEndOfWhiteSpaceBefore(String document, int offset, int start) throws BadLocationException {
        while (offset >= start) {
            if (!IndenterUtil.isWhiteSpaceChar(document.charAt(offset))) {
                return offset;
            }

            offset--;
        }

        return start;
    }

    private static boolean isAfterOpenBrace(String document, int offset, int startLineOffset) throws BadLocationException {
        int nonEmptyOffset = findEndOfWhiteSpaceBefore(document, offset, startLineOffset);
        return document.charAt(nonEmptyOffset) == OPENING_BRACE_CHAR;
    }

    private static boolean isBeforeCloseBrace(String document, int offset, int endLineOffset) throws BadLocationException {
        int nonEmptyOffset = findEndOfWhiteSpaceAfter(document, offset, endLineOffset);
        if (nonEmptyOffset == document.length()) {
            nonEmptyOffset--;
        }
        return document.charAt(nonEmptyOffset) == CLOSING_BRACE_CHAR;
    }

    private static boolean isNewLineBefore(String document, int offset) {
        offset--;
        char prev = IndenterUtil.SPACE_CHAR;
        StringBuilder bufBefore = new StringBuilder(prev);
        while (IndenterUtil.isWhiteSpaceChar(prev) && offset > 0) {
            prev = document.charAt(offset--);
            bufBefore.append(prev);
        }

        return containsNewLine(bufBefore.toString());
    }

    private static boolean startsWithNewLine(String text) {
        return text.startsWith("\n");
    }
    
    private static boolean containsNewLine(String text) {
        return text.contains("\n");
    }

    private static int findEndOfWhiteSpace(String text, int offset) {
        while (offset > 0) {
            char c = text.charAt(offset);
            if (!IndenterUtil.isWhiteSpaceChar(c)) {
                return offset;
            }

            offset--;
        }

        return offset;
    }

    private static boolean isNewLine(String text) {
        return "\n".equals(text);
    }

}
