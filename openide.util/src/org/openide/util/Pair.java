/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.openide.util;

/**
 * A type safe pair of two object.
 * @author Tomas Zezula
 * @since 8.32
 */
public final class Pair<First,Second> {

    private final First first;
    private final Second second;


    private Pair(final First first, final Second second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first element of the {@link Pair}.
     * @return the first element.
     */
    public First first() {
        return first;
    }

    /**
     * Returns the second element of the {@link Pair}.
     * @return the second element.
     */
    public Second second() {
        return second;
    }

    @Override
    public String toString () {
        return String.format("Pair[%s,%s]", first,second);  //NOI18N
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Pair)) {
            return false;
        }
        final Pair<?,?> otherPair = (Pair<?,?>) other;
        return (first == null ? otherPair.first == null : first.equals(otherPair.first)) &&
            (second == null ? otherPair.second == null : second.equals(otherPair.second));
    }

    @Override
    public int hashCode() {
        int res = 17;
        res = res * 31 + (first == null ? 0 : first.hashCode());
        res = res * 31 + (second == null ? 0 : second.hashCode());
        return res;
    }


    /**
     * Creates a new Pair.
     * @param <First>   the type of the first element
     * @param <Second>  the type of the second element
     * @param first     the first element
     * @param second    the second element
     * @return  the new {@link Pair} of the first and second elements.
     */
    public static <First,Second> Pair<First,Second> of (final First first, final Second second) {
        return new Pair<First, Second>(first, second);
    }
}
