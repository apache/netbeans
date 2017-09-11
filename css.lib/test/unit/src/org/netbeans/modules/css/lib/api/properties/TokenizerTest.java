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
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.api.properties.Token;
import java.util.Arrays;
import java.util.List;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.css.lib.CssTestBase;

/**
 *
 * @author marekfukala
 */
public class TokenizerTest extends CssTestBase {

    public TokenizerTest(String name) {
        super(name);
    }
    
    public void testMoveNextAndPrevious() {
        Tokenizer t = new Tokenizer("a b c");
        
        assertEquals(-1, t.tokenIndex());
        assertEquals(3, t.tokensCount());
        
        assertFalse(t.movePrevious());
        
        assertTrue(t.moveNext());
        assertFalse(t.movePrevious());
        
        assertEquals(-1, t.tokenIndex());
        assertEquals("a", t.token().image().toString());

        assertTrue(t.moveNext());
        assertEquals(1, t.tokenIndex());
        assertEquals("b", t.token().image().toString());
        
        assertTrue(t.moveNext());
        assertEquals(2, t.tokenIndex());
        assertEquals("c", t.token().image().toString());
        
        assertFalse(t.moveNext());
        
        
    }
    public void testMoveToIndex() {
        Tokenizer t = new Tokenizer("a b c");

        assertEquals(-1, t.tokenIndex());
        t.move(0);
        
        assertEquals(0,t.tokenIndex());
        assertEquals("a", t.token().image().toString());
        
        t.move(2);
        assertEquals(2,t.tokenIndex());
        assertEquals("c", t.token().image().toString());
        
    }
    
    public void testEmptyInput() {
        Tokenizer t = new Tokenizer("");

        assertEquals(-1, t.tokenIndex());
        assertFalse(t.moveNext());
        assertNull(t.token());
        
        t.move(0);
        assertFalse(t.moveNext());
        
        assertNull(t.token());
    }
    
    public void testEndOfInput() {
        Tokenizer t = new Tokenizer("x");

        assertTrue(t.moveNext());
        assertNotNull(t.token());
        
        assertFalse(t.moveNext());
        assertNotNull(t.token()); //still points to the last token
        
    }

    public void testFillList() {
        Tokenizer t = new Tokenizer("bla , ble bli,blo,,blu bly,oh/eh");

        assertTokens(t,
                "bla", ",", "ble", "bli", ",", "blo", ",", ",", "blu", "bly",
                ",", "oh", "/", "eh");
    }

    public void testFillListWithQuotedValues() {
        Tokenizer t = new Tokenizer("'Times New Roman',serif");
        assertTokens(t,
                "'Times New Roman'", ",", "serif");
    }

    public void testFillListWithBraces() {
        Tokenizer t = new Tokenizer("rect(20,30,40)");
        assertTokens(t,
                "rect", "(", "20", ",", "30", ",", "40", ")");
    }

    public void testFillListWithNewLine() {
        Tokenizer t = new Tokenizer("marek jitka \n");
        assertTokens(t,
                "marek", "jitka");

    }

    public void testFillListWithURL() {
        Tokenizer t = new Tokenizer("url(http://www.redballs.com/redball.png)");
        assertTokens(t,
                "url(http://www.redballs.com/redball.png)");
    }
    
    public void testTokenizeMinusLen() {
        Tokenizer t = new Tokenizer("-60px");
        assertTokens(t,
                "-", "60px");
    }

    private void assertTokens(Tokenizer tokenizer, String... expected) {
        List<String> exp = Arrays.asList(expected);
        while (tokenizer.moveNext()) {
            Token token = tokenizer.token();
            String t = token.image().toString();
            if (exp.size() > tokenizer.tokenIndex()) {
                String et = exp.get(tokenizer.tokenIndex());
                assertEquals(et, t);
            } else {
                throw new AssertionFailedError(
                        String.format("Unexpected tokens: %s",
                        dumpList(tokenizer.tokensList().subList(tokenizer.tokenIndex(), tokenizer.tokensCount()))));
            }
        }


        if (tokenizer.tokensCount() < exp.size()) {
            throw new AssertionFailedError(
                    String.format("Missing expected tokens: %s", dumpList(exp.subList(tokenizer.tokensCount(), exp.size()))));
        }


    }
}
