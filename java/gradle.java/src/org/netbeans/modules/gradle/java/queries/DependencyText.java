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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;

/**
 * Single dependency information.
 */
public class DependencyText {

    /**
     * True if the dependency is a single statement, false if declared in
     * a container block.
     */
    boolean single;
    /**
     * Container / target configuration name
     */
    String configuration;
    /**
     * Custom API call or "project" for project dependencies.
     */
    String keyword;
    boolean custom;
    /**
     * Starting position of this dependency. For {@link #single single=true}, the start
     * of the configuration name. Otherwise the first character of the string or
     * first char of the map key identifier.
     */
    int startPos;
    /**
     * End position, exclusive. After last quote, parenthesis.
     */
    int endPos;
    /**
     * If the dependency is defined as a map or sequence of Strings, contains
     * individual parts.
     */
    List<Part> partList = new ArrayList<>();
    
    /**
     * Textual contents of the dependency, if defined as a simple String. May be
     * computed from parts.
     */
    String contents;
    
    /**
     * GAV coordinates for dependency matching. In the future, the variable references may be
     * interpolated to their values.
     */
    String group;
    String name;
    String version;
    
    Style style;
    
    Container container;

    public DependencyText(String container, int startPos) {
        this.configuration = container;
        this.startPos = startPos;
    }
    
    public String getProjectPath() {
        return KEYWORD_PROJECT.equals(keyword) ? contents : null;
    }
    static final String KEYWORD_PROJECT = "project";

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(configuration).append(" += ");
        if (keyword != null) {
            sb.append(keyword).append("(");
            if (!partList.isEmpty()) {
                sb.append(partList.stream().map(p -> p.value).collect(Collectors.joining(", ")));
            } else if (contents != null) {
                sb.append(contents);
            }
            sb.append(")");
        } else {
            if (contents != null) {
                sb.append(contents);
            } else {
                sb.append(partList.stream().map(p -> p.toString()).collect(Collectors.joining(", ")));
            }
        }
        sb.append(" [").append(startPos).append(", ").append(endPos).append("]}");
        return sb.toString();
    }
    
    public String getContentsOrGav() {
        if (contents != null) {
            return contents;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(group).append(':').append(name);
            if (version != null && !version.isEmpty()) {
                sb.append(':').append(version);
            }
            return sb.toString();
        }
    }
    
    public static enum Style {
        /**
         * Specified as attribtute: value list
         */
        MAP_LITERAL,
        
        /**
         * Followed by {} customization block
         */
        CUSTOMIZED,
        
        /**
         * Specified as single string,
         */
        GAV_STRING,
        
        /**
         * Item in a multi-valued configuration container
         */
        CONTAINER_ITEM,
        /**
         * Surrounded by brackets
         */
        BRACKETS,
        
        /**
         * Surrounded by parenthesis
         */
        PARENTHESIS
    }

    /**
     * Dependency part information
     */
    public static final class Part {

        /**
         * Id of the part
         */
        String partId;
        /**
         * Starting position. If the part is in form key: value, the first character of the key.
         */
        int startPos;
        /**
         * Part value, interpreted
         */
        String value;
        /**
         * Ending position, exclusive.
         */
        int endPos;
        /**
         * True, if the part is quoted.
         */
        int quoted;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (partId != null) {
                sb.append(partId).append(": ");
            }
            sb.append(value);
            sb.append(" [").append(startPos).append(", ").append(endPos).append("]");
            return sb.toString();
        }
    }
    
    public final static class Container {
        final List<DependencyText> items;
        final DependencyText.Part containerPart;

        public Container(List<DependencyText> items, Part containerPart) {
            this.items = items;
            this.containerPart = containerPart;
        }
    }
    
    
    public final static class Mapping {
        private final Map<Dependency, DependencyText> textMapping;
        /**
         * Container for all dependencies.
         */
        private final DependencyText.Part container;

        public Mapping(Map<Dependency, DependencyText> textMapping, Part container) {
            this.textMapping = textMapping;
            this.container = container;
        }

        public DependencyText.Part getText(Dependency d, String part) {
            if (d == null && DependencyResult.PART_CONTAINER.equals(part)) {
                return container;
            }
            DependencyText t = textMapping.get(d);
            if (t == null) {
                return null;
            }
            if (part == null) {
                DependencyText.Part p = new DependencyText.Part();
                p.startPos = t.startPos;
                p.endPos = t.endPos;
                p.value = t.contents;
                return p;
            }
            if (DependencyResult.PART_CONTAINER.equals(part)) {
                if (t.container != null) {
                    return t.container.containerPart;
                } else {
                    return null;
                }
            }
            for (DependencyText.Part p : t.partList) {
                if (part.equals(p.partId)) {
                    return p;
                }
            }
            return null;
        }
    }
}
