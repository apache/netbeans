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

import com.intellij.formatting.Alignment;
import com.intellij.lang.ASTNode;
import org.jetbrains.kotlin.idea.formatter.CommonAlignmentStrategy;

/**
 *
 * @author Alexander.Baratynski
 */
public abstract class NodeAlignmentStrategy extends CommonAlignmentStrategy {
    
    private static NodeAlignmentStrategy nullStrategy = fromTypes(KotlinAlignmentStrategy.getNullStrategy());
    
    public static NodeAlignmentStrategy fromTypes(KotlinAlignmentStrategy strategy) {
        return new AlignmentStrategyWrapper(strategy);
    }
    
    public static NodeAlignmentStrategy getNullStrategy(){
        return nullStrategy;
    }
    
    private static class AlignmentStrategyWrapper extends NodeAlignmentStrategy {
        private final KotlinAlignmentStrategy internalStrategy;
        
        AlignmentStrategyWrapper(KotlinAlignmentStrategy internalStrategy) {
            this.internalStrategy = internalStrategy;
        }
        
        @Override
        public Alignment getAlignment(ASTNode node) {
            ASTNode parent = node.getTreeParent();
            if (parent != null) {
                return internalStrategy.getAlignment(parent.getElementType(), node.getElementType());
            }
            
            return internalStrategy.getAlignment(node.getElementType());
        }
        
    }
    
}
