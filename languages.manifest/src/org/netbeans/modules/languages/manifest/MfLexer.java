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

package org.netbeans.modules.languages.manifest;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
class MfLexer implements Lexer<MfTokenId> {

    private LexerRestartInfo<MfTokenId> info;
    private int                         state = 0;

    MfLexer (LexerRestartInfo<MfTokenId> info) {
        this.info = info;
        if (info.state () != null)
            state = (Integer) info.state ();
    }

    public Token<MfTokenId> nextToken () {
        LexerInput input = info.input ();
        if (state == 0) {
            int i = input.read ();
            if (i == '#') {
                do {
                    i = input.read ();
                } while (
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                do {
                    i = input.read ();
                } while (
                    i == '\n' ||
                    i == '\r'
                );
                input.backup (1);
                state = 0;
                return info.tokenFactory ().createToken (MfTokenId.COMMENT);
            }
            if (i == ':')
                i = input.read ();
            while (
                i != '\n' &&
                i != '\r' &&
                i != ':' &&
                i != LexerInput.EOF
            )
                i = input.read ();
            if (i == '\n' || i == '\r')
                do {
                    i = input.read ();
                } while (
                    i == '\n' ||
                    i == '\r'
                );
            if (i != LexerInput.EOF)
                input.backup (1);
            state = i == ':' ? 1 : 0;
            if (input.readLength() == 0) return null;
            return info.tokenFactory ().createToken (MfTokenId.KEYWORD);
        }
        if (state == 1) {
            input.read ();
            state = 2;
            return info.tokenFactory ().createToken (MfTokenId.OPERATOR);
        }
        int i = 0;
        do {
            i = input.read ();
            while (
                i != '\n' &&
                i != '\r' &&
                i != LexerInput.EOF
            )
                i = input.read ();
            do {
                i = input.read ();
            } while (
                i == '\n' ||
                i == '\r'
            );
        } while (i == ' ');
        if (i != LexerInput.EOF)
            input.backup (1);
        state = 0;
        if (input.readLength() == 0) return null;
        return info.tokenFactory ().createToken (MfTokenId.IDENTIFIER);
    }

    public Object state () {
        return state;
    }

    public void release () {
    }
}


