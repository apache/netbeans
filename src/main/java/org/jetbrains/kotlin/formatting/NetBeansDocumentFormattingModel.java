package org.jetbrains.kotlin.formatting;

/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 ******************************************************************************
 */
import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingDocumentModel;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;

import com.intellij.formatting.FormattingModelEx;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansDocumentFormattingModel implements FormattingModelEx {

    private static String newText;
    private final Project myProject;
    private final ASTNode myASTNode;
    private final NetBeansFormattingModel myDocumentModel;
    private final Block myRootBlock;
    protected boolean myCanModifyAllWhiteSpaces = false;
    private final String source;
    private final List<ReplaceEdit> edits = new ArrayList<>();
    private final CodeStyleSettings settings;

    public NetBeansDocumentFormattingModel(PsiFile file, Block rootBlock,
            NetBeansFormattingModel documentModel, String source,
            CodeStyleSettings settings) {
        myASTNode = SourceTreeToPsiMap.psiElementToTree(file);
        myDocumentModel = documentModel;
        myRootBlock = rootBlock;
        myProject = file.getProject();
        this.source = source;
        this.settings = settings;
    }

    @Override
    public Block getRootBlock() {
        return myRootBlock;
    }

    @Override
    public FormattingDocumentModel getDocumentModel() {
        return myDocumentModel;
    }

    @Override
    public TextRange replaceWhiteSpace(TextRange textRange, String whiteSpace) {
        return replaceWhiteSpace(textRange, null, whiteSpace);
    }

    @Override
    public TextRange replaceWhiteSpace(TextRange textRange, ASTNode nodeAfter, String whiteSpace) {
        CharSequence whiteSpaceToUse = getDocumentModel().adjustWhiteSpaceIfNecessary(whiteSpace,
                textRange.getStartOffset(), textRange.getEndOffset(), nodeAfter, false);

        replace(textRange, whiteSpaceToUse.toString());
        return textRange;
    }

    @Override
    public TextRange shiftIndentInsideRange(ASTNode node, TextRange range, int indent) {
        try {
            shiftIndentInside(range, indent);
            return range;
        } catch (BadLocationException e) {
        }

        return null;
    }

    private int shiftIndentInside(final TextRange elementRange, final int shift) throws BadLocationException {
        final StringBuilder buffer = new StringBuilder();
        StringBuilder afterWhiteSpace = new StringBuilder();
        int whiteSpaceLength = 0;
        boolean insideWhiteSpace = true;
        int line = 0;
        for (int i = elementRange.getStartOffset(); i < elementRange.getEndOffset(); i++) {
            final char c = source.charAt(i);
            switch (c) {
                case '\n':
                    if (line > 0) {
                        createWhiteSpace(whiteSpaceLength + shift, buffer);
                    }
                    buffer.append(afterWhiteSpace.toString());
                    insideWhiteSpace = true;
                    whiteSpaceLength = 0;
                    afterWhiteSpace = new StringBuilder();
                    buffer.append(c);
                    line++;
                    break;
                case ' ':
                    if (insideWhiteSpace) {
                        whiteSpaceLength += 1;
                    } else {
                        afterWhiteSpace.append(c);
                    }
                    break;
                case '\t':
                    if (insideWhiteSpace) {
                        whiteSpaceLength += getIndentOptions().TAB_SIZE;
                    } else {
                        afterWhiteSpace.append(c);
                    }

                    break;
                default:
                    insideWhiteSpace = false;
                    afterWhiteSpace.append(c);
            }
        }
        if (line > 0) {
            createWhiteSpace(whiteSpaceLength + shift, buffer);
        }
        buffer.append(afterWhiteSpace.toString());

        replace(elementRange, buffer.toString());

        return buffer.length();
    }

    private void createWhiteSpace(final int whiteSpaceLength, StringBuilder buffer) {
        if (whiteSpaceLength < 0) {
            return;
        }

        StringUtil.repeatSymbol(buffer, ' ', whiteSpaceLength);
    }

    private CommonCodeStyleSettings.IndentOptions getIndentOptions() {
        return settings.getIndentOptions();
    }

    public Project getProject() {
        return myProject;
    }

    @Nullable
    public static String mergeWsWithCdataMarker(String whiteSpace, final String s, final int cdataPos) {
        final int firstCrInGeneratedWs = whiteSpace.indexOf('\n');
        final int secondCrInGeneratedWs = firstCrInGeneratedWs != -1
                ? whiteSpace.indexOf('\n', firstCrInGeneratedWs + 1) : -1;
        final int firstCrInPreviousWs = s.indexOf('\n');
        final int secondCrInPreviousWs = firstCrInPreviousWs != -1 ? s.indexOf('\n', firstCrInPreviousWs + 1) : -1;

        boolean knowHowToModifyCData = false;

        if (secondCrInPreviousWs != -1 && secondCrInGeneratedWs != -1 && cdataPos > firstCrInPreviousWs
                && cdataPos < secondCrInPreviousWs) {
            whiteSpace = whiteSpace.substring(0, secondCrInGeneratedWs)
                    + s.substring(firstCrInPreviousWs + 1, secondCrInPreviousWs)
                    + whiteSpace.substring(secondCrInGeneratedWs);
            knowHowToModifyCData = true;
        }
        if (!knowHowToModifyCData) {
            whiteSpace = null;
        }
        return whiteSpace;
    }

    private void replace(TextRange range, String whiteSpace) {
        String convertedWhiteSpace = StringUtil.convertLineSeparators(whiteSpace, "\n");
        int startOffset = convertOffset(range.getStartOffset());
        int endOffset = convertOffset(range.getEndOffset());
        ReplaceEdit edit = new ReplaceEdit(startOffset, endOffset - startOffset, convertedWhiteSpace);
        edits.add(edit);
    }

    private int convertOffset(int offset) {
        return offset;
    }

    @Override
    public void commitChanges() {
        StringBuilder newTextBuilder = new StringBuilder();
        int offset = 0;
        if (edits.isEmpty()) {
            newText = source;
            return;
        }
        
        for (ReplaceEdit edit : edits) {
            newTextBuilder.append(source.substring(offset, edit.startOffset));
            newTextBuilder.append(edit.replaceStr);
            offset = edit.startOffset+edit.length;
        }
        
        newTextBuilder.append(source.substring(offset));
        
        newText = newTextBuilder.toString();
    }

    public static String getNewText() {
        return newText;
    }

    private static class ReplaceEdit {

        public int getStartOffset() {
            return startOffset;
        }

        public int getLength() {
            return length;
        }

        public String getReplaceStr() {
            return replaceStr;
        }
        private final int startOffset;
        private final int length;
        private final String replaceStr;

        public ReplaceEdit(int startOffset, int endOffset, String replaceStr) {
            this.startOffset = startOffset;
            this.length = endOffset;
            this.replaceStr = replaceStr;
        }
    }

}
