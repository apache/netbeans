/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

/**
 *
 * @author marekfukala
 */
public class ExtCss3Parser extends Css3Parser {

    //less css unit tests sets this
    static boolean isLessSource_unit_tests = false;
    static boolean isScssSource_unit_tests = false;
    
    private boolean isLessSource = isLessSource_unit_tests;
    private boolean isScssSource = isScssSource_unit_tests;
    
    public ExtCss3Parser(TokenStream input, NbParseTreeBuilder dbg, String mimeType) {
        super(input, dbg);        
        if(mimeType != null) {
            this.isLessSource = mimeType.equals("text/less");
            this.isScssSource = mimeType.equals("text/scss");
        }
    }

    public ExtCss3Parser(TokenStream input, int port, RecognizerSharedState state) {
        super(input, port, state);
    }

    public ExtCss3Parser(TokenStream input) {
        super(input);
    }

    @Override
    protected boolean isLessSource() {
        return isLessSource;
    }

    @Override
    protected boolean isScssSource() {
        return isScssSource;
    }
    
    @Override
    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
        //disable the default token auto-insertion/deletion recovery
        throw new MismatchedTokenException(ttype, input);
    }

    @Override
    public void emitErrorMessage(String msg) {
        //no-op since errors are reported via NbParseTreeBuilder (an impl of parser's DebugEventListener)
    }

    /** Consume tokens until one matches the given token set */
    @Override
    public void consumeUntil(IntStream i, BitSet set) {
//        System.out.println("consumeUntil(" + set.toString(getTokenNames()) + ")");
        Token ttype;
        List<Token> skipped = new ArrayList<>();
        beginResync();
        try {
            while ((ttype = input.LT(1)) != null && ttype.getType() != Token.EOF && !set.member(ttype.getType())) {
//            System.out.println("consume during recover LA(1)=" + getTokenNames()[input.LA(1)]);
                input.consume();
                skipped.add(ttype);
            }
        } finally {
            endResync();
        }
        ((NbParseTreeBuilder) dbg).consumeSkippedTokens(skipped);
    }
}
