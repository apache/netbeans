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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for class member declarations.
 */
public abstract class BodyDeclaration extends Statement implements Attributed {

    private int modifier;
    private final List<Attribute> attributes = new ArrayList<>();

    public BodyDeclaration(int start, int end, int modifier, boolean shouldComplete, List<Attribute> attributes) {
        super(start, end);
        this.modifier = (shouldComplete ? completeModifier(modifier) : modifier);
        this.attributes.addAll(attributes);
    }

    public BodyDeclaration(int start, int end, int modifier, boolean shouldComplete) {
        this(start, end, modifier, shouldComplete, Collections.emptyList());
    }

    public BodyDeclaration(int start, int end, int modifier) {
        this(start, end, modifier, false);
    }

    public String getModifierString() {
        return Modifier.toString(modifier);
    }

    public int getModifier() {
        return modifier;
    }

    /**
     * Get attributes.
     *
     * e.g.
     * <pre>
     * #[A1(1)] // attribute
     * pulic const CONSTANT = 'constant';
     *
     * #[A2(2)] // attribute
     * public $x;
     *
     * #[A3(3)] // attribute
     * public function fnc() {};
     * </pre>
     *
     * @return attributes
     */
    @Override
    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public boolean isAttributed() {
        return !attributes.isEmpty();
    }

    /**
     * Complets the modidifer to public if needed.
     *
     * @param mod
     */
    private static int completeModifier(int mod) {
        if (!BodyDeclaration.Modifier.isPrivate(mod) && !BodyDeclaration.Modifier.isProtected(mod)) {
            mod |= BodyDeclaration.Modifier.PUBLIC;
        }
        return mod;
    }

    /**
     * This is a utility for member modifiers.
     */
    public static class Modifier {

        /**
         * The <code>int</code> value representing the <code>public</code> modifier.
         */
        public static final int PUBLIC = 0x00000001;
        /**
         * The <code>int</code> value representing the <code>private</code> modifier.
         */
        public static final int PRIVATE = 0x00000002;
        /**
         * The <code>int</code> value representing the <code>protected</code> modifier.
         */
        public static final int PROTECTED = 0x00000004;
        /**
         * The <code>int</code> value representing the <code>static</code> modifier.
         */
        public static final int STATIC = 0x00000008;
        /**
         * The <code>int</code> value representing the <code>final</code> modifier.
         */
        public static final int FINAL = 0x00000010;
        /**
         * The <code>int</code> value representing the implicit <code>public</code> modifier.
         */
        public static final int IMPLICIT_PUBLIC = 0x00000020;
        /**
         * The <code>int</code> value representing the <code>readonly</code> modifier.
         */
        public static final int READONLY = 0x00000040;
        /**
         * The <code>int</code> value representing the <code>public(set)</code> modifier.
         */
        public static final int PUBLIC_SET = 0x00000080;
        /**
         * The <code>int</code> value representing the <code>private(set)</code> modifier.
         */
        public static final int PRIVATE_SET = 0x00000100;
        /**
         * The <code>int</code> value representing the <code>protected(set)</code> modifier.
         */
        public static final int PROTECTED_SET = 0x00000200;
        /**
         * The <code>int</code> value representing the <code>abstract</code> modifier.
         */
        public static final int ABSTRACT = 0x00000400;

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>public</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>public</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isPublic(int mod) {
            return (mod & PUBLIC) != 0 || (mod & IMPLICIT_PUBLIC) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * implicit <tt>public</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param 	mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * implicit <tt>public</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isImplicitPublic(int mod) {
            return (mod & IMPLICIT_PUBLIC) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>private</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>private</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isPrivate(int mod) {
            return (mod & PRIVATE) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>protected</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>protected</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isProtected(int mod) {
            return (mod & PROTECTED) != 0;
        }

        /**
         * Return {@code true} if the integer argument includes the
         * {@code public(set)} modifer, {@code false} otherwise.
         *
         * @param mod a set of modifers
         * @return {@code true} if {@code mod} includes the {@code public(set)}
         * modifier; {@code false} otherwise.
         */
        public static boolean isPublicSet(int mod) {
            return (mod & PUBLIC_SET) != 0;
        }

        /**
         * Return {@code true} if the integer argument includes the
         * {@code private(set)} modifer, {@code false} otherwise.
         *
         * @param mod a set of modifers
         * @return {@code true} if {@code mod} includes the {@code private(set)}
         * modifier; {@code false} otherwise.
         */
        public static boolean isPrivateSet(int mod) {
            return (mod & PRIVATE_SET) != 0;
        }

        /**
         * Return {@code true} if the integer argument includes the
         * {@code protected(set)} modifer, {@code false} otherwise.
         *
         * @param mod a set of modifers
         * @return {@code true} if {@code mod} includes the
         * {@code protected(set)} modifier; {@code false} otherwise.
         */
        public static boolean isProtectedSet(int mod) {
            return (mod & PROTECTED_SET) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>static</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>static</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isStatic(int mod) {
            return (mod & STATIC) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>final</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>final</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isFinal(int mod) {
            return (mod & FINAL) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>abstract</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>abstract</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isAbstract(int mod) {
            return (mod & ABSTRACT) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>readonly</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>readonly</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isReadonly(int mod) {
            return (mod & READONLY) != 0;
        }

        /**
         * Check whether the modifier is a visibility modifier(public,
         * protected, or private).
         *
         * @param modifier the modifier
         * @return {@code true} it's a visibility modifier, otherwise
         * {@code false}
         */
        public static boolean isVisibilityModifier(int modifier) {
            return isPublic(modifier)
                    || isProtected(modifier)
                    || isPrivate(modifier);
        }

        /**
         * Check whether the modifier is a set visibility modifier(public(set),
         * protected(set), or private(set)).
         *
         * @param modifier the modifier
         * @return {@code true} it's a set visibility modifier, otherwise
         * {@code false}
         */
        public static boolean isSetVisibilityModifier(int modifier) {
            return isPublicSet(modifier)
                    || isProtectedSet(modifier)
                    || isPrivateSet(modifier);
        }

        public static String toString(int mod) {
            StringBuilder sb = new StringBuilder();

            if ((mod & PUBLIC) != 0 || (mod & IMPLICIT_PUBLIC) != 0) {
                sb.append("public "); //$NON-NLS-1$
            }
            if ((mod & PROTECTED) != 0) {
                sb.append("protected "); //$NON-NLS-1$
            }
            if ((mod & PRIVATE) != 0) {
                sb.append("private "); //$NON-NLS-1$
            }
            if ((mod & PUBLIC_SET) != 0) {
                sb.append("public(set) "); // NOI18N
            }
            if ((mod & PROTECTED_SET) != 0) {
                sb.append("protected(set) "); // NOI18N
            }
            if ((mod & PRIVATE_SET) != 0) {
                sb.append("private(set) "); // NOI18N
            }

            if ((mod & READONLY) != 0) {
                sb.append("readonly "); // NOI18N
            }

            //Canonical order
            if ((mod & ABSTRACT) != 0) {
                sb.append("abstract "); //$NON-NLS-1$
            }
            if ((mod & STATIC) != 0) {
                sb.append("static "); //$NON-NLS-1$
            }
            if ((mod & FINAL) != 0) {
                sb.append("final "); //$NON-NLS-1$
            }

            int len;
            if ((len = sb.length()) > 0) { /* trim trailing space */
                return sb.toString().substring(0, len - 1);
            }
            return ""; //$NON-NLS-1$
        }
    }
}
