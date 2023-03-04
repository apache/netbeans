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

package org.netbeans.modules.java.hints.declarative;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.declarative.Condition.Instanceof;
import org.netbeans.modules.java.hints.declarative.Condition.MethodInvocation;
import org.netbeans.modules.java.hints.declarative.Condition.MethodInvocation.ParameterKind;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.FixTextDescription;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.HintTextDescription;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.Result;
import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class DeclarativeHintsParserTest extends NbTestCase {

    public DeclarativeHintsParserTest(String name) {
        super(name);
    }

    public void testSimpleParse() throws Exception {
        performTest(" 1 + 1 :: $1 instanceof something && $test instanceof somethingelse => 1 + 1;; ",
                    StringHintDescription.create(" 1 + 1 ")
                                         .addCondition(new Instanceof(false, "$1", " something ", new int[2]))
                                         .addCondition(new Instanceof(false, "$test", " somethingelse ", new int[2]))
                                         .addTos(" 1 + 1"));
    }

    public void testParseDisplayName() throws Exception {
        performTest("'test': 1 + 1 :: $1 instanceof something && $test instanceof somethingelse => 1 + 1;; ",
                    StringHintDescription.create(" 1 + 1 ")
                                         .addCondition(new Instanceof(false, "$1", " something ", new int[2]))
                                         .addCondition(new Instanceof(false, "$test", " somethingelse ", new int[2]))
                                         .addTos(" 1 + 1")
                                         .setDisplayName("test"));
    }

    public void testMultiple() throws Exception {
        performTest("'test': 1 + 1 => 1 + 1;; 'test2': 1 + 1 => 1 + 1;;",
                    StringHintDescription.create(" 1 + 1 ")
                                         .addTos(" 1 + 1")
                                         .setDisplayName("test"),
                    StringHintDescription.create(" 1 + 1 ")
                                         .addTos(" 1 + 1")
                                         .setDisplayName("test2"));
    }

    public void testMethodInvocationCondition1() throws Exception {
        Map<String, ParameterKind> m = new LinkedHashMap<>();

        m.put("a", ParameterKind.STRING_LITERAL);
        m.put("$2", ParameterKind.VARIABLE);
        m.put("$1", ParameterKind.VARIABLE);

        performTest("'test': $1 + $2 :: test(\"a\", $2, $1) => 1 + 1;;",
                    StringHintDescription.create("$1 + $2 ")
                                         .addCondition(new MethodInvocation(false, "test", m, null))
                                         .addTos(" 1 + 1")
                                         .setDisplayName("test"));
    }

    public void testMethodInvocationCondition2() throws Exception {
        Map<String, ParameterKind> m = new LinkedHashMap<>();

        m.put("$1", ParameterKind.VARIABLE);
        m.put("javax.lang.model.element.Modifier.VOLATILE", ParameterKind.ENUM_CONSTANT);
        m.put("javax.lang.model.SourceVersion.RELEASE_6", ParameterKind.ENUM_CONSTANT);

        performTest("'test': $1 + $2 :: test($1, Modifier.VOLATILE, SourceVersion.RELEASE_6) => 1 + 1;;",
                    StringHintDescription.create("$1 + $2 ")
                                         .addCondition(new MethodInvocation(false, "test", m, null))
                                         .addTos(" 1 + 1")
                                         .setDisplayName("test"));
    }

    public void testMethodInvocationCondition3() throws Exception {
        Map<String, ParameterKind> m = new LinkedHashMap<>();

        m.put("$1", ParameterKind.VARIABLE);
        m.put("42", ParameterKind.INT_LITERAL);

        performTest("'test': $1 + $2 :: test($1, 42) => 1 + 1;;",
                    StringHintDescription.create("$1 + $2 ")
                                         .addCondition(new MethodInvocation(false, "test", m, null))
                                         .addTos(" 1 + 1")
                                         .setDisplayName("test"));
    }

    public void testNegation() throws Exception {
        Map<String, ParameterKind> m = new LinkedHashMap<>();

        m.put("$1", ParameterKind.VARIABLE);
        m.put("javax.lang.model.element.Modifier.VOLATILE", ParameterKind.ENUM_CONSTANT);
        m.put("javax.lang.model.SourceVersion.RELEASE_6", ParameterKind.ENUM_CONSTANT);

        performTest("'test': $1 + $2 :: !test($1, Modifier.VOLATILE, SourceVersion.RELEASE_6) => 1 + 1;;",
                    StringHintDescription.create("$1 + $2 ")
                                         .addCondition(new MethodInvocation(true, "test", m, null))
                                         .addTos(" 1 + 1")
                                         .setDisplayName("test"));
    }

    public void testComments1() throws Exception {
        performTest("/**/'test': /**/1 /**/+ 1//\n =>/**/ 1 + 1/**/;; //\n'test2': /**/1 + 1 =>//\n 1/**/ + 1;;",
                    StringHintDescription.create("1 /**/+ 1//\n ")
                                         .addTos(" 1 + 1/**/")
                                         .setDisplayName("test"),
                    StringHintDescription.create("1 + 1 ")
                                         .addTos(" 1/**/ + 1")
                                         .setDisplayName("test2"));
    }

    public void testParserSanity1() throws Exception {
        String code = "'Use of assert'://\n" +
                      "   assert /**/ $1 : $2; :: //\n $1 instanceof boolean && $2 instanceof java.lang.Object\n" +
                      "=> if (!$1) throw new /**/ IllegalStateException($2);\n" +
                      ";;//\n";
        performParserSanityTest(code);
    }

    public void testJavaBlocks() throws Exception {
        performTest("<?import java.util.List;?> /**/'test': List $l; :: test($_)\n => List $l; ;; <?private boolean test(Variable v) {return false;}?>",
                    "import java.util.List;",
                    Arrays.asList(StringHintDescription.create(" List $l; ")
                                                       .addTos(" List $l; ")
                                                       .setDisplayName("test")
                                                       .addCondition(new MethodInvocation(false, "test", Collections.singletonMap("$_", ParameterKind.VARIABLE), null))),
                    Arrays.asList("private boolean test(Variable v) {return false;}"));
    }

    public void testConditionOnFix() throws Exception {
        Map<String, ParameterKind> m = new LinkedHashMap<>();

        m.put("a", ParameterKind.STRING_LITERAL);
        m.put("$2", ParameterKind.VARIABLE);
        m.put("$1", ParameterKind.VARIABLE);

        performTest("'test': $1 + $2 => 1 + 1 :: test(\"a\", $2, $1);;",
                    StringHintDescription.create("$1 + $2 ")
                                         .addTos(new StringFixDescription(" 1 + 1 ")
                                                 .addCondition(new MethodInvocation(false, "test", m, null)))
                                         .setDisplayName("test"));
    }

    public void testParseOptions() {
        Map<String, String> result = new HashMap<>();

        DeclarativeHintsParser.parseOptions("key1=value1,key2=value2,key3=value3", result);

        Map<String, String> golden = new HashMap<>();

        golden.put("key1", "value1");
        golden.put("key2", "value2");
        golden.put("key3", "value3");

        assertEquals(golden, result);

        result = new HashMap<>();

        DeclarativeHintsParser.parseOptions("key1=\"value1a,value1b\",key2=\"value2a,value2b\",key3=\"value3a,value3b\"", result);

        golden = new HashMap<>();

        golden.put("key1", "value1a,value1b");
        golden.put("key2", "value2a,value2b");
        golden.put("key3", "value3a,value3b");

        assertEquals(golden, result);
    }

    public void testParseOptionsInContext() throws Exception {
        Map<String, ParameterKind> m = new LinkedHashMap<>();

        m.put("a", ParameterKind.STRING_LITERAL);
        m.put("$2", ParameterKind.VARIABLE);
        m.put("$1", ParameterKind.VARIABLE);

        performTest("<!key=value>'test': $1 + $2 <!key1=value1>=> 1 + 1 <!key2=value2>:: test(\"a\", $2, $1);;",
                    Collections.singletonMap("key", "value"),
                    null,
                    Collections.singletonList(
                        StringHintDescription.create("$1 + $2 ")
                                             .addTos(new StringFixDescription(" 1 + 1 ")
                                                     .addCondition(new MethodInvocation(false, "test", m, null))
                                                     .addOption("key2", "value2"))
                                             .addOption("key1", "value1")
                                             .setDisplayName("test")
                    ),
                    Collections.<String>emptyList());
    }

    public void testOptionsSanity() throws Exception {
        String code = "<!key=value>'test': $1 + $2 <!key1=value1>=> 1 + 1 <!key2=value2>:: test(\"a\", $2, $1);;";

        performParserSanityTest(code);
    }
    
    public void testCustomConditionSanity() throws Exception {
        String code = "Thread.sleep($time) :: condition($time) ;;\n<? boolean condition(Variable var) { return false; } ?>\n";

        performParserSanityTest(code);
    }
    
    public void test218739() throws Exception {
        String code = "<!description=\"ImageIcon\">\nnew javax.swing.ImageIcon($image) ::\norg.openide.util.ImageUtilities.image2icon(@image)\n;;";

        performSimpleParserSanityTest(code);
    }

    public void testError1() throws Exception {
        performErrorGatheringTest("$a + $b :: unknown($a, $b);;",
                                  "0:10-0:26:error:Cannot resolve method");
    }

    public void testError2() throws Exception {
        performErrorGatheringTest("$a + $b :: sourceVersionGE(Foo.BAR);;",
                                  "0:27-0:34:error:Cannot resolve enum constant",
                                  "0:10-0:35:error:Cannot resolve method");
    }

    public void testVarArgs1() throws Exception {
        performErrorGatheringTest("$a + $b :: test($a, \"a\", \"b\");;");
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        FileObject dir = SourceUtilsTestUtil.makeScratchDir(this);
        
        System.setProperty("netbeans.user", FileUtil.toFile(dir).getAbsolutePath());
        
        DeclarativeHintsParser.auxConditionClasses = new Class[] {TestConditionClass.class};
    }
    
    private void performTest(String code, StringHintDescription... golden) throws Exception {
        performTest(code, Collections.<String, String>emptyMap(), null, Arrays.asList(golden), Collections.<String>emptyList());
    }

    private void performTest(String code, String goldenImportsBlock, Collection<StringHintDescription> goldenHints, Collection<String> goldenBlocks) throws Exception {
        performTest(code, Collections.<String, String>emptyMap(), goldenImportsBlock, goldenHints, goldenBlocks);
    }

    private void performTest(String code, Map<String, String> goldenGlobalOptions, String goldenImportsBlock, Collection<StringHintDescription> goldenHints, Collection<String> goldenBlocks) throws Exception {
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        FileObject file = FileUtil.createData(new File(getWorkDir(), "Test.java"));
        TestUtilities.copyStringToFile(file, code);
        Result parsed = new DeclarativeHintsParser().parse(file, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<StringHintDescription> real = new LinkedList<>();

        for (HintTextDescription hint : parsed.hints) {
            real.add(StringHintDescription.create(code, hint));
        }

        assertEquals(goldenGlobalOptions, parsed.options);
        
        if (goldenImportsBlock != null) {
            assertNotNull(parsed.importsBlock);
            assertEquals(goldenImportsBlock, code.substring(parsed.importsBlock[0], parsed.importsBlock[1]));
        } else {
            assertNull(parsed.importsBlock);
        }

        assertEquals(goldenHints, real);
        if (goldenBlocks != null) {
            assertNotNull(parsed.blocks);

            List<String> realBlocks = new LinkedList<>();
            
            for (int[] span : parsed.blocks) {
                realBlocks.add(code.substring(span[0], span[1]));
            }
        } else {
            assertNull(parsed.blocks);
        }
    }

    private void performErrorGatheringTest(String code, String... errors) throws Exception {
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        FileObject file = FileUtil.createData(new File(getWorkDir(), "Test.java"));
        TestUtilities.copyStringToFile(file, code);
        Result parsed = new DeclarativeHintsParser().parse(file, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<String> actualError = new LinkedList<>();

        for (ErrorDescription ed : parsed.errors) {
            actualError.add(ed.toString());
        }

        assertEquals(Arrays.asList(errors), actualError);
    }

    private void performParserSanityTest(String code) throws Exception {
        FileObject file = FileUtil.createData(new File(getWorkDir(), "Test.java"));
        TestUtilities.copyStringToFile(file, code);

        for (int cntr = 0; cntr < code.length(); cntr++) {
            String currentpath = code.substring(0, cntr);
            TokenHierarchy<?> h = TokenHierarchy.create(currentpath, DeclarativeHintTokenId.language());

            new DeclarativeHintsParser().parse(file, currentpath, h.tokenSequence(DeclarativeHintTokenId.language()));
        }
    }
    
    private void performSimpleParserSanityTest(String code) throws Exception {
        FileObject file = FileUtil.createData(new File(getWorkDir(), "Test.java"));
        TestUtilities.copyStringToFile(file, code);
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());

        new DeclarativeHintsParser().parse(file, code, h.tokenSequence(DeclarativeHintTokenId.language()));
    }

    private static final class StringHintDescription {
        private String displayName;
        private final String text;
        private final List<String> conditions;
        private final List<StringFixDescription> to;
        private final Map<String, String> options;

        private StringHintDescription(String text) {
            this.text = text;
            this.conditions = new LinkedList<>();
            this.to = new LinkedList<>();
            this.options = new HashMap<>();
        }

        public static StringHintDescription create(String text) {
            return new StringHintDescription(text);
        }

        public static StringHintDescription create(String code, HintTextDescription desc) {
            StringHintDescription r = StringHintDescription.create(code.substring(desc.textStart, desc.textEnd));

            for (Condition c : desc.conditions) {
                r = r.addCondition(c);
            }

            for (FixTextDescription fix : desc.fixes) {
                int[] range = fix.fixSpan;
                final StringFixDescription sfd = new StringFixDescription(code.substring(range[0], range[1]));

                for (Condition c : fix.conditions) {
                    sfd.addCondition(c);
                }

                sfd.options.putAll(fix.options);
                r = r.addTos(sfd);
            }

            r.options.putAll(desc.options);//TODO: not nice

            return r.setDisplayName(desc.displayName);
        }

        public StringHintDescription addCondition(Condition c) {
            conditions.add(c.toString());
            return this;
        }

        public StringHintDescription addTos(String... to) {
            for (String t : to) {
                this.to.add(new StringFixDescription(t));
            }
            return this;
        }

        public StringHintDescription addTos(StringFixDescription... to) {
            this.to.addAll(Arrays.asList(to));
            return this;
        }

        public StringHintDescription setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public StringHintDescription addOption(String key, String value) {
            this.options.put(key, value);
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StringHintDescription other = (StringHintDescription) obj;
            if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName)) {
                return false;
            }
            if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
                return false;
            }
            if (this.conditions != other.conditions && (this.conditions == null || !this.conditions.equals(other.conditions))) {
                return false;
            }
            if (this.to != other.to && (this.to == null || !this.to.equals(other.to))) {
                return false;
            }
            if (this.options != other.options && (this.options == null || !this.options.equals(other.options))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
            hash = 89 * hash + (this.text != null ? this.text.hashCode() : 0);
            hash = 89 * hash + (this.conditions != null ? this.conditions.hashCode() : 0);
            hash = 89 * hash + (this.to != null ? this.to.hashCode() : 0);
            hash = 89 * hash + (this.options != null ? this.options.hashCode() : 0);
            return hash;
        }


        @Override
        public String toString() {
            return "<" + String.valueOf(displayName) + ":" + text + ":" + conditions + ":" + to + ":" + options + ">";
        }

    }

    private static final class StringFixDescription {
        private final String to;
        private final List<String> conditions = new LinkedList<>();
        private final Map<String, String> options = new HashMap<>();

        public StringFixDescription(String to) {
            this.to = to;
        }

        public StringFixDescription addCondition(Condition c) {
            conditions.add(c.toString());
            return this;
        }

        public StringFixDescription addOption(String key, String value) {
            options.put(key, value);
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StringFixDescription other = (StringFixDescription) obj;
            if ((this.to == null) ? (other.to != null) : !this.to.equals(other.to)) {
                return false;
            }
            if (this.conditions != other.conditions && (this.conditions == null || !this.conditions.equals(other.conditions))) {
                return false;
            }
            if (this.options != other.options && (this.options == null || !this.options.equals(other.options))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.to != null ? this.to.hashCode() : 0);
            hash = 53 * hash + (this.conditions != null ? this.conditions.hashCode() : 0);
            hash = 53 * hash + (this.options != null ? this.options.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "<" + to + ":" + conditions + ":" + options + ">";
        }
    }

    public static final class TestConditionClass {
        public boolean test(String s, Variable v1, Variable v2) { return false; }
        public boolean test(Variable var, int i) { return false; }
        public boolean test(Variable var, Modifier mod, SourceVersion sv) { return false; }
        public boolean test(Variable var, String... strings) { return false; }
   }

}