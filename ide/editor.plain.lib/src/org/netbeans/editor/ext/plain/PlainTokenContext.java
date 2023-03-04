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

package org.netbeans.editor.ext.plain;

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;

/**
 * Tokens used in formatting
 *
 * @author Miloslav Metelka
 * @deprecated If you need this class you are doing something wrong, 
 *   please ask on nbdev@netbeans.org.
 */
@Deprecated
public class PlainTokenContext extends TokenContext {

    // Numeric-ids for token-ids
    public static final int TEXT_ID       =  1;
    public static final int EOL_ID        =  2;

    public static final BaseTokenID TEXT
    = new BaseTokenID("text", TEXT_ID); // NOI18N

    public static final BaseImageTokenID EOL
    = new BaseImageTokenID("EOL", EOL_ID, "\n"); // NOI18N

    // Context declaration
    public static final PlainTokenContext context = new PlainTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private PlainTokenContext() {
        super("format-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Utilities.annotateLoggable(e);
        }

    }

}
