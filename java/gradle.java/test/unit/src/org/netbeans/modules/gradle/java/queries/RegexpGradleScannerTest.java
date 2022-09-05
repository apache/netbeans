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
package org.netbeans.modules.gradle.java.queries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class RegexpGradleScannerTest extends NbTestCase {

    public RegexpGradleScannerTest(String name) {
        super(name);
    }
    
    
    /**
     * Checks various complex constructs in the prologue that should be skipped before dependencies block is identified.
     * - curly braces (nested)
     * - parenthesis after an identifier
     * - valid dependencies block in a block comment
     * - strings in quotes and doublequotes
     * @throws Exception 
     */
    public void testComplexPrologue() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/complexPrologue.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner();
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation"
        ));
        List<DependencyText> deps = scanner.parseDependencyList(f.asText());
        assertEquals(2, deps.size());
        assertTrue(deps.get(0).toString().contains("org.hibernate:hibernate:3.0.5"));
    }
    
    public void testSkipExecutableCodeInDependencies() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/executableCodeInDependencies.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner();
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation"
        ));
        List<DependencyText> deps = scanner.parseDependencyList(f.asText());
        assertEquals(2, deps.size());
    }
    
    String filteredText;
    
    public void testScanSimpleScript() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/simple.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner();
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation"
        ));
        filteredText = filterAndStorePositions(f.asText());

        List<DependencyText> deps = scanner.parseDependencyList(filteredText);
        assertEquals(12, deps.size());
        checkDependencyBoundaries(deps);
    }
    
    public void testMicronautStarter() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/starter.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner();
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation", "annotationProcessor"
        ));
        filteredText = filterAndStorePositions(f.asText());
        List<DependencyText> deps = scanner.parseDependencyList(filteredText);
        assertEquals(7, deps.size());
    }
    
    private Map<String, Integer> startPosition = new HashMap<>();
    private Map<String, Integer> endPosition = new HashMap<>();
    
    private void checkDependencyBoundaries(List<DependencyText> deps) {
        int pos = 0;
        for (DependencyText d : deps) {
            String key = (char)(pos + 'A') + "";
            pos++;
            
            Integer s = startPosition.get(key);
            Integer e = endPosition.get(key);
            if (s == null || e == null) {
                fail("No positions for " + d);
            }
            
            assertEquals("Invalid start pos for " + d, (int)s, d.startPos);
            assertEquals("Invalid end pos for " + d, (int)e, d.endPos);
        }
    }
    
    private String filterAndStorePositions(String code) {
        Matcher m = Pattern.compile("@@([A-Z])@@").matcher(code);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        
        while (m.find()) {
            int s = m.start();
            sb.append(code.substring(last, s));
            String id = m.group(1);
            Integer sp = startPosition.get(id);
            if (sp == null) {
                startPosition.put(id, sb.length());
            } else {
                endPosition.put(id, sb.length());
            }
            last = m.end();
        }
        sb.append(code.substring(last));
        return sb.toString();
    }

    /*
    public void testTestScan() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/test.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner();
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation"
        ));
        String text = filterAndStorePositions(f.asText());
        List<TextDependencyScanner.Dependency> deps = scanner.parseDependencyList(text);
        assertEquals(6, deps.size());
    }
    */
}
