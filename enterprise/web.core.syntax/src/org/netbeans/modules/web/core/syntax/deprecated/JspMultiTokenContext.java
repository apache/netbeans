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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.plain.PlainTokenContext;

/**
* Token context for JSP.
*
* @author Miloslav Metelka
* @deprecated Use JSP Lexer instead
*/
@Deprecated
public class JspMultiTokenContext extends TokenContext {

    // Jsp token numericIDs
    public static final int ERROR_ID              =  1;

    /** jsp-error token-id */
    public static final BaseTokenID ERROR = new BaseTokenID("error", ERROR_ID); // NOI18N

    // Context instance declaration
    public static final JspMultiTokenContext context = new JspMultiTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    /** Path for jsp-tags in jsp */
    public static final TokenContextPath jspTagContextPath
        = context.getContextPath(JspTagTokenContext.contextPath);

    /** Path for Expression Language tokens in jsp */
    public static final TokenContextPath elContextPath
        = context.getContextPath(ELTokenContext.contextPath);

    /** Path for java tokens in jsp */
    public static final TokenContextPath javaScriptletContextPath
        = context.getContextPath(JspJavaFakeTokenContext.JavaScriptletTokenContext.contextPath);

    public static final TokenContextPath javaExpressionContextPath
        = context.getContextPath(JspJavaFakeTokenContext.JavaExpressionTokenContext.contextPath);

    public static final TokenContextPath javaDeclarationContextPath
        = context.getContextPath(JspJavaFakeTokenContext.JavaDeclarationTokenContext.contextPath);

    
    /** Path for HTML tokens in jsp */
    public static final TokenContextPath htmlContextPath
        = context.getContextPath(HtmlTokenContext.contextPath);

    /** Path for plain tokens in jsp */
    public static final TokenContextPath plainContextPath
        = context.getContextPath(PlainTokenContext.contextPath);


    private JspMultiTokenContext() {
        super("jsp-", new TokenContext[] {  // NOI18N
                JspTagTokenContext.context,
                ELTokenContext.context,
                JspJavaFakeTokenContext.JavaScriptletTokenContext.context,
                JspJavaFakeTokenContext.JavaDeclarationTokenContext.context,
                JspJavaFakeTokenContext.JavaExpressionTokenContext.context,
                HtmlTokenContext.context,
                PlainTokenContext.context
            }
        );

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

    }

}

