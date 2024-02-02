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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.SourceLocation;
import org.openide.util.BaseUtilities;

/**
 *
 * @author sdedic
 */
public class TextDependencyScanner {
    private Set<String> configurationNames = new HashSet<>();
    
    private final Map<GradleDependency, String> origins = new HashMap<>();
    private final Map<GradleDependency, Map<String, SourceLocation>> locations = new HashMap<>();
    private final boolean matchScopes;
    
    private List<DependencyText> dependencies = new ArrayList<>();
    
    public void addDependencyOrigin(GradleDependency dep, String originConfig) {
        origins.put(dep, originConfig);
    }
    
    private int lastPos;
    private int pos;
    private String contents;
    
    public List<DependencyText> getDependencies() {
        return dependencies;
    }
    
    private enum Token {
        NONE,
        LINECOMMENT,
        BLOCKCOMMENT,
        QUOTED,
        IDENTIFIER,
        SYMBOL,
        OTHER
    }
    
    /**
     * The current dependency being built / added to.
     */
    private DependencyText dep;
    
    /**
     * Start of the token scanned by {@link #nextToken}
     */
    private int tokenStart;
    
    /**
     * Token text scanned by {@link #nextToken}. For quoted Strings, the 
     * value is unquoted and unescaped.
     */
    private String tokenText;
    
    /**
     * Token type of the last token read by {@link #nextToken}.
     */
    private Token tokenType;
    
    /**
     * Position of the last newline encountered in the whitespaces preceding
     * the current position. Will be reset when a non-whitespace character is
     * read.
     */
    int newLinePos;

    public TextDependencyScanner(boolean matchScopes) {
        this.matchScopes = matchScopes;
    }
    
    /**
     * End of the last item in the group
     */
    private int groupItemsEnd;

    private void backChar() {
        pos--;
        lastPos--;
    }
    
    private int nextChar() {
        if (pos == contents.length()) {
            pos++;
            return -1;
        }
        if (pos < contents.length()) {
            lastPos = pos;
            char c = contents.charAt(pos++);
            return c;
        } else {
            throw new EndInputException();
        }
    }
    
    
    private String readQuotedString(int q) {
        int s = lastPos;
        int c;

        do {
            c = nextChar();
            if (c == '\\') {
                nextChar();
            } 
        } while (c != q);
        newLinePos = -1;
        String r = contents.substring(s, lastPos + 1);
        String[] args = BaseUtilities.parseParameters(r);
        return args.length == 1 ? args[0] : r;
    }
    
    private int groupStartPos;
    private int groupItemCount;

    
    /*
        dependencies {
            antContrib files('ant/antcontrib.jar')
            runtimeOnly group: 'org.springframework', name: 'spring-core', version: '2.5'
            runtimeOnly 'org.springframework:spring-core:2.5',
                    'org.springframework:spring-aop:2.5'
            runtimeOnly(
                [group: 'org.springframework', name: 'spring-core', version: '2.5'],
                [group: 'org.springframework', name: 'spring-aop', version: '2.5']
            )
            runtimeOnly('org.hibernate:hibernate:3.0.5') {
                transitive = true
            }
            runtimeOnly group: 'org.hibernate', name: 'hibernate', version: '3.0.5', transitive: true
            runtimeOnly(group: 'org.hibernate', name: 'hibernate', version: '3.0.5') {
                transitive = true
            }
            implementation project(':utils')
            implementation projects.utils
            implementation gradleApi()
            implementation('org.ow2.asm:asm:7.1') {
                because 'we require a JDK 9 compatible bytecode generator'
            }
        }    
    */
    
    /**
     * Finishes the current dependency, if any; adds the dependency to the result list and
     * clears the current dependency.
     */
    private void finishDependency() {
        if (dep == null) {
            return;
        }
        groupItemCount++;
        dependencies.add(dep);
        dep = null;
    }
    private int nextToken() {
        skipWhitespace();
        int c = nextChar();
        if (c == '"' || c == '\'') {
            tokenText = readQuotedString(c);
            tokenType = Token.QUOTED;
            return c;
        }
        int s = lastPos;
        if (Character.isJavaIdentifierStart(c)) {
            int c2;
            do {
                c2 = nextChar();
            } while (Character.isJavaIdentifierPart(c2));
            tokenText = contents.substring(s, lastPos);
            tokenType = Token.IDENTIFIER;
            tokenStart = s;
            backChar();
        } else {
            tokenType = Token.OTHER;
        }
        return c;
    }
    
