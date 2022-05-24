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
package org.netbeans.modules.html.editor.hints;

import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.HintSeverity;

/**
 *
 * @author marekfukala
 */
public class Ids extends PatternRule {

    private static final String[] PATTERNS_SOURCES = new String[]{
       "Duplicate ID",
       "The first occurrence of the ID value",
       "Attribute .id. declares a duplicate ID value",
        
        
    }; //NOI18N
    
    private static final Pattern[] PATTERNS = buildPatterns(PATTERNS_SOURCES);

    @Override
    public Pattern[] getPatterns() {
        return PATTERNS;
    }
    

}
