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


import com.google.common.collect.Sets;
import com.intellij.formatting.Alignment;
import com.intellij.psi.tree.IElementType;
import java.util.Collection;
import java.util.Set;
/**
 *
 * @author Alexander.Baratynski
 */
public abstract class KotlinAlignmentStrategy {
    
    private static KotlinAlignmentStrategy nullStrategy = wrap(null, null, null);
    
    public static KotlinAlignmentStrategy getNullStrategy() {
        return nullStrategy;
    }
    
    public static KotlinAlignmentStrategy wrap(Alignment alignment, IElementType... filterTypes) {
        return new SharedAlignmentStrategy(alignment, true, filterTypes);
    }
    
    public static KotlinAlignmentStrategy wrap(Alignment alignment, boolean ignoreFilterTypes,
            IElementType... filterTypes) {
        return new SharedAlignmentStrategy(alignment, ignoreFilterTypes, filterTypes);
    }
    
    public static AlignmentPerTypeStrategy createAlignmentPerTypeStrategy(Collection<IElementType> targetTypes,
            boolean allowBackwardShift) {
        return new AlignmentPerTypeStrategy(targetTypes, null, allowBackwardShift, Alignment.Anchor.LEFT);
    }
    
    public static AlignmentPerTypeStrategy createAlignmentPerStrategy(Collection<IElementType> targetTypes,
            IElementType parentType, boolean allowBackwardShift, Alignment.Anchor anchor) {
        Alignment.Anchor a = anchor;
        if (a == null) {
            a = Alignment.Anchor.LEFT;
        }
        return new AlignmentPerTypeStrategy(targetTypes, parentType, allowBackwardShift, a);
    }
    
    public abstract Alignment getAlignment(IElementType parentType, IElementType childType);
    
    public Alignment getAlignment(IElementType childType) {
        return getAlignment(null, childType);
    }
    
    private static class SharedAlignmentStrategy extends KotlinAlignmentStrategy {
        
        private final Alignment myAlignment;
        private final boolean myIgnoreFilterTypes;
        private final Set<IElementType> myFilterElementTypes;
        
        SharedAlignmentStrategy(Alignment myAlignment, boolean myIgnoreFilterTypes, 
                IElementType... disabledElementTypes) {
            this.myAlignment = myAlignment;
            this.myIgnoreFilterTypes = myIgnoreFilterTypes;
            this.myFilterElementTypes = Sets.newHashSet(disabledElementTypes);
        }
        
        @Override
        public Alignment getAlignment(IElementType parentType, IElementType childType) {
            if (myFilterElementTypes.contains(childType) != myIgnoreFilterTypes) {
                return myAlignment;
            } else return null;
        }
        
    }
    
}