    private void skipNestedBlock(int brace) {
        int c;
        
        while ((c = nextToken()) != -1) {
            if (tokenType != Token.OTHER) {
                continue;
            }
            switch (c) {
                case '{':
                    skipNestedBlock('}');
                    break;
                case '(':
                    skipNestedBlock(')');
                    break;
                case '[':
                    skipNestedBlock(']');
                    break;
                default:
                    if (c == brace) {
                        newLinePos = -1;
                        return;
                    }
                    break;
            }
        }
    }
    
    boolean wasEndOfLine() {
        return newLinePos != -1;
    }
    
    /**
     * Skips whitespace AND comments. Returns the first non-whitespace character,
     * but does not advance the position, so that character will be returned from {@link #nextChar}.
     */
    private int skipWhitespace() {
        Token skipMode = Token.NONE;
        
        newLinePos = -1;
        while (pos < contents.length()) {
            int c = contents.charAt(pos);
            switch (skipMode) {
                case LINECOMMENT:
                    if (c == '\n') {
                        newLinePos = -1;
                        skipMode = Token.NONE;
                        break;
                    } else {
                        pos++;
                        continue;
                    }
                case BLOCKCOMMENT:
                    if (c == '*') {
                        if (pos + 1 < contents.length()) {
                            if (contents.charAt(pos + 1) == '/') {
                                skipMode = Token.NONE;
                                newLinePos = -1;
                            }
                            pos++;
                        }
                    }
                    pos++;
                    continue;
            }

            if (c == '/') {
                if (pos + 1 < contents.length()) {
                    c = contents.charAt(pos + 1);
                    if (c == '/') {
                        // skip until the newline
                        skipMode = Token.LINECOMMENT;
                        newLinePos = -1;
                        pos += 2;
                        continue;
                    } else if (c == '*') {
                        skipMode = Token.BLOCKCOMMENT;
                        newLinePos = -1;
                        pos += 2;
                        continue;
                    }
                }
            }
            if (!Character.isWhitespace(c)) {
                return c;
            }
            if (c == '\n') {
                newLinePos = pos;
            }
            pos++;
        }
        return -1;
    }
    
