/*******************************************************************************
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
 *******************************************************************************/
package org.black.kotlin.highlighter.semanticanalyzer;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.netbeans.modules.csl.api.ColoringAttributes;

/**
 *
 * @author Александр
 */
public class KotlinHighlightingAttributesGetter {

    public static KotlinHighlightingAttributesGetter INSTANCE = 
            new KotlinHighlightingAttributesGetter();
    
    private KotlinHighlightingAttributesGetter() {}

    private KotlinHighlightingAttributes withAttributes(
            Function1<KotlinHighlightingAttributes, Unit> changeAttributes) {
        KotlinHighlightingAttributes attributes = new KotlinHighlightingAttributes();
        changeAttributes.invoke(attributes);
        return attributes;
    }

    public final KotlinHighlightingAttributes LOCAL_FINAL_VARIABLE 
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.LOCAL_VARIABLE;
                    return Unit.INSTANCE;
                }
            });
    
    public final KotlinHighlightingAttributes LOCAL_VARIABLE
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.LOCAL_VARIABLE;
                    attr.underline = true;
                    return Unit.INSTANCE;
                }
            });

        public final KotlinHighlightingAttributes PARAMETER_VARIABLE
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.PARAMETER;
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes FIELD
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.FIELD;
                    attr.underline = true;
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes FINAL_FIELD
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.FIELD;
                    return Unit.INSTANCE;
                }
            });

        public final KotlinHighlightingAttributes STATIC_FIELD
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.STATIC;
                    attr.underline = true;
                    return Unit.INSTANCE;
                }
            });

        public final KotlinHighlightingAttributes STATIC_FINAL_FIELD
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.STATIC;
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes TYPE_PARAMETER
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.TYPE_PARAMETER_USE;
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes ANNOTATION
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.ANNOTATION_TYPE;
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes ENUM_CLASS
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.ENUM;
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes INTERFACE
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.INTERFACE;
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes CLASS
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.CLASS;
//                    ColoringAttributes.
                    return Unit.INSTANCE;
                }
            });
        
        public final KotlinHighlightingAttributes FUNCTION_DECLARATION
            = withAttributes(new Function1<KotlinHighlightingAttributes, Unit>() {
                @Override
                public Unit invoke(KotlinHighlightingAttributes attr) {
                    attr.styleKey = ColoringAttributes.DECLARATION;
                    return Unit.INSTANCE;
                }
            });

    public class KotlinHighlightingAttributes {
        ColoringAttributes styleKey;
        boolean underline = false;
    }
}
