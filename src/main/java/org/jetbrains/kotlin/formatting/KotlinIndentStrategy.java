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
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinIndentStrategy {

    private static final char OPENING_BRACE_CHAR = '{';
    private static final char CLOSING_BRACE_CHAR = '}';
    private static final String CLOSING_BRACE_STRING = Character.toString(CLOSING_BRACE_CHAR);
    private static final String OPENING_BRACE_STRING = Character.toString(OPENING_BRACE_CHAR);

    private final StyledDocument doc;
    private final FileObject file;
    private int caretOffset;
    private int offset;
    
    public KotlinIndentStrategy(StyledDocument doc, int offset) {
        new FormatterImpl();
        this.doc = doc;
        this.file = ProjectUtils.getFileObjectForDocument(doc);
        this.offset = offset;
        caretOffset = offset;
    }

    public KotlinIndentStrategy(Context context) {
        this((StyledDocument) context.document(), context.caretOffset());
    }

    public int addIndent() throws BadLocationException {
        if (offset == 1) {
            return offset;
        }
        if (offset == doc.getLength()) {
            offset--;
        }
        String text = doc.getText(0, doc.getLength());
        String commandText = String.valueOf((text).charAt(offset));
        String openingChar = String.valueOf((text).charAt(offset - 2));
        if (CLOSING_BRACE_STRING.equals(commandText)
                && OPENING_BRACE_STRING.equals(openingChar)) {
            return autoEditAfterOpenBraceAndBeforeCloseBrace(text);
        } else if(CLOSING_BRACE_STRING.equals(commandText)) {
            return autoEditBeforeCloseBrace(text);
        } else {
            return autoEdit(text);
        }
    }

    private int autoEdit(String text) throws BadLocationException {
        StringBuilder textToFormat = new StringBuilder();
        textToFormat.append(text.substring(0, caretOffset));
        
        char charToInsert = ' ';
        if (isAfterEqualityOrOpenBrace(textToFormat.toString(), textToFormat.length())) {
            charToInsert = '1';
        }
        textToFormat.append(charToInsert).
                append(text.substring(caretOffset));
        
        String indent = getIndent(textToFormat.toString(), caretOffset);
        doc.insertString(caretOffset, indent, null);
        
        return caretOffset + indent.length();
    }

    private int autoEditAfterOpenBraceAndBeforeCloseBrace(String text) throws BadLocationException {
        String indent = getIndent(text, caretOffset);
        StringBuilder builder = new StringBuilder();
        builder.append(indent).append("    ").append('\n').append(indent);
        doc.insertString(caretOffset, builder.toString(), null);
        setDocumentOffset(indent.length() + 4);
        
        return caretOffset + indent.length() + 4;
    }

    private int autoEditBeforeCloseBrace(String text) throws BadLocationException {
        if (isNewLineBefore(text, caretOffset)) {
            StringBuilder oldText = new StringBuilder();
            
            oldText.append(text.substring(0, caretOffset - 1));
            oldText.append(CLOSING_BRACE_STRING).append(" ");
            oldText.append(text.substring(caretOffset + 1));
            
            if (oldText.charAt(caretOffset - 2) == '\n') {
                return caretOffset;
            }
            
            String indent = getIndent(oldText.toString(), caretOffset);
            doc.insertString(caretOffset, indent, null);
            return caretOffset + indent.length();
        }
        
        return caretOffset;
    }
    
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
        String newText = NetBeansDocumentFormattingModel.getNewText();
        if (newText == null) {
            return "";
        }

        if (offset >= newText.length()) {
            return "";
        }
        newText = newText.substring(offset);
        
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

    private boolean isAfterEqualityOrOpenBrace(String text, int offset) {
        int curOffset = offset - 2;
        while (curOffset > 0) {
            char charAtCurrentOffset = text.charAt(curOffset);
            if (charAtCurrentOffset == '=' || charAtCurrentOffset == '{') {
                return true;
            } else if (charAtCurrentOffset != ' ') {
                return false;
            }
            curOffset--;
        }
        
        return false;
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
    
    private void setDocumentOffset(int column) {
        Line line = NbEditorUtilities.getLine(doc, offset, false);
        line.show(Line.ShowOpenType.NONE,Line.ShowVisibilityType.NONE, column);
    }
}
