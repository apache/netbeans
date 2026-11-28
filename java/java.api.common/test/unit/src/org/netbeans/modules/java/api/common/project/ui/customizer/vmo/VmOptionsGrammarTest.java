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
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;

import java.util.*;

/**
 * @author Rastislav Komara
 */
public class VmOptionsGrammarTest extends NbTestCase {

    public VmOptionsGrammarTest(String name) {
        super(name);
    }

    public static final OptionValue.SwitchOnly so = new OptionValue.SwitchOnly(true);
    public static final OptionValue.SimpleString stringOnly = new OptionValue.SimpleString("hello");
    public static final OptionValue.SimpleString memDef = new OptionValue.SimpleString("100M");
    public static final OptionValue.SimpleString classpath = new OptionValue.SimpleString("c:\\hellodolly\\capo-1.3.2_dev.jar:/hello/dolly/common.jar:bla.jar");

    /**
     * A fresh option of each form the customizer can render.
     */
    private static List<JavaVMOption<?>> testOptions() {
        return new ArrayList<>(List.of(
                new SwitchNode("server"),
                new SwitchNode("verbose:gc"),
                new ParametrizedNode("Xmx", ""),
                new ParametrizedNode("cp", " "),
                new ParametrizedNode("splash", ":"),
                new UserPropertyNode("Dfoo", "bar")
        ));
    }

    @Test
    public void testIndividualOption() {
        final List<JavaVMOption<?>> list = testOptions();
        final List<JavaVMOption<?>> errors = new LinkedList<>();
        for (JavaVMOption<?> option : list) {
            try {
                verifyOption(option);
            } catch (Exception e) {
                errors.add(option);
            }
        }
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("The following options are not recognized: ");
            for (JavaVMOption<?> option : errors) {
                sb = option.print(sb).append(' ');
            }
            fail(sb.toString());
        }
    }

    @Test
    public void testRandomOptionsSet() {
        final List<JavaVMOption<?>> list = testOptions();
        final Set<JavaVMOption<?>> selection = new HashSet<>(10);
        Random r = new Random(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int idx = r.nextInt(list.size());
            final JavaVMOption<?> e = list.get(idx);
            if (!selection.contains(e)) {
                selection.add(setOptionValue(e));
                sb = e.print(sb).append(" ");
            }
        }
        Set<JavaVMOption<?>> result = new HashSet<>(VMOptionParser.parse(sb.toString()));
        assertEquals("The result and selection are not same.", result, selection);
    }

    @Test
    public void testRecognitionErrors() {
        final String str = "-client -serv hello:dolly -version:hello";
        final List<JavaVMOption<?>> children = VMOptionParser.parse(str);
        if (children.isEmpty()) fail("The list of recognized options is empty");

        Set<JavaVMOption<?>> template = new HashSet<>();
        template.add(setOptionValue(new SwitchNode("client")));
        template.add(setOptionValue(new UnknownOption("hello:dolly")));
        template.add(setOptionValue(new UnrecognizedOption("serv")));
        template.add(setOptionValue(new ParametrizedNode("version", ":")));

        Iterator<JavaVMOption<?>> it = children.iterator();
        while (it.hasNext()) {
            JavaVMOption<?> child = it.next();
            if (template.contains(child)) {
                it.remove();
            }
        }

        if (!children.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (JavaVMOption<?> child : children) {
                child.print(sb).append(" ");
            }
            fail("Following options are not in template: " + sb + ";\n\t where template is " + template);
        }
    }

    @Test
    public void testEquality() {
        JavaVMOption<?> option1 = setOptionValue(new SwitchNode("version"));
        JavaVMOption<?> option2 = setOptionValue(new ParametrizedNode("version", ":"));
        if (option1.equals(option2)) {
            fail("The equals function for JavaVMOption class is incorrect.");
        }
    }

    @Test
    public void testSystemProperty() {
        List<JavaVMOption<?>> options = VMOptionParser.parse("-Dfoo=bar");
        assertEquals(1, options.size());
        UserPropertyNode upn = (UserPropertyNode) options.getFirst();
        assertEquals("Dfoo", upn.getName());
        assertEquals("bar", ((OptionValue.StringPair) upn.getValue()).getValue().getValue());
        assertEquals("-Dfoo=bar", upn.print(new StringBuilder()).toString().strip());
    }

    @Test
    public void testQuotedValue() {
        assertPropertyValue("-Dfoo='bar'", "bar");
        assertPropertyValue("-Dfoo=\"bar\"", "bar");
    }

    @Test
    public void testMemoryOption() {
        List<JavaVMOption<?>> options = VMOptionParser.parse("-Xmx512m");
        assertEquals(1, options.size());
        ParametrizedNode pn = (ParametrizedNode) options.getFirst();
        assertEquals("Xmx", pn.getName());
        assertEquals("512m", pn.getValue().getValue());
    }

    @Test
    public void testClasspathOption() {
        List<JavaVMOption<?>> options = VMOptionParser.parse("-cp a.jar:b.jar");
        assertEquals(1, options.size());
        ParametrizedNode pn = (ParametrizedNode) options.getFirst();
        assertEquals("cp", pn.getName());
        assertEquals("a.jar:b.jar", pn.getValue().getValue());
        assert !pn.isValid();
    }

    @Test
    public void testSwitchVariants() {
        final List<JavaVMOption<?>> options = VMOptionParser.parse("-verbose:gc -Xshare:on -Xcheck:jni");
        assertEquals(3, options.size());
        for (JavaVMOption<?> option : options) {
            assert option instanceof SwitchNode : option;
        }
        assertEquals("verbose:gc", options.get(0).getName());
        assertEquals("Xshare:on", options.get(1).getName());
        assertEquals("Xcheck:jni", options.get(2).getName());
    }

    private JavaVMOption<?> setOptionValue(JavaVMOption<?> option) {
        final OptionValue<?> value = option.getValue();
        if (value != null && value.isPresent()) {
            return option;
        }
        /*-version:hello  -splash:hello  -javaagent:hello  -agentlib:hello  -agentpath:hello */
        if (option instanceof SwitchNode node) {
            node.setValue(so);
        } else if (option instanceof UserPropertyNode node) {
            node.setValue(new OptionValue.StringPair("Dsimple.human.description", "value"));
        } else if (option instanceof ParametrizedNode node) {
            final String name = node.getName();
            if ("cp".equalsIgnoreCase(name) || "classpath".equalsIgnoreCase(name)) {
                node.setValue(classpath);
            }
            if ("Xmx".equals(name) || "Xms".equals(name) || "Xss".equals(name)) {
                node.setValue(memDef);
            } else {
                node.setValue(stringOnly);
            }
        }
        return option;
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void verifyOption(JavaVMOption<?> option) throws Exception {
        option = setOptionValue(option);
        StringBuilder sb = new StringBuilder();
        sb = option.print(sb);
        final List<JavaVMOption<?>> parsed = VMOptionParser.parse(sb.toString());
        assertEquals(1, parsed.size());
        if (!option.equals(parsed.getFirst())) {
            throw new Exception("The parsed and origin is not same");
        }
    }

    private void assertPropertyValue(String input, String expected) {
        final List<JavaVMOption<?>> options = VMOptionParser.parse(input);
        assertEquals(1, options.size());
        assertEquals(expected, ((OptionValue.StringPair) ((UserPropertyNode) options.getFirst()).getValue()).getValue().getValue());
    }

}
