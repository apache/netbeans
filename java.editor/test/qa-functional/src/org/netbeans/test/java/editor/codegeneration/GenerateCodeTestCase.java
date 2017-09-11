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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.editor.codegeneration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.test.java.editor.lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 *
 * @author Jiri Prox
 */
public class GenerateCodeTestCase extends EditorTestCase {

    public GenerateCodeTestCase(String testMethodName) {
        super(testMethodName);
    }



    public static String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");
               
        if (specVersion.startsWith("1.7")) {
            return "jdk17";
        }
        
        if (specVersion.startsWith("1.8")) {
            return "jdk18";
        }

        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
    }

    private boolean isWin() {
        return System.getProperty("os.name").contains("Windows");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openProject("java_editor_test");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void waitAndCompare(String... expected) {
        for (String string : expected) {
            waitMaxMilisForValue(1500, new EditorValueResolver(string), Boolean.TRUE);
            if (!editor.getText().contains(string)) {
                System.out.println("Text pattern:");
                System.out.println(string);
                System.out.println("-------------------");
                System.out.println(editor.getText());
            } else {
                return;
            }            
        }        
        fail("Expected code is not inserted");
    }

    protected void waitAndCompareRegexp(String regexp) {
        Pattern  p = Pattern.compile(regexp, Pattern.DOTALL);
        waitMaxMilisForValue(1500, new EditorValueResolverRegexp(p), Boolean.TRUE);
        if (!p.matcher(editor.getText()).matches()) {
            System.out.println("Regular expresion pattern:");
            System.out.println(regexp);
            System.out.println("-------------------");

            System.out.println(editor.getText());
        }
        assertTrue("Expected code is not inserted", p.matcher(editor.getText()).matches());
    }
    protected EditorOperator editor;
    protected JEditorPaneOperator txtOper;

    protected class EditorValueResolver implements ValueResolver {

        private String text;

        public EditorValueResolver(String text) {
            this.text = text;
        }

        public Object getValue() {
            return editor.getText().contains(text);
        }
    }

    protected class EditorValueResolverRegexp implements ValueResolver {

        private Pattern pattern;

        public EditorValueResolverRegexp(String text) {
        }

        private EditorValueResolverRegexp(Pattern pattern) {
            this.pattern = pattern;
            
        }

        public Object getValue() {
            Matcher matcher = pattern.matcher(editor.getText());
            return matcher.matches();
        }
    }
}
