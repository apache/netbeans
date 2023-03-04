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
