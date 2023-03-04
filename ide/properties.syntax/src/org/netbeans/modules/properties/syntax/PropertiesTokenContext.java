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

package org.netbeans.modules.properties.syntax;

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Token-ids and token-categories defined
* for the properties syntax.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class PropertiesTokenContext extends TokenContext {

    // Token numeric-IDs
    public static final int TEXT_ID         = 1; // plain text
    public static final int LINE_COMMENT_ID = 2; // line comment
    public static final int KEY_ID          = 3; // key
    public static final int EQ_ID           = 4; // equal-sign
    public static final int VALUE_ID        = 5; // value
    public static final int EOL_ID          = 6; // EOL

    // TokenIDs
    public static final BaseTokenID TEXT
    = new BaseTokenID("text", TEXT_ID);
    public static final BaseTokenID LINE_COMMENT
    = new BaseTokenID("line-comment", LINE_COMMENT_ID);
    public static final BaseTokenID KEY
    = new BaseTokenID("key", KEY_ID);
    public static final BaseTokenID EQ
    = new BaseTokenID("equal-sign", EQ_ID);
    public static final BaseTokenID VALUE
    = new BaseTokenID("value", VALUE_ID);
    public static final BaseTokenID EOL
    = new BaseTokenID("EOL", EOL_ID);


    // Context instance declaration
    public static final PropertiesTokenContext context = new PropertiesTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private PropertiesTokenContext() {
        super("properties-");

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

    }

}

