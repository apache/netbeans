/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.api.completion.util;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 * Holder class for the context of a given completion.
 * This means the two surrounding Lexer-tokens before and after the completion point are stored here.
 *
 * @author Martin Janicek
 */
public class CompletionSurrounding {

    // b2    b1      |       a1        a2
    // class MyClass extends BaseClass {
    public Token<GroovyTokenId> beforeLiteral;
    public Token<GroovyTokenId> before2;
    public Token<GroovyTokenId> before1;
    public Token<GroovyTokenId> active;
    public Token<GroovyTokenId> after1;
    public Token<GroovyTokenId> after2;
    public Token<GroovyTokenId> afterLiteral;
    public TokenSequence<GroovyTokenId> ts; // we keep the sequence with us.

    
    public CompletionSurrounding(
        Token<GroovyTokenId> beforeLiteral,
        Token<GroovyTokenId> before2,
        Token<GroovyTokenId> before1,
        Token<GroovyTokenId> active,
        Token<GroovyTokenId> after1,
        Token<GroovyTokenId> after2,
        Token<GroovyTokenId> afterLiteral,
        TokenSequence<GroovyTokenId> ts) {

        this.beforeLiteral = beforeLiteral;
        this.before2 = before2;
        this.before1 = before1;
        this.active = active;
        this.after1 = after1;
        this.after2 = after2;
        this.afterLiteral = afterLiteral;
        this.ts = ts;
    }
}
