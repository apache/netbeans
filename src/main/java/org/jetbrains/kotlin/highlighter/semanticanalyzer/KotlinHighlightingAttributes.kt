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
package org.jetbrains.kotlin.highlighter.semanticanalyzer

import org.netbeans.modules.csl.api.ColoringAttributes

class KotlinHighlightingAttributes private constructor() {
    lateinit var styleKey: Set<ColoringAttributes>
    
    companion object {
        fun withAttributes(changeAttributes: KotlinHighlightingAttributes.() -> Unit): KotlinHighlightingAttributes {
            val attributes = KotlinHighlightingAttributes()
            attributes.changeAttributes()
            return attributes
        }
        
        val LOCAL_FINAL_VARIABLE = withAttributes { styleKey = setOf(ColoringAttributes.LOCAL_VARIABLE) }
        val LOCAL_VARIABLE = withAttributes { styleKey = setOf(ColoringAttributes.LOCAL_VARIABLE, 
                ColoringAttributes.CUSTOM2) }
        val PARAMETER_VARIABLE = withAttributes { styleKey = setOf(ColoringAttributes.PARAMETER) }
        val FIELD = withAttributes { styleKey = setOf(ColoringAttributes.FIELD, 
                ColoringAttributes.CUSTOM2,
                /*bold*/ColoringAttributes.ENUM) }
        val FINAL_FIELD = withAttributes { styleKey = setOf(ColoringAttributes.FIELD, 
                /*bold*/ColoringAttributes.ENUM) }
        val STATIC_FIELD = withAttributes { styleKey = setOf(ColoringAttributes.FIELD,
                ColoringAttributes.STATIC, 
                ColoringAttributes.CUSTOM2,
                /*bold*/ColoringAttributes.ENUM) }
        val STATIC_FINAL_FIELD = withAttributes { styleKey = setOf(ColoringAttributes.FIELD,
                ColoringAttributes.STATIC,
                /*bold*/ColoringAttributes.ENUM) }
        val TYPE_PARAMETER = withAttributes { styleKey = setOf(ColoringAttributes.TYPE_PARAMETER_USE) }
        val ANNOTATION = withAttributes { styleKey = setOf(ColoringAttributes.ANNOTATION_TYPE) }
        val ENUM_CLASS = withAttributes { styleKey = setOf(ColoringAttributes.ENUM) }
        val INTERFACE = withAttributes { styleKey = setOf(ColoringAttributes.INTERFACE) }
        val CLASS = withAttributes { styleKey = setOf(ColoringAttributes.CLASS) }
        val FUNCTION_DECLARATION = withAttributes { styleKey = setOf(ColoringAttributes.DECLARATION) }
        val SMART_CAST = withAttributes { styleKey = setOf(ColoringAttributes.CUSTOM1) }
        
    }
    
}