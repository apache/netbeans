/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.core.syntax.deprecated;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.core.syntax.deprecated.JspJavaFakeTokenContext;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.modules.web.core.syntax.deprecated.HtmlTokenContext;
import org.netbeans.editor.ext.plain.PlainTokenContext;
import org.netbeans.modules.web.core.syntax.deprecated.ELTokenContext;

/**
* Token context for JSP.
*
* @author Miloslav Metelka
* @deprecated Use JSP Lexer instead
*/

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

