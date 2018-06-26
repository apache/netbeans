/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.core.syntax.completion;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class Util {
    public static int findPositionForJspDirective(BaseDocument doc){
        int insertPos = 0;
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<JspTokenId> ts = th.tokenSequence(JspTokenId.language());

        ts.moveStart();

        while (ts.moveNext() && (ts.token().id() == JspTokenId.COMMENT || ts.token().id() == JspTokenId.EOL)){
            insertPos = ts.offset() + ts.token().length();
        }

        TokenSequence<HTMLTokenId> tsHTML = ts.embedded(HTMLTokenId.language());

        if (tsHTML != null){
            tsHTML.moveStart();

            while (tsHTML.moveNext() && (tsHTML.token().id() == HTMLTokenId.WS || tsHTML.token().id() == HTMLTokenId.EOL
                    || tsHTML.token().id() == HTMLTokenId.TEXT && isWsToken(tsHTML.token().text()))) {
                insertPos = tsHTML.offset() + tsHTML.token().length();
            }
        }

        return insertPos;
    }

    private static boolean isWsToken(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))){
                return false;
            }
        }

        return true;
    }
}
