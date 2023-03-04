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
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.BaseTree;
import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.gen.CommandLineLexer;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.gen.CommandLineParser;

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
    public static final OptionValue.StringPair sp = new OptionValue.StringPair("simple.human.description", "\"Hello Dolly\"");


    @Test
    public void testIndividualOption() {
        final List<JavaVMOption<?>> list = CommandLineParser.getAllOptions();
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
        final List<JavaVMOption<?>> list = CommandLineParser.getAllOptions();
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
        final CommandLineLexer lineLexer = new CommandLineLexer(new ANTLRStringStream(sb.toString()));
        CommandLineParser parser = new CommandLineParser(new CommonTokenStream(lineLexer));
        parser.setTreeAdaptor(new VMOptionTreeAdaptor());
        CommandLineParser.vmOptions_return options_return;
        try {
            Set<JavaVMOption<?>> result = new HashSet<>();
            options_return = parser.vmOptions();
            CommonTree root = (CommonTree) options_return.getTree();
            if (root instanceof JavaVMOption<?>) {
                result.add((JavaVMOption<?>) root);
            } else if (root != null) {
                result.addAll((List<JavaVMOption<?>>) (List<?>) root.getChildren());
            }
            assertEquals("The result and selection are not same.", result, selection);
        } catch (RecognitionException e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void testRecognitionErrors() throws RecognitionException {
        final String str = "-client -serv hello:dolly -version:hello";
        final CommandLineLexer lineLexer = new CommandLineLexer(new ANTLRStringStream(str));
        final CommandLineParser parser = new CommandLineParser(new CommonTokenStream(lineLexer));
        parser.setTreeAdaptor(new VMOptionTreeAdaptor());
        final CommandLineParser.vmOptions_return options_return = parser.vmOptions();
        final BaseTree tree = (BaseTree) options_return.getTree();
        if (tree == null) fail("The options tree returned from parser is null.");
        assert tree != null;
        final List<JavaVMOption<?>> children = (List<JavaVMOption<?>>) tree.getChildren();
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
            for (Object child : children) {
                ((JavaVMOption<?>)child).print(sb).append(" ");
            }
            fail("Following options are not in template: " + sb.toString() + ";\n\t where template is " + template);
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

    @SuppressWarnings("AssignmentToMethodParameter")
    private void verifyOption(JavaVMOption<?> option) throws Exception {
        option = setOptionValue(option);
        StringBuilder sb = new StringBuilder();
        sb = option.print(sb);
        final CommandLineLexer lineLexer = new CommandLineLexer(new ANTLRStringStream(sb.toString()));
        CommandLineParser parser = new CommandLineParser(new CommonTokenStream(lineLexer));
        parser.setTreeAdaptor(new VMOptionTreeAdaptor());
        final CommandLineParser.vmOptions_return options_return = parser.vmOptions();
        final JavaVMOption<?> parsedOption = extractOption(options_return);
        if (!option.equals(parsedOption)) {
            throw new Exception("The parsed and origin is not same");
        }
    }

    private JavaVMOption<?> extractOption(CommandLineParser.vmOptions_return options_return) {
        CommonTree root = (CommonTree) options_return.getTree();
        if (root instanceof JavaVMOption<?>) {
            return (JavaVMOption<?>) root;
        } else if (root != null) {
            return (JavaVMOption<?>) root.getChildren().get(0);
        }
        return null;
    }

    private JavaVMOption<?> setOptionValue(JavaVMOption<?> option) {
        /*-version:hello  -splash:hello  -javaagent:hello  -agentlib:hello  -agentpath:hello */
        if (option instanceof SwitchNode) {
            SwitchNode switchNode = (SwitchNode) option;
            switchNode.setValue(so);
        } else if (option instanceof UserPropertyNode) {
            UserPropertyNode node = (UserPropertyNode) option;
            node.setValue(sp);
        } else if (option instanceof ParametrizedNode) {
            ParametrizedNode node = (ParametrizedNode) option;
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
}
