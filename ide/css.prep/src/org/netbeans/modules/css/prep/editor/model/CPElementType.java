/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.prep.editor.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marekfukala
 */
public enum CPElementType {

    /**
     * Variable in the stylesheet body.
     *
     * $var: value;
     */
    VARIABLE_GLOBAL_DECLARATION("var_gl"),
    
    /**
     * Variable in a rule or mixin or other code block.
     *
     * $var: value;
     */
    VARIABLE_LOCAL_DECLARATION("var_loc"),
    
    /**
     * Variable declared as a param in a mixin declaration or for, each, while
     * block.
     *
     * @mixin left($dist) { ... }
     */
    VARIABLE_DECLARATION_IN_BLOCK_CONTROL("var_prms"),
    
    /**
     * Variable usage.
     *
     * .clz { color: $best; }
     */
    VARIABLE_USAGE("var_usg"),
    
    /**
     * Mixin declaration:
     * 
     * @mixin mymixin() { ... }
     */
    MIXIN_DECLARATION("mx"),
    
    /**
     * Mixin usage:
     * 
     * @include mymixin;
     */
    MIXIN_USAGE("mx_usg");
    
    private static Map<String, CPElementType> CODES_TO_ELEMENTS;
    
    private String indexCode;

    private CPElementType(String indexCode) {
        this.indexCode = indexCode;
    }
    
    public String getIndexCode() {
        return indexCode;
    }
    
    public boolean isOfTypes(CPElementType... types) {
        for(CPElementType type : types) {
            if(type == this) {
                return true;
            }
        }
        return false;
    }
    
    public static CPElementType forIndexCode(String indexCode) {
        if(CODES_TO_ELEMENTS == null) {
            CODES_TO_ELEMENTS = new HashMap<>();
            for(CPElementType et : values()) {
                CODES_TO_ELEMENTS.put(et.getIndexCode(), et);
            }
        }
        return CODES_TO_ELEMENTS.get(indexCode);
    }
}
