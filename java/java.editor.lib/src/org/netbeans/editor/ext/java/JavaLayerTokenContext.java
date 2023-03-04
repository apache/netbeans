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

package org.netbeans.editor.ext.java;

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;

/**
* Various extensions to the displaying of the java tokens
* is defined here. The tokens defined here are used
* by the java-drawing-layer.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaLayerTokenContext extends TokenContext {

    // Token category-ids

    // Numeric-ids for token-ids
    public static final int METHOD_ID     = 1;

    // Token-categories
//    public static final BaseTokenCategory KEYWORDS
//    = new BaseTokenCategory("keywords", KEYWORDS_ID);


    // Token-ids
    public static final BaseTokenID METHOD
    = new BaseTokenID("method", METHOD_ID); // NOI18N

    // Context instance declaration
    public static final JavaLayerTokenContext context = new JavaLayerTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();


    private JavaLayerTokenContext() {
        super("java-layer-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Utilities.annotateLoggable(e);
        }

    }

}

