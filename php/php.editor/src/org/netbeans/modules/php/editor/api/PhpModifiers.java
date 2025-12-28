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
package org.netbeans.modules.php.editor.api;

import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;

/**
 * @author Radek Matous
 */
public final class PhpModifiers extends Modifier {
    private int mod;
    public static final int NO_FLAGS = 0;
    public static final int ALL_FLAGS = 0xFFFFFFFF;

    private static final PhpModifiers EMPTY = new PhpModifiers(NO_FLAGS);

    // visibility
    public static final String VISIBILITY_VAR = "var"; // NOI18N
    public static final String VISIBILITY_PUBLIC = "public"; // NOI18N
    public static final String VISIBILITY_PRIVATE = "private"; // NOI18N
    public static final String VISIBILITY_PROTECTED = "protected"; // NOI18N
    public static final String VISIBILITY_PUBLIC_SET = "public(set)"; // NOI18N
    public static final String VISIBILITY_PRIVATE_SET = "private(set)"; // NOI18N
    public static final String VISIBILITY_PROTECTED_SET = "protected(set)"; // NOI18N

    public static final String STATIC_MODIFIER = "static"; // NOI18N
    public static final String FINAL_MODIFIER = "final"; // NOI18N
    public static final String ABSTRACT_MODIFIER = "abstract"; // NOI18N
    public static final String READONLY_MODIFIER = "readonly"; // NOI18N

    public static PhpModifiers noModifiers() {
        return fromBitMask(new int[]{});
    }

    // usage: bitMasks(Modifier.PUBLIC | Modifier.FINAL) or bitMasks(Modifier.PUBLIC, Modifier.FINAL)
    public static PhpModifiers fromBitMask(int... bitmasks) {
        return bitmasks.length == 0 ? EMPTY : new PhpModifiers(bitmasks);
    }

    public PhpModifiers setImplicitPublic() {
        mod |= Modifier.IMPLICIT_PUBLIC;
        return this;
    }

    public PhpModifiers setPublic() {
        mod |= Modifier.PUBLIC;
        return this;
    }

    public PhpModifiers setPrivate() {
        mod |= Modifier.PRIVATE;
        return this;
    }

    public PhpModifiers setProtected() {
        mod |= Modifier.PROTECTED;
        return this;
    }

    /**
     * Set public(set) to a modifier.
     *
     * @return {@link PhpModifiers}
     * @since 2.44.0
     */
    public PhpModifiers setPublicSet() {
        mod |= Modifier.PUBLIC_SET;
        return this;
    }

    /**
     * Set private(set) to a modifier.
     *
     * @return {@link PhpModifiers}
     * @since 2.44.0
     */
    public PhpModifiers setPrivateSet() {
        mod |= Modifier.PRIVATE_SET;
        return this;
    }

    /**
     * Set protected(set) to a modifier.
     *
     * @return {@link PhpModifiers}
     * @since 2.44.0
     */
    public PhpModifiers setProtectedSet() {
        mod |= Modifier.PROTECTED_SET;
        return this;
    }

    public PhpModifiers setStatic() {
        mod |= Modifier.STATIC;
        return this;
    }

    public PhpModifiers setFinal() {
        mod |= Modifier.FINAL;
        return this;
    }

    public PhpModifiers setAbstract() {
        mod |= Modifier.ABSTRACT;
        return this;
    }

    public PhpModifiers setReadonly() {
        mod |= Modifier.READONLY;
        return this;
    }

    private PhpModifiers(int... bitmask) {
        for (int mode : bitmask) {
            this.mod |= mode;
        }
        if (!Modifier.isPrivate(mod) && !Modifier.isProtected(mod) && !Modifier.isImplicitPublic(mod)) {
            mod |= Modifier.PUBLIC;
        }
    }

    public Set<org.netbeans.modules.csl.api.Modifier> toModifiers() {
        @SuppressWarnings("SetReplaceableByEnumSet")
        Set<org.netbeans.modules.csl.api.Modifier> retval = new LinkedHashSet<>();
        if ((isPublic() || isImplicitPublic()) && !isStatic()) {
            retval.add(org.netbeans.modules.csl.api.Modifier.PUBLIC);
        }
        if (isProtected()) {
            retval.add(org.netbeans.modules.csl.api.Modifier.PROTECTED);
        }
        if (isPrivate()) {
            retval.add(org.netbeans.modules.csl.api.Modifier.PRIVATE);
        }
        if (isStatic()) {
            retval.add(org.netbeans.modules.csl.api.Modifier.STATIC);
        }
        if (isAbstract()) {
            retval.add(org.netbeans.modules.csl.api.Modifier.ABSTRACT);
        }
        return retval;
    }

    public int toFlags() {
        return mod;
    }

    public boolean isImplicitPublic() {
        return Modifier.isImplicitPublic(mod);
    }

    public boolean isPublic() {
        return Modifier.isPublic(mod);
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(mod);
    }

    public boolean isProtected() {
        return Modifier.isProtected(mod);
    }

    /**
     * Check whether a modifier is public(set).
     *
     * @return {@code true} if a modifier is public(set), {@code false}
     * otherwise
     * @since 2.44.0
     */
    public boolean isPublicSet() {
        // public(set)
        return Modifier.isPublicSet(mod);
    }

    /**
     * Check whether a modifier is private(set).
     *
     * @return {@code true} if a modifier is private(set), {@code false}
     * otherwise
     * @since 2.44.0
     */
    public boolean isPrivateSet() {
        // private(set)
        return Modifier.isPrivateSet(mod);
    }

    /**
     * Check whether a modifier is protected(set).
     *
     * @return {@code true} if a modifier is protected(set), {@code false}
     * otherwise
     * @since 2.44.0
     */
    public boolean isProtectedSet() {
        // protected(set)
        return Modifier.isProtectedSet(mod);
    }

    public boolean isStatic() {
        return Modifier.isStatic(mod);
    }

    public boolean isFinal() {
        return Modifier.isFinal(mod);
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(mod);
    }

    public boolean isReadonly() {
        return Modifier.isReadonly(mod);
    }

    @Override
    public String toString() {
        return Modifier.toString(mod);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PhpModifiers) ? ((PhpModifiers) obj).mod == mod : false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + this.mod;
        return hash;
    }
}
