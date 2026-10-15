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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Various extensions to the displaying of the EL tokens
* is defined here. The tokens defined here are used
* by the el-drawing-layer.
*
* @author Petr Pisl
* @deprecated Will be replaced by Semantic Coloring
*/
@Deprecated
public class ELLayerTokenContext extends TokenContext {

    // Token category-ids

    // Numeric-ids for token-ids
    public static final int METHOD_ID     = 1;



    // Token-ids
    public static final BaseTokenID METHOD = new BaseTokenID("method", METHOD_ID);

    // Context instance declaration
    public static final ELLayerTokenContext context = new ELLayerTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();



    private ELLayerTokenContext() {
        super("jsp-el-layer-");

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

    }

}