    private void scanDepdendencyContainer(String container) {
        int c;
        int braceLevel = 0;
        int kPos = -1;
        int startKpos = -1;
        String kwd = null;
        boolean singleStatement = true;
        boolean continuation = false;
        DependencyText.Part openPart = null;
        StringBuilder typedProjects = null;
        groupItemsEnd = -1;
        
        while (true) {
            if (kPos < 0) {
                int e = pos;
                skipWhitespace();
                if (braceLevel == 0 && singleStatement && !continuation && wasEndOfLine()) {
                    if (dep != null) {
                        if (typedProjects != null) {
                            if (kwd != null) {
                                typedProjects.append(':').append(kwd);
                            }
                            dep.contents = typedProjects.toString();
                        }
                        dep.endPos = e;
                    }
                    return;
                }
                newLinePos = -1;
            }
            c = nextChar();
            
            if (kPos >= 0) {
                if (Character.isJavaIdentifierPart(c)) {
                    newLinePos = -1;
                    continue;
                }
                startKpos = kPos;
                kwd = contents.substring(kPos, lastPos);
                kPos = -1;
                
                if (openPart != null) {
                    openPart.value = kwd;
                    openPart.endPos = lastPos;
                    openPart = null;
                    kwd = null;
                }
            }
            
            if (braceLevel == 0 && singleStatement && !continuation && (c == '\n' || c == ';')) {
                if (dep != null) {
                    if (typedProjects != null) {
                        if (kwd != null) {
                            typedProjects.append(':').append(kwd);
                        }
                        dep.contents = typedProjects.toString();
                    }
                    dep.endPos = c == '\n' ? lastPos : pos;
                }
                return;
            }

            if (Character.isJavaIdentifierStart(c)) {
                kPos = lastPos;
                continue;
            }
            
            continuation = false;
            
            switch (c) {
                case '.':
                    if (braceLevel == 0) {
                        if (dep == null) {
                            if ("projects".equals(kwd)) {
                                dep = new DependencyText(container, startKpos);
                                dep.keyword = "project";
                                kwd = null;
                                typedProjects = new StringBuilder();
                            }
                        } else if (typedProjects != null) {
                            typedProjects.append(':').append(kwd);
                            kwd = null;
                        }
                    }
                    break;
                case '{':
                    skipNestedBlock('}');
                    if (braceLevel == 0) {
                        groupItemsEnd = pos;
                        return;
                    }
                    break;
                case '}':
                    if (braceLevel == 0) {
                        groupItemsEnd = pos;
                        return;
                    }
                    braceLevel--;
                    break;
                    
                case ',':
                    if (braceLevel == 0) {
                        continuation = true;
                    }
                    break;
                    
                case '(':
                    if (braceLevel > 0) {
                        skipNestedBlock(')');
                        break;
                    }
                    // list of dependencies, or arguments
                    if (kwd != null) {
                        if (dep == null) {
                            dep = new DependencyText(container, groupStartPos);
                        }
                        dep.keyword = kwd;
                        kwd = null;
                    } else if (dep == null) {
                        singleStatement = false;
                    }
                    break;
                    
                case ')':
                    if (braceLevel == 0) {
                        groupItemsEnd = pos;
                        singleStatement = true;
                        int x = skipWhitespace();
                        if (wasEndOfLine() || x == ';') {
                            if (dep != null) {
                                dep.endPos = newLinePos;
                                return;
                            }
                        }
                        if (x == '{') {
                            break;
                        } else {
                            if (dep != null) {
                                dep.endPos = groupItemsEnd;
                            }
                            return;
                        }
                    } 
                    braceLevel--;
                    if (braceLevel == 0 && dep != null) {
                        dep.endPos = pos;
                        finishDependency();
                        openPart = null;
                    }
                    break;
                    
                case '[':
                    if (braceLevel > 0) {
                        braceLevel++;
                        break;
                    }
                    // dependency as a Map
                    dep = new DependencyText(container, lastPos);
                    break;
                    
                case ']':
                    // end dependency Map
                    if (braceLevel == 0 && dep != null) {
                        dep.endPos = pos;
                        finishDependency();
                    } else {
                        braceLevel--;
                    }
                    break;
                    
                case ':':
                    if (braceLevel > 0) {
                        break;
                    }
                    // dependency part, keyed
                    int s;
                    if (dep == null) {
                        dep = new DependencyText(container, groupStartPos);
                        s = startKpos;
                    } else {
                        s = startKpos;
                    }
                    openPart = new DependencyText.Part();
                    openPart.startPos = startKpos;
                    openPart.partId = kwd;
                    dep.partList.add(openPart);
                    kwd = null;
                    break;
                    
                case '\'':
                case '"':
                    // single String dependency
                    if (dep == null) {
                        dep = new DependencyText(container, lastPos);
                    }
                    if (braceLevel > 0 && openPart == null) {
                        openPart = new DependencyText.Part();
                        openPart.startPos = lastPos;
                        dep.partList.add(openPart);
                    }
                    
                    String qval = readQuotedString(c);
                    String[] parts = BaseUtilities.parseParameters(qval);
                    String v = String.join(" ", parts);
                    if (!v.trim().isEmpty()) {
                        if (openPart != null) {
                            openPart.value = v;
                            openPart.endPos = pos;

                            openPart = null;
                        } else if (dep != null) {
                            dep.contents = qval;
                            dep.endPos = pos;
                            finishDependency();
                        }
                    }
                    break;
            }
        }
    }
    
    private void buildDependencies() {
        boolean onlyAfterNewline = false;
        int c;
        while ((c = nextToken()) != '}') {
            if ((!onlyAfterNewline || wasEndOfLine()) && (tokenType == Token.IDENTIFIER)) {
                if (configurationNames.contains(tokenText)) {
                    String saveToken = tokenText;
                    groupStartPos = tokenStart;
                    groupItemCount = 0;
                    scanDepdendencyContainer(tokenText);
                    finishDependency();
                    if (groupItemCount == 1) {
                        DependencyText last = dependencies.get(dependencies.size() - 1);
                        last.startPos = groupStartPos;
                        if (groupItemsEnd != -1) {
                            last.endPos = groupItemsEnd;
                        }
                    } else {
                        DependencyText.Part containerPart = new DependencyText.Part();
                        containerPart.partId = saveToken;
                        containerPart.startPos = groupStartPos;
                        containerPart.endPos = groupItemsEnd;
                        containerPart.quoted = 0;
                        List<DependencyText> items = new ArrayList<>(dependencies.subList(dependencies.size() - groupItemCount, dependencies.size()));
                        DependencyText.Container nc = new DependencyText.Container(items, containerPart);
                        items.forEach(i -> i.container = nc);
                    }
                    onlyAfterNewline = false;
                    continue;
                } else {
                    // ignore up to the semicolon or newline
                    onlyAfterNewline = true;
                }
            } else {
                if (c == ';') {
                    onlyAfterNewline = false;
                } else {
                    skipNestedBlocks(c);
                }
            }
        }
    }
    
