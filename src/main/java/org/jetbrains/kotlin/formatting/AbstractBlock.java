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
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.TokenType;
import java.util.List;

/**
 *
 * @author Alexander.Baratynski
 */
public abstract class AbstractBlock implements ASTBlock {

    private final ASTNode myNode;
    private final Wrap myWrap;
    private final Alignment myAlignment;
    private List<Block> mySubBlocks = null;
    private Boolean myIncomplete = null;
    private final List<Block> EMPTY = Lists.newArrayList();
    protected boolean isBuildIndentsOnly = false;
    
    public AbstractBlock(ASTNode myNode, Wrap myWrap, Alignment myAlignment) {
        this.myNode = myNode;
        this.myWrap = myWrap;
        this.myAlignment = myAlignment;
    }
    
    protected abstract List<Block> buildChildren();
    
    @Override
    public ASTNode getNode() {
        return myNode;
    }

    @Override
    public TextRange getTextRange() {
        return myNode.getTextRange();
    }

    @Override
    public List<Block> getSubBlocks() {
        if (mySubBlocks == null) {
            mySubBlocks = buildChildren();
        }
        
        return mySubBlocks;
    }

    @Override
    public Wrap getWrap() {
        return myWrap;
    }

    @Override
    public Indent getIndent() {
        return null;
    }

    @Override
    public Alignment getAlignment() {
        return myAlignment;
    }

    @Override
    public ChildAttributes getChildAttributes(int i) {
        return new ChildAttributes(getChildIndent(), getFirstChildAlignment());
    }

    @Override
    public boolean isIncomplete() {
        if (myIncomplete == null) {
            myIncomplete = isIncomplete(getNode());
        }
        
        return myIncomplete;
    }
    
    private boolean isIncomplete(ASTNode node) {
        if (node == null) {
            return false;
        }
        ASTNode lastChild = node.getLastChildNode();
        
        while (lastChild != null && lastChild.getElementType() == TokenType.WHITE_SPACE) {
            lastChild = lastChild.getTreePrev();
        }
        
        if (lastChild == null) {
            return false;
        }
        
        if (lastChild.getElementType() == TokenType.ERROR_ELEMENT) {
            return true;
        }
        
        return isIncomplete(lastChild);
    }
    
    private Alignment getFirstChildAlignment() {
        for (Block subBlock : getSubBlocks()) {
            if (subBlock.getAlignment() != null) {
                return subBlock.getAlignment();
            }
        }
        
        return null;
    }
    
    protected Indent getChildIndent() {
        return null;
    }
    
    @Override
    public String toString() {
        return getNode().getText() + " " + getTextRange();
    }
    
}
