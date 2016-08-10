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

import com.intellij.formatting.Block;
import com.intellij.formatting.FormatTextRanges;
import com.intellij.formatting.FormatterImpl;
import com.intellij.formatting.Indent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions;
import org.jetbrains.kotlin.formatting.KotlinFormatter.KotlinSpacingBuilderUtilImpl;
import org.jetbrains.kotlin.idea.formatter.KotlinSpacingRulesKt;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import org.jetbrains.kotlin.utils.LineEndUtil;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinFormatterUtils {
    
    private static CodeStyleSettings settings = new CodeStyleSettings(true);
    
    public static CodeStyleSettings getSettings() {
        return settings;
    }
    
    private static KtPsiFactory createPsiFactory(Project project) {
        KotlinEnvironment environment = KotlinEnvironment.getEnvironment(project);
        return new KtPsiFactory(environment.getProject());
    }
    
    public static String formatCode(String source, String fileName, Project project, String lineSeparator) {
        return formatCode(source, fileName, createPsiFactory(project), lineSeparator);
    }
    
    public static String formatCode(String source, String fileName, KtPsiFactory psiFactory, String lineSeparator) {
        return new KotlinFormatter(source, fileName, psiFactory, lineSeparator).formatCode();
    }
    
    public static void reformatAll(KtFile containingFile, Block rootBlock, 
            CodeStyleSettings settings, String source ) {
        formatRange(containingFile, rootBlock, settings, source, containingFile.getTextRange());
    }
    
    public static void formatRange(String source, NetBeansDocumentRange range, 
            KtPsiFactory psiFactory, String fileName) {
        formatRange(source, range.toPsiRange(source), psiFactory, fileName);
    }
    
    public static void formatRange(String source, TextRange range, KtPsiFactory psiFactory, String fileName) {
        KtFile ktFile = createKtFile(source, psiFactory, fileName);
        Block rootBlock = new KotlinBlock(ktFile.getNode(),
                NodeAlignmentStrategy.getNullStrategy(),
                Indent.getNoneIndent(),
                null,   
                settings,
                KotlinSpacingRulesKt.createSpacingBuilder(settings, KotlinSpacingBuilderUtilImpl.INSTANCE));
        formatRange(ktFile, rootBlock, settings, source, range);
    }
    
    private static void formatRange(KtFile containingFile, Block rootBlock,
            CodeStyleSettings settings, String source, TextRange range) {
        NetBeansDocumentFormattingModel formattingModel = 
                buildModel(containingFile, rootBlock, settings, source, false);
        FormatTextRanges ranges = new FormatTextRanges(range, true);
        new FormatterImpl().format(formattingModel, settings, settings.getIndentOptions(), ranges, false);
    }
    
    private static void initializeSettings(IndentOptions options) {
        options.USE_TAB_CHARACTER = !IndenterUtil.isSpacesForTabs();
        options.INDENT_SIZE = IndenterUtil.getDefaultIndent();
        options.TAB_SIZE = IndenterUtil.getDefaultIndent();
    }
    
    private static NetBeansDocumentFormattingModel buildModel(KtFile ktFile,
            Block rootBlock, CodeStyleSettings settings, String source, 
            boolean forLineIndentation) {
        initializeSettings(settings.getIndentOptions());
        NetBeansFormattingModel formattingDocumentModel =
                new NetBeansFormattingModel(
                        new DocumentImpl(ktFile.getViewProvider().getContents(), true),
                    ktFile, settings, forLineIndentation);
        
        return new NetBeansDocumentFormattingModel(
                ktFile, rootBlock, formattingDocumentModel, source, settings);
    }
    
    // ???
    public static Document getMockDocument(Document document) {
        return document;
    }
    
    public static KtFile createKtFile(String source, KtPsiFactory psiFactory, String fileName) {
        return psiFactory.createFile(fileName, StringUtil.convertLineSeparators(source));
    }
    
    public static class NetBeansDocumentRange {
        private final int startOffset, endOffset;
        
        public NetBeansDocumentRange(int start, int end) {
            startOffset = start;
            endOffset = end;
        }
        
        public TextRange toPsiRange(String source) {
            int startPsiOffset = LineEndUtil.convertCrToDocumentOffset(source, startOffset);
            int endPsiOffset = LineEndUtil.convertCrToDocumentOffset(source, endOffset);
            
            return new TextRange(startPsiOffset, endPsiOffset);
        }
    }
}
