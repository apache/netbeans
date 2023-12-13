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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.ProjectSpec;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;
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
        TextDependencyScanner scanner = new TextDependencyScanner(true);
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation"
        ));
        List<DependencyText> deps = scanner.parseDependencyList(f.asText());
        assertEquals(2, deps.size());
        assertTrue(deps.get(0).toString().contains("org.hibernate:hibernate:3.0.5"));
    }
    
    public void testSkipExecutableCodeInDependencies() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/executableCodeInDependencies.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner(true);
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation"
        ));
        List<DependencyText> deps = scanner.parseDependencyList(f.asText());
        assertEquals(2, deps.size());
    }
    
    String filteredText;
    
    public void testScanSimpleScript() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/simple.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner(true);
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation"
        ));
        filteredText = filterAndStorePositions(f.asText());

        List<DependencyText> deps = scanner.parseDependencyList(filteredText);
        assertEquals(12, deps.size());
        checkDependencyBoundaries(deps);
        checkDependencyMap(scanner, deps);
    }
    
    public void testMicronautStarter() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/starter.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner(true);
        
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation", "annotationProcessor"
        ));
        filteredText = filterAndStorePositions(f.asText());
        List<DependencyText> deps = scanner.parseDependencyList(filteredText);
        assertEquals(7, deps.size());
        checkDependencyMap(scanner, deps);
    }
    
    private static final List<String> expectedGavs = Arrays.asList(
            "io.micronaut:micronaut-http-validation",
            "io.micronaut:micronaut-http-client",
            "io.micronaut:micronaut-jackson-databind",
            "jakarta.annotation:jakarta.annotation-api",
            "ch.qos.logback:logback-classic",
            "io.micronaut:micronaut-validation:2.5",
            "org.hibernate:hibernate:3.0.5",
            "org.ow2.asm:asm:7.1",
            "org.apache.logging.log4j:log4j-core:2.17.0"
    );
    
    /**
     * Checks that various declaration syntaxes are understood well by the parser.
     */
    public void testVariousSyntaxes() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/variousSyntax.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner(true);
        
        filteredText = filterAndStorePositions(f.asText());
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation", "annotationProcessor"
        ));
        List<DependencyText> deps = scanner.parseDependencyList(filteredText);
        List<String> gavs = deps.stream().map(DependencyText::getContentsOrGav).collect(Collectors.toList());
        assertEquals(expectedGavs, gavs);
        checkDependencyBoundaries(deps);
        checkDependencyMap(scanner, deps);
    }
    
    
    
    /**
     * Checks that the container is properly reported. For single dependencies, the container is null.
     * For dependency blocks like compileOnly {...}, the container is the 'compileOnly" block.
     * @throws Exception 
     */
    public void testDependencyContainers() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/variousSyntax.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner(true);
        
        filteredText = filterAndStorePositions(f.asText());
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation", "annotationProcessor"
        ));
        List<DependencyText> deps = scanner.parseDependencyList(filteredText);
        DependencyText text = deps.stream().filter(d -> "io.micronaut:micronaut-http-validation".equals(d.contents)).findAny().get();
        assertNull(text.container); // no specific container
        
        text = deps.stream().filter(d -> "io.micronaut:micronaut-jackson-databind".equals(d.contents)).findAny().get();
        assertNotNull("Container is found for string lists", text.container);
        assertEquals("implementation", text.container.containerPart.partId);
        assertEquals((int)startPosition.get("P"), text.container.containerPart.startPos);
        assertEquals((int)startPosition.get("Q"), text.container.containerPart.endPos);
        
        text = deps.stream().filter(d -> "ch.qos.logback:logback-classic".equals(d.contents)).findAny().get();
        assertNull("Parser is not fooled by braced customization", text.container); // no specific container

        text = deps.stream().filter(d -> "org.ow2.asm".equals(d.group)).findAny().get();
        assertNotNull("Container is found for map lists", text.container);
        assertEquals("runtimeOnly", text.container.containerPart.partId);
        assertEquals((int)startPosition.get("R"), text.container.containerPart.startPos);
        assertEquals((int)startPosition.get("S"), text.container.containerPart.endPos);
    }
    
    public void testMapLikeDeclaration() throws Exception {
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("dependencies/parse/variousSyntax.gradle");
        TextDependencyScanner scanner = new TextDependencyScanner(true);
        
        filteredText = filterAndStorePositions(f.asText());
        scanner.withConfigurations(Arrays.asList(
            "runtimeOnly", "implementation", "annotationProcessor"
        ));
        List<DependencyText> deps = scanner.parseDependencyList(filteredText);
        assertNotNull(deps);
        checkDependencyBoundaries(deps);
        checkDependencyMap(scanner, deps);
    }
    
    private Map<String, Integer> startPosition = new HashMap<>();
    private Map<String, Integer> endPosition = new HashMap<>();
    
    /**
     * Stub that only serves as an identifier, cannot answer imply/inherit questions.
     */
    private static final class ScopeStub extends Scope {
        public ScopeStub(String name) {
            super(name);
        }

        public boolean includes(Scope s) {
            return false;
        }

        public boolean exports(Scope s) {
            return false;
        }

        public boolean implies(Scope s) {
            return false;
        }
    }
    
    private Scope s(String name) {
        return new ScopeStub(name);
    }
    
    private void checkDependencyMap(TextDependencyScanner scanner, List<DependencyText> deps) {
        List<Dependency> list = new ArrayList<>();
        for (DependencyText t : deps) {
            if (t.keyword == null && t.name != null && t.group != null && t.version != null) {
                ArtifactSpec as = ArtifactSpec.builder(t.group, t.name, t.version, null).build();
                Dependency d = Dependency.create(as, s(t.configuration), Collections.emptyList(), null);
                list.add(d);
            } else if ("project".equals(t.keyword)) {
                ProjectSpec p = ProjectSpec.create(t.contents, null);
                ArtifactSpec as = ArtifactSpec.builder(t.group, t.name, t.version, null).build();
                Dependency d = Dependency.create(p, as, s(t.configuration), Collections.emptyList(), null);
                list.add(d);
            }
        }
        
        DependencyText.Mapping mapping = scanner.mapDependencies(list);
        for (Dependency d : list) {
            DependencyText.Part found = mapping.getText(d, null);
            assertNotNull(found);
        }
    }
    
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
