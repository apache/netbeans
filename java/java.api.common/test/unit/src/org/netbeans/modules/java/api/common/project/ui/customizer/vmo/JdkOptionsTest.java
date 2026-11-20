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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 * Tests the parsing of the {@code java --help} / {@code java -X} output.
 */
public class JdkOptionsTest extends NbTestCase {

    public JdkOptionsTest(String name) {
        super(name);
    }

    private static final String HELP_OUTPUT =
            """
            Usage: java [options] <mainclass> [args...]
                       (to execute a class)
            
             where options include:
            
                -cp <class search path of directories and zip/jar files>
                -classpath <class search path of directories and zip/jar files>
                --class-path <class search path of directories and zip/jar files>
                              A : separated list of directories, JAR archives,
                              and ZIP archives to search for class files.
                -D<name>=<value>
                              set a system property
                -ea[:<packagename>...|:<classname>]
                              enable assertions with specified granularity
                -version      print product version to the error stream and exit
                -esa | -enablesystemassertions
                              enable system assertions
                -splash:<imagepath>
                              show splash screen with specified image
            """;

    private static final String X_OUTPUT =
            """
                -Xbatch           disable background compilation
                -Xbootclasspath/a:<directories and zip/jar files separated by :>
                                  append to end of bootstrap class path
                -Xfuture          enable strictest checks, anticipating future default.
                                  This option is deprecated and may be removed in a
                                  future release.
                -Xmn<size>        sets the initial and maximum size (in bytes) of the heap
                                  for the young generation (nursery)
                -agentlib:<libname>[=<options>]
                                  load native agent library <libname>, e.g. -agentlib:jdwp
            """;

    @Test
    public void testParseHelpOptions() {
        Map<String, JdkOptions.OptionDefinition> byName = toMap(JdkOptions.parseHelp(HELP_OUTPUT));
        assertEquals(8, byName.size()); //NOI18N

        assertEquals("<class search path of directories and zip/jar files>", byName.get("cp").placeholder());
        assertEquals("A : separated list of directories, JAR archives, and ZIP archives to search for class files.", byName.get("class-path").description());
        assertEquals("<name>=<value>", byName.get("D").placeholder());
        assertNull(byName.get("ea").placeholder());
        assertEquals("enable assertions with specified granularity", byName.get("ea").description());
        assertEquals("print product version to the error stream and exit", byName.get("version").description());
        assertEquals(":", byName.get("splash").delimiter());
        assertEquals("<imagepath>", byName.get("splash").placeholder());
        assertEquals("-enablesystemassertions enable system assertions", byName.get("esa").description());
    }

    @Test
    public void testParseX() {
        Map<String, JdkOptions.OptionDefinition> byName = toMap(JdkOptions.parseHelp(X_OUTPUT));
        assertEquals(5, byName.size()); //NOI18N

        assertEquals("disable background compilation", byName.get("Xbatch").description());
        assertEquals("Xbootclasspath/a", byName.get("Xbootclasspath/a").name());
        assertEquals(":", byName.get("Xbootclasspath/a").delimiter());
        assertEquals("append to end of bootstrap class path", byName.get("Xbootclasspath/a").description());
        assertTrue(byName.get("Xfuture").description().contains("This option is deprecated"));
        assertEquals("", byName.get("Xmn").delimiter());
        assertEquals("<size>", byName.get("Xmn").placeholder());
        assertEquals("agentlib", byName.get("agentlib").name());
        assertEquals(":", byName.get("agentlib").delimiter());
        assertEquals("<libname>[=<options>]", byName.get("agentlib").placeholder());
    }

    @Test
    public void testParseGarbage() {
        assertTrue(JdkOptions.parseHelp(null).isEmpty());
        assertTrue(JdkOptions.parseHelp("").isEmpty());
        assertTrue(JdkOptions.parseHelp("no options here").isEmpty());
    }

    @Test
    public void testToNodes() {
        List<JdkOptions.OptionDefinition> defs = JdkOptions.parseHelp(HELP_OUTPUT + X_OUTPUT);
        List<JavaVMOption<?>> nodes = JdkOptions.toNodes(defs);
        Map<String, JavaVMOption<?>> byName = nodes.stream()
                .collect(Collectors.toMap(JavaVMOption::getName, Function.identity()));

        // the -D option is offered by the free form row instead
        assertFalse(byName.containsKey("D"));
        assertTrue(nodes.size() == defs.size() - 1); //NOI18N

        assertTrue(byName.get("cp") instanceof ParametrizedNode);
        assertEquals(" ", ((ParametrizedNode) byName.get("cp")).getDelimiter());
        assertNull(byName.get("cp").getValue().getValue());
        assertTrue(byName.get("version") instanceof SwitchNode);
        assertEquals("<size>", ((ParametrizedNode) byName.get("Xmn")).getHint());
        assertNotNull(byName.get("Xbatch").getDescription());
    }

    @Test
    public void testToNodesMerged() {
        // the same option printed by both --help and -X is listed only once
        List<JdkOptions.OptionDefinition> defs = JdkOptions.parseHelp(
                """
                    -Xbatch           disable background compilation
                    -Xbatch
                """
        );
        assertEquals(1, JdkOptions.toNodes(defs).size()); //NOI18N
    }

    private static Map<String, JdkOptions.OptionDefinition> toMap(List<JdkOptions.OptionDefinition> defs) {
        return defs.stream()
                .collect(Collectors.toMap(JdkOptions.OptionDefinition::name, Function.identity()));
    }

}
