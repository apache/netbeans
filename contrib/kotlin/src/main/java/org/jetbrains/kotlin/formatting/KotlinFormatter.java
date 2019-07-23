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

import com.intellij.formatting.DependentSpacingRule;
import com.intellij.formatting.FormatterImpl;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.formatting.DependantSpacingImpl;
import org.jetbrains.kotlin.idea.formatter.KotlinSpacingBuilderUtil;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import org.jetbrains.kotlin.idea.formatter.KotlinSpacingRulesKt;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinFormatter {
    
    private final String source;
    private final String fileName;
    private final KtPsiFactory psiFactory;
    private final String lineSeparator;
    private final KtFile ktFile;
    
    public KotlinFormatter(String source, String fileName, KtPsiFactory psiFactory, String lineSeparator) {
        this.source = source;
        this.fileName = fileName;
        this.psiFactory = psiFactory;
        this.lineSeparator = lineSeparator;
        ktFile = KotlinFormatterUtils.createKtFile(source, psiFactory, fileName);
    }
    
    public String formatCode() {
        new FormatterImpl();
        KotlinBlock rootBlock = new KotlinBlock(ktFile.getNode(),
                NodeAlignmentStrategy.getNullStrategy(),
                Indent.getNoneIndent(),
                null,
                KotlinFormatterUtils.getSettings(),
                KotlinSpacingRulesKt.createSpacingBuilder(
                    KotlinFormatterUtils.getSettings(), KotlinSpacingBuilderUtilImpl.INSTANCE));
        return KotlinFormatterUtils.reformatAll(ktFile, rootBlock, 
                KotlinFormatterUtils.getSettings(), source);
    }
    
    static class KotlinSpacingBuilderUtilImpl implements KotlinSpacingBuilderUtil {
        
        public static KotlinSpacingBuilderUtilImpl INSTANCE = 
                new KotlinSpacingBuilderUtilImpl();
        
        private KotlinSpacingBuilderUtilImpl(){}
        
        @Override
        public Spacing createLineFeedDependentSpacing(int minSpaces,
                int maxSpaces, int minimumLineFeeds, boolean keepLineBreaks,
                int keepBlankLines, TextRange dependency, DependentSpacingRule rule) {
            return new DependantSpacingImpl(minSpaces, maxSpaces, dependency, keepLineBreaks,
                keepBlankLines, rule);
        }
        
        @Override
        public ASTNode getPreviousNonWhitespaceLeaf(ASTNode node) {
            return FormatterUtil.getPreviousNonWhitespaceLeaf(node);
        }
        
        @Override
        public boolean isWhitespaceOrEmpty(ASTNode node) {
            return FormatterUtil.isWhitespaceOrEmpty(node);
        }
        
    }
    
}
