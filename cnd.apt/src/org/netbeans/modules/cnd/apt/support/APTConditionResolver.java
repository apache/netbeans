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
