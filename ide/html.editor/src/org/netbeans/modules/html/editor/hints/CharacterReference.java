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
public class CharacterReference extends PatternRule {

    private static final String[] PATTERNS_SOURCES = new String[]{
        "Character reference was not terminated by a semicolon",
        "Source text is not in Unicode Normalization Form C",
        "The string following .&. was interpreted as a character reference",
        "Named character reference was not terminated by a semicolon",
        ".&. did not start a character reference",
        "Character reference expands to",
        "A numeric character reference expanded to",
        "Character reference outside the permissible Unicode range."
        
        
    }; //NOI18N
    
    private static final Pattern[] PATTERNS = buildPatterns(PATTERNS_SOURCES);

    @Override
    public Pattern[] getPatterns() {
        return PATTERNS;
    }


}
