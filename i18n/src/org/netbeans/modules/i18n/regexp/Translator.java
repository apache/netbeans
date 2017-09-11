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


package org.netbeans.modules.i18n.regexp;

import java.util.Map;

/**
 * Translator of Apache's Regexp regular expressions to JDK's regular
 * expressions.
 *
 * @author  Marian Petras
 */
public final class Translator {

    /**
     * Translates the given Apache Regexp regular expression
     * into a JDK's regular expression.
     *
     * @param  regexp  regular expression according to Apache's Regexp library
     *                 syntax rules
     * @return  regular expression according to syntax rules of JDK's class
     *          {@link java.util.regex.Pattern Pattern}.
     * @exception  java.lang.IllegalArgumentException
     *             if the argument was <code>null</code>
     * @exception  ParseException
     *             if the given expression contained a syntax error
     */
    public static String translateRegexp(String regexp)
            throws IllegalArgumentException, ParseException {
        TreeNodeRoot tree = Parser.parse(regexp);
        return Generator.generateRegexp(tree);
    }

    /**
     * Translates the given Apache Regexp regular expression
     * into a JDK's regular expression.
     *
     * @param  regexp  regular expression according to Apache's Regexp library
     *                 syntax rules
     * @param  tokenReplacements  maps token names to strings to be put in place
     *                            of them, or <code>null</code> to ignore tokens
     *                            (leave them unchanged)
     * @return  regular expression according to syntax rules of JDK's class
     *          {@link java.util.regex.Pattern Pattern}.
     * @exception  java.lang.IllegalArgumentException
     *             if the regular expression is <code>null</code>
     * @exception  java.lang.ClassCastException
     *             if not all keys in the map were strings
     * @exception  ParseException
     *             if the given expression contained a syntax error
     */
    public static String translateRegexp(String regexp,
                                         Map<String,String> tokenReplacements)
            throws IllegalArgumentException, ParseException {

        if ((tokenReplacements == null) || (tokenReplacements.isEmpty())) {
            return translateRegexp(regexp);
        }

        String[] tokenNames = new String[tokenReplacements.size()];
        try {
            tokenReplacements.keySet().toArray(tokenNames);
        } catch (ArrayStoreException ex) {
            throw new ClassCastException();
        }
        TreeNodeRoot tree = Parser.parse(regexp, tokenNames);
        return Generator.generateRegexp(tree, tokenReplacements);
    }

}
