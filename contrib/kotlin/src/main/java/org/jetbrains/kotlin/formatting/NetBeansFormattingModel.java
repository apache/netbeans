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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.formatting.FormattingDocumentModel;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.WhiteSpaceFormattingStrategy;
import com.intellij.psi.formatter.WhiteSpaceFormattingStrategyFactory;

public class NetBeansFormattingModel implements FormattingDocumentModel {
    
    private final WhiteSpaceFormattingStrategy myWhiteSpaceStrategy;
    
    @NotNull
    private final Document myDocument;
    private final PsiFile myFile;
    
    private static final Logger LOG = Logger.getInstance("#com.intellij.psi.formatter.FormattingDocumentModelImpl");
    private final CodeStyleSettings mySettings;
    
    private final int maxDepthToCheck = 10;
    private final String applyChangeStateClassName = "com.intellij.formatting.FormatProcessor$ApplyChangesState";
    private final String prepareMethodName = "prepare";
    
    private final boolean forLineIndentation;
    
    public NetBeansFormattingModel(
            @NotNull final Document document, 
            PsiFile file, 
            CodeStyleSettings settings,
            boolean forLineIndentation) {
        myDocument = document;
        myFile = file;
        if (file != null) {
            Language language = file.getLanguage();
            myWhiteSpaceStrategy = WhiteSpaceFormattingStrategyFactory.getStrategy(language);
        } else {
            myWhiteSpaceStrategy = WhiteSpaceFormattingStrategyFactory.getStrategy();
        }
        mySettings = settings;
        this.forLineIndentation = forLineIndentation;
    }
    
    @Nullable
    public static Document getDocumentToBeUsedFor(final PsiFile file) {
        final Project project = file.getProject();
        final Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document == null)
            return null;
        if (PsiDocumentManager.getInstance(project).isUncommited(document))
            return null;
        
        return document;
    }
    
    @Override
    public int getLineNumber(int offset) {
        if (offset > myDocument.getTextLength()) {
            LOG.error(String.format("Invalid offset detected (%d). Document length: %d. Target file: %s", offset,
                    myDocument.getTextLength(), myFile));
        }
        return myDocument.getLineNumber(offset);
    }
    
    @Override
    public int getLineStartOffset(int line) {
        return myDocument.getLineStartOffset(line);
    }
    
    @Override
    public CharSequence getText(final TextRange textRange) {
        if (textRange.getStartOffset() < 0 || textRange.getEndOffset() > myDocument.getTextLength()) {
            LOG.error(String.format(
                    "Please submit a ticket to the tracker and attach current source file to it!%nInvalid processing detected: given text "
                            + "range (%s) targets non-existing regions (the boundaries are [0; %d)). File's language: %s",
                    textRange, myDocument.getTextLength(), myFile.getLanguage()));
        }
        return myDocument.getCharsSequence().subSequence(textRange.getStartOffset(), textRange.getEndOffset());
    }
    
    @Override
    public int getTextLength() {
        return myDocument.getTextLength();
    }
    
    @NotNull
    @Override
    public Document getDocument() {
        if (forLineIndentation) { 
            return myDocument;
        }
        
        int i = 0;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (i > maxDepthToCheck) {
                break;
            }
            
            if (prepareMethodName.equals(element.getMethodName()) && applyChangeStateClassName.equals(element.getClassName())) {
                return KotlinFormatterUtils.getMockDocument(myDocument);
            }
            
            i++;
        }
        return myDocument;
    }
    
    public PsiFile getFile() {
        return myFile;
    }
    
    @Override
    public boolean containsWhiteSpaceSymbolsOnly(int startOffset, int endOffset) {
        WhiteSpaceFormattingStrategy strategy = myWhiteSpaceStrategy;
        if (strategy.check(myDocument.getCharsSequence(), startOffset, endOffset) >= endOffset) {
            return true;
        }
        return false;
    }
    
    @NotNull
    @Override
    public CharSequence adjustWhiteSpaceIfNecessary(@NotNull CharSequence whiteSpaceText, int startOffset,
            int endOffset, @Nullable ASTNode nodeAfter, boolean changedViaPsi) {
        if (!changedViaPsi) {
            return myWhiteSpaceStrategy.adjustWhiteSpaceIfNecessary(whiteSpaceText, myDocument.getCharsSequence(),
                    startOffset, endOffset, mySettings, nodeAfter);
        }
        
        final PsiElement element = myFile.findElementAt(startOffset);
        if (element == null) {
            return whiteSpaceText;
        } else {
            return myWhiteSpaceStrategy.adjustWhiteSpaceIfNecessary(whiteSpaceText, element, startOffset, endOffset,
                    mySettings);
        }
    }
    
    public static boolean canUseDocumentModel(@NotNull Document document, @NotNull PsiFile file) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(file.getProject());
        return !psiDocumentManager.isUncommited(document) && !psiDocumentManager.isDocumentBlockedByPsi(document)
                && file.getText().equals(document.getText());
    }
}