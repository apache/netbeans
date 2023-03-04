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

package org.netbeans.modules.debugger.jpda.expr;

import java.util.Random;

/**
 * Represents an pre-parsed Java expression to be later evaluated in a specific JVM context.
 * The expression can have a 1.4 or 1.5 syntax.
 *
 * 
 * Uses Compiler API
 * 1) Generate a method (like evaluate_<long random number>), which takes all local variables as arguments
 * 2) Add that method into the current source file
 * 3) Compile the current source file, like http://www.javabeat.net/javabeat/java6/articles/java_6_0_compiler_api_4.php
 * 4) Traverse the parsed tree and perform the evaluation - http://java.sun.com/javase/6/docs/jdk/api/javac/tree/com/sun/source/util/JavacTask.html
 * 5) Instead of (4) it would be cool to be able to do a hot-swap. But we can not add new methods. :-(
 * 
 * 
 * @author Martin Entlicher
 */
public class JavaExpression {

    public static final String LANGUAGE_JAVA_1_5 = "1.5.0"; // NOI18N

    private static final String REPLACE_return = "return01234";
    private static final String REPLACE_class = "class01234";

    static final String RETURN_MACRO = "{return}";
    static final String CLASS_MACRO = "{class}";
    
    private String       strExpression;
    private String       language;
    private String       replace_return;
    private String       replace_class;

    /**
     * Creates a new expression by pre-parsing the given String representation of the expression.
     *
     * @param expr textual representation of an expression
     * @param language one of the LANGUAGE_XXX constants
     * @return pre-parsed Java expression
     * @throws ParseException if the expression has wrong syntax
     */
    public static JavaExpression parse (String expr, String language) {
    //throws ParseException {
        String replace_return = REPLACE_return;
        while (expr.indexOf(replace_return) >= 0) {
            replace_return = "return" + new Random().nextLong(); // NOI18N
        }
        String replace_class = REPLACE_class;
        while (expr.indexOf(replace_class) >= 0) {
            replace_class = "class" + new Random().nextLong(); // NOI18N
        }
        String replacedExpr = replaceSpecialVar(expr, RETURN_MACRO, replace_return); // NOI18N
        replacedExpr = replaceSpecialVar(replacedExpr, CLASS_MACRO, replace_class); // NOI18N
        return new JavaExpression(replacedExpr, language, replace_return, replace_class);
    }
    
    private static String replaceSpecialVar(String expr, String var, String replace_var) {
        int i = expr.indexOf(var);
        while (i >= 0) {
            boolean replace;
            if (i > 0) {
                char ch = expr.charAt(i - 1);
                if (Character.isJavaIdentifierStart(ch) ||
                    Character.isJavaIdentifierPart(ch) ||
                    ch == '.') {
                    replace = false;
                } else {
                    replace = true;
                }
            } else {
                replace = true;
            }
            if (replace && i < (expr.length() - var.length())) {
                char ch = expr.charAt(i + var.length());
                if (Character.isJavaIdentifierPart(ch)) {
                    replace = false;
                }
            }
            if (replace) {
                expr = expr.substring(0, i) + replace_var + expr.substring(i + var.length());
                i += replace_var.length();
            } else {
                i += var.length();
            }
            i = expr.indexOf(var, i);
        }
        return expr;
    }
    
    private JavaExpression(String expression, String language,
                       String replace_return, String replace_class) {
        strExpression = expression;
        this.language = language;
        this.replace_return = replace_return;
        this.replace_class = replace_class;
    }

    /**
     * Creates an evaluator engine that can be used to evaluate this expression in a given
     * runtime JVM context.
     *
     * @param context a runtime JVM context
     * @return the evaluator engine
     */
    public TreeEvaluator evaluator(EvaluationContext context,
                                   CompilationInfoHolder ciHolder) {
        return new TreeEvaluator(this, context, ciHolder);
    }

    public String getLanguage() {
        return language;
    }

    public String getExpression() {
        return strExpression;
    }
    
    String returnReplaced() {
        return replace_return;
    }
    
    String classReplaced() {
        return replace_class;
    }
}
