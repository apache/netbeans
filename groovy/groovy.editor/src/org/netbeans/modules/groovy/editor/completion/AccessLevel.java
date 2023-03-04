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

package org.netbeans.modules.groovy.editor.completion;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;

/**
 *
 * @author Petr Hejl
 */
public enum AccessLevel {

    PUBLIC {
        @Override
        public ElementAcceptor getJavaAcceptor() {
            return new ElementAcceptor() {
                public boolean accept(Element e, TypeMirror type) {
                    return e.getModifiers().contains(Modifier.PUBLIC);
                }
            };
        }

        @Override
        public boolean accept(Set<org.netbeans.modules.csl.api.Modifier> modifiers) {
            return modifiers.contains(org.netbeans.modules.csl.api.Modifier.PUBLIC);
        }
    },

    PACKAGE {
        @Override
        public ElementAcceptor getJavaAcceptor() {
            return new ElementAcceptor() {
                public boolean accept(Element e, TypeMirror type) {
                    Set<Modifier> modifiers = e.getModifiers();
                    return !modifiers.contains(Modifier.PUBLIC)
                            && !modifiers.contains(Modifier.PROTECTED)
                            && !modifiers.contains(Modifier.PRIVATE);
                }
            };
        }

        @Override
        public boolean accept(Set<org.netbeans.modules.csl.api.Modifier> modifiers) {
            return !modifiers.contains(org.netbeans.modules.csl.api.Modifier.PRIVATE)
                    && !modifiers.contains(org.netbeans.modules.csl.api.Modifier.PROTECTED)
                    && !modifiers.contains(org.netbeans.modules.csl.api.Modifier.PUBLIC);
        }
    },

    PROTECTED {
        @Override
        public ElementAcceptor getJavaAcceptor() {
            return new ElementAcceptor() {
                public boolean accept(Element e, TypeMirror type) {
                    return e.getModifiers().contains(Modifier.PROTECTED);
                }
            };
        }

        @Override
        public boolean accept(Set<org.netbeans.modules.csl.api.Modifier> modifiers) {
            return modifiers.contains(org.netbeans.modules.csl.api.Modifier.PROTECTED);
        }
    },

    PRIVATE {
        @Override
        public ElementAcceptor getJavaAcceptor() {
            return new ElementAcceptor() {
                public boolean accept(Element e, TypeMirror type) {
                    return e.getModifiers().contains(Modifier.PRIVATE);
                }
            };
        }

        @Override
        public boolean accept(Set<org.netbeans.modules.csl.api.Modifier> modifiers) {
            return modifiers.contains(org.netbeans.modules.csl.api.Modifier.PRIVATE);
        }
    };

    public abstract ElementAcceptor getJavaAcceptor();

    public abstract boolean accept(Set<org.netbeans.modules.csl.api.Modifier> modifiers);

    public static Set<AccessLevel> create(ClassNode source, ClassNode type) {
        Set<AccessLevel> levels;

        if (source == null) {
            // may happen in GSP
            levels = EnumSet.of(AccessLevel.PUBLIC);
        } else if (type.equals(source)) {
            levels = EnumSet.allOf(AccessLevel.class);
        } else if (getPackageName(source).equals(getPackageName(type))) {
            levels = EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PACKAGE);
        } else if (source.getSuperClass() == null && type.getName().equals("java.lang.Object") // NOI18N
                || source.getSuperClass() != null && source.getSuperClass().getName().equals(type.getName())) {
            levels = EnumSet.complementOf(EnumSet.of(AccessLevel.PRIVATE));
        } else {
            levels = EnumSet.of(AccessLevel.PUBLIC);
        }

        return levels;
    }

    public static Set<AccessLevel> update(Set<AccessLevel> levels, ClassNode source, ClassNode type) {
        HashSet<AccessLevel> modifiedAccess = new HashSet<>(levels);
        // leav flag
        if (source == null || !type.equals(source)) {
            modifiedAccess.remove(AccessLevel.PRIVATE);
        }

        if (source == null || !getPackageName(source).equals(getPackageName(type))) {
            modifiedAccess.remove(AccessLevel.PACKAGE);
        } else {
            modifiedAccess.add(AccessLevel.PACKAGE);
        }

        return modifiedAccess;
    }

    private static String getPackageName(ClassNode node) {
        if (node.getPackageName() != null) {
            return node.getPackageName();
        }
        return ""; // NOI18N
    }
}
