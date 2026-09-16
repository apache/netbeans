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
package org.netbeans.modules.java.hints.errors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;

/**
 *
 * @author Jan Lahoda
 */
public class SuppressWarningsFixerTest extends HintsTestBase {
    
    public SuppressWarningsFixerTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.doSetUp("org/netbeans/modules/java/hints/resources/layer.xml");
        TestCompilerSettings.commandLine = "-Xlint:deprecation -Xlint:fallthrough -Xlint:unchecked";
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/SuppressWarningsFixerTest/";
    }
    
    public void testSuppressWarnings1() throws Exception {
        performTest("Test", "unchecked", 8, 5);
    }
    
    public void testSuppressWarnings2() throws Exception {
        performTest("Test", "unchecked", 11, 5);
    }
    
    public void testSuppressWarnings3() throws Exception {
        performTest("Test", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings4() throws Exception {
        performTest("Test", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings5() throws Exception {
        performTest("Test", "unchecked", 28, 5);
    }
    
    public void testSuppressWarnings6() throws Exception {
        performTest("Test", "unchecked", 35, 5);
    }
    
    public void testSuppressWarnings7() throws Exception {
        performTest("Test2", "unchecked", 10, 5);
    }
    
    public void testSuppressWarnings8() throws Exception {
        performTest("Test2", "unchecked", 16, 5);
    }
    
    public void testSuppressWarnings9() throws Exception {
        performTest("Test2", "unchecked", 22, 5);
    }
    
    public void testSuppressWarnings10() throws Exception {
        performTestDoNotPerform("Test2", 31, 5);
    }
    
    public void testSuppressWarnings11() throws Exception {
        performTestDoNotPerform("Test2", 38, 5);
    }
    
    public void testSuppressWarnings106794() throws Exception {
        performTestDoNotPerform("Test3", 3, 10);
    }
    
    public void testDiag2LintMap() throws Exception {
        // We could get the suppression keys via Lint.LintCategory but not the diag keys.
        // The diag keys are in a properties file and the suppression key in comments, so we have to get them the hacky way to create a mapping.
        // https://github.com/openjdk/jdk/blob/master/src/jdk.compiler/share/classes/com/sun/tools/javac/resources/compiler.properties
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(SuppressWarningsFixer.class.getResourceAsStream("/com/sun/tools/javac/resources/compiler.properties")))) {
            
            List<String> lines = readAllLines(reader);
            
            Map<String, Set<String>> mapping = new TreeMap<>();
            // Gatherer with windowSliding(3)
            for (int i = 0; i < lines.size(); i++) {
//              # 0: symbol
//              # lint: removal
//              # flags: aggregate, mandatory, default-enabled
//              compiler.warn.has.been.deprecated.for.removal.module=\
//                  module {0} has been deprecated and marked for removal
                String comment = lines.get(i);
                if (comment.startsWith("# lint: ")) {
                    String lintCategory = comment.substring(8).strip();
                    String propertyLine = lines.get(i + 1).startsWith("#") // skip line if still a comment
                            ? lines.get(i + 2)
                            : lines.get(i + 1);
                    String diagKey = propertyLine.substring(0, propertyLine.indexOf("=")).strip();
                    mapping.computeIfAbsent(lintCategory, k -> new TreeSet<>())
                           .add(diagKey);
                }
            }
            
            Set<String> diagKeys = mapping.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
            for (String key : diagKeys) {
                if (!SuppressWarningsFixer.DIAG2LINT.containsKey(key)) {
                    dumpCodeSnippet(mapping);
                    fail(key + " not found");
                }
            }
            if (diagKeys.size() != SuppressWarningsFixer.DIAG2LINT.size()) {
                dumpCodeSnippet(mapping);
                fail("map size does not match");
            }
        }
    }

    private void dumpCodeSnippet(Map<String, Set<String>> diag2lint) {
        StringBuilder snippet = new StringBuilder();
        snippet.append("Please update the keys in ").append(SuppressWarningsFixer.class.getName()).append("\n");
        snippet.append("- - -\n");
        for (Map.Entry<String, Set<String>> entry : diag2lint.entrySet()) {
            for (String diag : entry.getValue()) {
                snippet.append(
                        "entry(\"%s\", \"%s\"),   // NOI18N\n"
                                .formatted(diag, entry.getKey())
                                .indent(12)
                );
            }
        }
        snippet.append("- - -\n");
        System.out.println(snippet);
    }

    // TODO remove after JDK 25
    private static List<String> readAllLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        return lines;
    }
    
}
