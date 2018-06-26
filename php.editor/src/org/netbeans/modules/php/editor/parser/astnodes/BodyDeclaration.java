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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Base class for class member declarations
 */
public abstract class BodyDeclaration extends Statement {

    private int modifier;

    public BodyDeclaration(int start, int end, int modifier, boolean shouldComplete) {
        super(start, end);

        this.modifier = (shouldComplete ? completeModifier(modifier) : modifier);
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
     * Complets the modidifer to public if needed
     * @param mod
     */
    private static int completeModifier(int mod) {
        if (!BodyDeclaration.Modifier.isPrivate(mod) && !BodyDeclaration.Modifier.isProtected(mod)) {
            mod |= BodyDeclaration.Modifier.PUBLIC;
        }
        return mod;
    }

    /**
     * This is a utility for member modifiers
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
         * The <code>int</code> value representing the <code>abstract</code> modifier.
         */
        public static final int ABSTRACT = 0x00000400;

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>public</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param 	mod a set of modifers
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
         * @param 	mod a set of modifers
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
         * @param 	mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>protected</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isProtected(int mod) {
            return (mod & PROTECTED) != 0;
        }

        /**
         * Return <tt>true</tt> if the integer argument includes the
         * <tt>static</tt> modifer, <tt>false</tt> otherwise.
         *
         * @param 	mod a set of modifers
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
         * @param 	mod a set of modifers
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
         * @param 	mod a set of modifers
         * @return <tt>true</tt> if <code>mod</code> includes the
         * <tt>abstract</tt> modifier; <tt>false</tt> otherwise.
         */
        public static boolean isAbstract(int mod) {
            return (mod & ABSTRACT) != 0;
        }

        public static String toString(int mod) {
            StringBuffer sb = new StringBuffer();

            if ((mod & PUBLIC) != 0 || (mod & IMPLICIT_PUBLIC) != 0) {
                sb.append("public "); //$NON-NLS-1$
            }
            if ((mod & PROTECTED) != 0) {
                sb.append("protected "); //$NON-NLS-1$
            }
            if ((mod & PRIVATE) != 0) {
                sb.append("private "); //$NON-NLS-1$
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
