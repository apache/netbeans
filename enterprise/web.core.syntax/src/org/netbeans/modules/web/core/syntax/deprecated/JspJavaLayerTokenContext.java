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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.java.JavaLayerTokenContext;

/** Helper class for token context for Java methods (layer above the lexical layer provided by the JavaSyntax class.
* This is necessary to make java method coloring work properly in JSP
*
* @author Petr Jiricka
* @deprecated Will be replaced by Semantic Coloring
*/
@Deprecated
public class JspJavaLayerTokenContext extends TokenContext {

    private JspJavaLayerTokenContext() {
        super("jsp-", new TokenContext[] { JavaLayerTokenContext.context } );   // NOI18N
    }

    public static final JspJavaLayerTokenContext context = new JspJavaLayerTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

}

