/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.modelutil;

import javax.swing.text.AttributeSet;

/**
 *
 */
public interface FontColorProvider {
    AttributeSet getColor(Entity color);
    String getMimeType();

    public enum Entity {

        PREPROCESSOR_DIRECTIVE("preprocessor"), // NOI18N
        INACTIVE_CODE("cc-highlighting-inactive"), // NOI18N
        //Macro Defined in Code
        DEFINED_MACRO("cc-highlighting-macros"), // NOI18N
        //Predefined Macros (compiler and compile time)
        SYSTEM_MACRO("cc-highlighting-macros-system"), // NOI18N
        //Macro Defined in Project (in comand line -D)
        USER_MACRO("cc-highlighting-macros-user"), // NOI18N
        // enum
        ENUM("cc-highlighting-enum"), // NOI18N
        ENUM_USAGE("cc-highlighting-enum-usage"), // NOI18N
        // enumerator
        ENUMERATOR("cc-highlighting-enumerator"), // NOI18N
        ENUMERATOR_USAGE("cc-highlighting-enumerator-usage"), // NOI18N
        // class
        CLASS("cc-highlighting-class"), // NOI18N
        CLASS_USAGE("cc-highlighting-class-usage"), // NOI18N
        //function
        FUNCTION("cc-highlighting-function"), // NOI18N
        FUNCTION_USAGE("cc-highlighting-function-usage"), // NOI18N
        CLASS_FIELD("cc-highlighting-class-fields"), // NOI18N
        MARK_OCCURENCES("mark-occurrences"), // NOI18N
        TYPEDEF("cc-highlighting-typedefs"), // NOI18N
        UNUSED_VARIABLES("cc-highlighting-unused-variables"); // NOI18N

        private final String resourceName;

        Entity(String resourceName) {
            this.resourceName = resourceName;
        }

        public String getResourceName() {
            return resourceName;
        }
    }
    
}
