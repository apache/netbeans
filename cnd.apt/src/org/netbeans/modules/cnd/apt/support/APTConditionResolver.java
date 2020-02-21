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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.apt.support;

import java.math.BigInteger;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTBigIntegerExprParser;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTExprParser;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTIfCondition;
import org.netbeans.modules.cnd.apt.structure.APTIfdef;
import org.netbeans.modules.cnd.apt.structure.APTIfndef;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.CharSequences;

/**
 * evaluator to resolve preproc expressions of APT condition nodes
 */
public final class APTConditionResolver {
    private static final boolean APT_EXPR_TRACE = Boolean.getBoolean("aptexpr.trace"); // NOI18N

    /**
     * Creates a new instance of APTConditionResolver
     */
    private APTConditionResolver() {
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("BC")
    public static boolean evaluate(APT cond, APTMacroCallback callback) throws TokenStreamException {
        boolean res = false;
        switch (cond.getType()) {
            case APT.Type.IFNDEF:
                {
                    APTToken macro = ((APTIfndef)cond).getMacroName();
                    res = macro == null ? PREPROCESSOR_ERROR_DEFAULT_RETURN_VALUE : !isDefined(macro, callback);
                }
                break;
            case APT.Type.IFDEF:
                {
                    APTToken macro = ((APTIfdef)cond).getMacroName();
                    res = macro == null ? PREPROCESSOR_ERROR_DEFAULT_RETURN_VALUE : isDefined(macro, callback);
                }
                break;
            case APT.Type.IF:
            case APT.Type.ELIF:
                Boolean out = evaluate((APTIfCondition)cond, callback, false);
                if (out == null) {
                    // #214618: Overflow on macro evalution
                    out = evaluate((APTIfCondition)cond, callback, true);
                } else if (APT_EXPR_TRACE) {
                    // check that BigInteger & long based gives the same result
                    Boolean val = evaluate((APTIfCondition)cond, callback, true);
                    assert out.equals(val) : "different values when evaluate " + cond + " " + val.booleanValue() + " vs. " + out.booleanValue();
                }
                res = out.booleanValue();
                break;
            default:
                assert (false) : "support only #ifdef,#ifndef,#if,#elif"; // NOI18N
        }
        return res;
    }

    private static boolean isDefined(APTToken macro, APTMacroCallback callback) {
        return callback.isDefined(macro);
    }

    private static final CharSequence __CPLUSPLUS = CharSequences.create("__cplusplus"); // NOI18N

    private static Boolean evaluate(APTIfCondition apt, APTMacroCallback callback, boolean bigIntegers) throws TokenStreamException {
        TokenStream expr = apt.getCondition();
        Boolean res;
        TokenStream expandedTS = expandTokenStream(expr, callback);
        // in C++ mode #if true means 1 for PP expression, all other IDs are 0
        boolean treatTrueIDAsValueOne = callback.isDefined(__CPLUSPLUS);
        try {
            if (bigIntegers) {
                APTBigIntegerExprParser parser = new APTBigIntegerExprParser(expandedTS, callback, treatTrueIDAsValueOne ? BigInteger.ONE : BigInteger.ZERO);
                BigInteger r = parser.expr();
                if (APT_EXPR_TRACE) {
                    System.out.println("Value is " + r); // NOI18N
                }
                if (BigInteger.ZERO.equals(r)) {
                    res = Boolean.FALSE;
                } else {
                    res = Boolean.TRUE;
                }
            } else {
                APTExprParser parser = new APTExprParser(expandedTS, callback, treatTrueIDAsValueOne ? 1L : 0L);
                long r = parser.expr();
                if (APT_EXPR_TRACE) {
                    System.out.println("Value is " + r); // NOI18N
                }
                if (parser.areBigValuesUsed()) {
                    // we don't know the answer due to big integers arithmetics
                    res = null;
                } else {
                    res = (r==0)?Boolean.FALSE:Boolean.TRUE;
                }
            }

            if (APTUtils.LOG.isLoggable(Level.FINE)) {
                APTUtils.LOG.log(Level.FINE,
                        "stream {0} \n was expanded for condition resolving to \n {1} \n with result {2}", // NOI18N
                        new Object[] { expr, expandedTS, res });
            }
        } catch (NullPointerException ex) {
            APTUtils.LOG.log(Level.SEVERE,
                    "exception on resolving expression: {0}\n{1}", // NOI18N
                    new Object[] {expr, ex});
            res = false;
        } catch (ArithmeticException ex) {
            if (DebugUtils.STANDALONE) {
                System.err.printf("arithmetic error \"%s\" on resolving expression:%n\t %s%n", // NOI18N
                    ex.getMessage(), expr);
            } else {
                APTUtils.LOG.log(Level.WARNING,
                    "arithmetic error \"{0}\" on resolving expression\n: {1}", // NOI18N
                    new Object[] {ex.getMessage(), expr});
            }
            res = PREPROCESSOR_ERROR_DEFAULT_RETURN_VALUE;
        }

        return res;
    }

    private static TokenStream expandTokenStream(TokenStream orig, APTMacroCallback callback) {
        // need to generate expanded token stream to have all macro substituted
        return new APTExpandedStream(orig, callback, true);
    }

    public static final boolean PREPROCESSOR_ERROR_DEFAULT_RETURN_VALUE = false;
}