    public TextDependencyScanner withConfigurations(Collection<String> configNames) {
        this.configurationNames.addAll(configNames);
        return this;
    }
    
    public List<DependencyText> parseDependencyList(String contents) {
        this.contents = contents;
        try {
            findDependencyBlock();
            buildDependencies();
            dependencyBlockEnd = pos;
            computeGAV();
        } catch (EndInputException ex) {
            // no op, just terminate processing
        }
        return dependencies;
    }
    
    private void skipNestedBlocks(int c) {
        switch (c) {
            case '"': case '\'':
                readQuotedString(c);
                break;
            case '(': 
                skipNestedBlock(')');
                break;
            case '{': 
                skipNestedBlock('}');
                break;
            case '[':
                skipNestedBlock(']');
                break;
        }
    }
    
    private int dependencyBlockStart = -1;
    private int dependencyBlockEnd;
    
    private void findDependencyBlock() {
        L: for (int i = 0; i < contents.length(); i++) {
            
            int c = nextToken();
            if (tokenType == Token.IDENTIFIER) {
                if ("dependencies".equals(tokenText)) {
                    dependencyBlockStart = tokenStart;
                    c = skipWhitespace();
                    if (c == '{') {
                        nextChar();
                        return;
                    }
                }
            } else if (tokenType != Token.QUOTED) {
                skipNestedBlocks(c);
            }
        }
    }
    
    private void computeGAV() {
        for (DependencyText text : dependencies) {
            if (text.contents != null) {
                String[] split = text.contents.split(":");
                text.group = split[0];
                text.name = split[1];
                if (split.length > 2) {
                    text.version = split[2];
                } else {
                    text.version = "";
                }
            } else {
                for (DependencyText.Part p : text.partList) {
                    switch (p.partId) {
                        case "group":
                            text.group = p.value;
                            break;
                        case "name":
                            text.name = p.value;
                            break;
                        case "version":
                            text.version = p.value;
                            break;
                    }
                }
                if (text.group == null || text.name == null) {
                    continue;
                }
            }
        }
    }
    
    private boolean scopeMatches(Dependency d, DependencyText t) {
        if (!matchScopes) {
            return true;
        }
        if (d.getScope() == null || t.configuration == null) {
            return true;
        }
        return d.getScope().name().equals(t.configuration);
    }
    
    private DependencyText findDependency(Dependency d) {
        String projectName = null;
        String gav = null;
        String groupAndName = null;
        if (d.getProject() != null) {
            projectName = d.getProject().getProjectId();
        } else {
            StringBuilder sb = new StringBuilder();
            ArtifactSpec as = d.getArtifact();
            if (as == null) {
                return null;
            }
            sb.append(as.getGroupId()).append(":").append(as.getArtifactId());
            groupAndName = sb.toString();
            if (as.getVersionSpec() != null) {
                sb.append(":").append(as.getVersionSpec());
            }
            gav = sb.toString();
        }
        for (DependencyText t : dependencies) {
            if (DependencyText.KEYWORD_PROJECT.equals(t.keyword) &&
                t.contents.equals(projectName)) {
                return t;
            } else if (t.keyword == null && t.getContentsOrGav().equals(gav) && scopeMatches(d, t)) {
                return t;
            }
        }
        
        for (DependencyText t : dependencies) {
            if (t.keyword == null && t.contents != null && t.contents.equals(groupAndName) && scopeMatches(d, t)) {
                return t;
            }
        }
        
        return null;
    }
    
    public DependencyText.Mapping mapDependencies(Collection<Dependency> rootDeps) {
        Map<Dependency, DependencyText> result = new HashMap<>();
        
        for (Dependency d : rootDeps) {
            DependencyText found = findDependency(d);
            if (found != null) {
                result.put(d, found);
            }
        }
        
        DependencyText.Part containerPart;
        
        if (dependencyBlockStart != -1) {
            containerPart = new DependencyText.Part();
            containerPart.partId = DependencyResult.PART_CONTAINER;
            containerPart.startPos = dependencyBlockStart;
            containerPart.endPos = dependencyBlockEnd;
            containerPart.value = "";
        } else {
            containerPart = null;
        }
        
        return new DependencyText.Mapping(result, containerPart);
    }
    
    /**
     * End of input reached.
     */
    private static final class EndInputException extends RuntimeException {}
}
