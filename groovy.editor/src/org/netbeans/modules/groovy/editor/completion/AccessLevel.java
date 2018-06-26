/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
