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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import java.util.List;
import org.jetbrains.kotlin.KtNodeTypes;
import org.jetbrains.kotlin.idea.formatter.CommonAlignmentStrategy;
import org.jetbrains.kotlin.idea.formatter.KotlinCommonBlock;
import org.jetbrains.kotlin.idea.formatter.KotlinSpacingBuilder;
import org.jetbrains.kotlin.lexer.KtTokens;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinBlock extends AbstractBlock {

    private final ASTNode node;
    private final CommonAlignmentStrategy myAlignmentStrategy;
    private final Indent indent;
    private final Wrap wrap;
    private final CodeStyleSettings mySettings;
    private final KotlinSpacingBuilder mySpacingBuilder;
    private KotlinCommonBlock kotlinDelegationBlock;
    
    public KotlinBlock(ASTNode node, CommonAlignmentStrategy myAlignmentStrategy,
            Indent indent, Wrap wrap, CodeStyleSettings mySettings,
            KotlinSpacingBuilder mySpacingBuilder) {
        super(node, wrap, myAlignmentStrategy.getAlignment(node));
        this.node = node;
        this.myAlignmentStrategy = myAlignmentStrategy;
        this.indent = indent;
        this.wrap = wrap;
        this.mySettings = mySettings;
        this.mySpacingBuilder = mySpacingBuilder;
        kotlinDelegationBlock = getKotlinCommonBlock();
    }
    
    private KotlinCommonBlock getKotlinCommonBlock() {
        return new KotlinCommonBlock(node, mySettings, mySpacingBuilder,myAlignmentStrategy) {
            
            @Override
            protected Block createBlock(ASTNode node, CommonAlignmentStrategy alignmentStrategy, 
                    Indent indent, Wrap wrap, CodeStyleSettings css, KotlinSpacingBuilder ksb) {
                return new KotlinBlock(node, alignmentStrategy, indent,
                    wrap, mySettings, mySpacingBuilder);
            }

            @Override
            protected ASTBlock createSyntheticSpacingNodeBlock(ASTNode node) {
                return new AbstractBlock(node, null, null) {
                    @Override
                    protected List<Block> buildChildren() {
                        return Lists.newArrayList();
                    }

                    @Override
                    public Spacing getSpacing(Block block, Block block1) {
                        return null;
                    }

                    @Override
                    public boolean isLeaf() {
                        return false;
                    }  
                };
            }

            @Override
            protected List<Block> getSubBlocks() {
                return KotlinBlock.super.getSubBlocks();
            }

            @Override
            protected ChildAttributes getSuperChildAttributes(int newChildIndex) {
                return KotlinBlock.super.getChildAttributes(newChildIndex);
            }

            @Override
            protected boolean isIncompleteInSuper() {
                return KotlinBlock.super.isIncomplete();
            }

            @Override
            protected CommonAlignmentStrategy getAlignmentForCaseBranch(boolean shouldAlignInColumns) {
                if (shouldAlignInColumns) {
                    return NodeAlignmentStrategy.fromTypes(
                            KotlinAlignmentStrategy.createAlignmentPerStrategy(Sets.newHashSet((IElementType) KtTokens.ARROW), 
                                KtNodeTypes.WHEN_ENTRY, true, null));
                } else {
                    return NodeAlignmentStrategy.getNullStrategy();
                }
            }

            @Override
            protected Alignment getAlignment() {
                return KotlinBlock.super.getAlignment();
            }

            @Override
            protected CommonAlignmentStrategy createAlignmentStrategy(boolean alignOption, 
                    Alignment defaultAlignment) {
                return NodeAlignmentStrategy.fromTypes(KotlinAlignmentStrategy.wrap(
                        createAlignment(alignOption, defaultAlignment)));
            }

            @Override
            protected CommonAlignmentStrategy getNullAlignmentStrategy() {
                return NodeAlignmentStrategy.fromTypes(KotlinAlignmentStrategy.wrap(null));
            }
            
            private Alignment createAlignment(boolean alignOption, Alignment defaultAlignment) {
                if (alignOption) {
                    return createAlignmentOrDefault(null, defaultAlignment);
                } else return defaultAlignment;
            }
            
            private Alignment createAlignmentOrDefault(Alignment base, Alignment defaultAlignment) {
                if (defaultAlignment != null) {
                    return defaultAlignment;
                }
                if (base == null) {
                    return Alignment.createAlignment();
                } else {
                    return Alignment.createChildAlignment(base);
                }
            }
            
        };
    }
    
    @Override
    public Indent getIndent() {
        return indent;
    }
    
    
    @Override
    protected List<Block> buildChildren() {
        return kotlinDelegationBlock.buildChildren();
    }

    @Override
    public Spacing getSpacing(Block child1, Block child2) {
        return mySpacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        return kotlinDelegationBlock.getChildAttributes(newChildIndex);
    }
    
    @Override
    public boolean isLeaf() {
        return kotlinDelegationBlock.isLeaf();
    }
    
}
