/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.tooling.utils;

/**
 * Enumeration helper methods:<ul>
 * <li>Ordinal value based comparison.</li>
 * </ul>
 * @author Tomas Kraus
 */
public final class EnumUtils {
    
    /**
     * Ordinal value based comparison: <i>equals</i> {@code v1 == v2}.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         and {@code v2} are equal or both values are {@code null}.
     *         Value of {@code false} otherwise.
     */
    public static final boolean eq(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() == v2.ordinal() : false)
                : v2 == null;
    }

    /**
     * Ordinal value based comparison: <i>not equals</i> {@code v1 != v2}.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         and {@code v2} are not equal or one of the values
     *         is {@code null} and second one is not {@code null}. Value
     *         of {@code false} otherwise.
     */
    public static final boolean ne(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() != v2.ordinal() : true)
                : v2 != null;
    }

    /**
     * Ordinal value based comparison: <i>less than</i> {@code v1 < v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is less than {@code v2} or {@code v1} is {@code null} 
     *         and {@code v2} is not {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean lt(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() < v2.ordinal() : false)
                : v2 != null;
    }

    /**
     * Ordinal value based comparison: <i>less than or equal</i> {@code v1 <= v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is less than or equal to {@code v2} or {@code v1} is {@code null} 
     *         and {@code v2} is not {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean le(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() <= v2.ordinal() : false)
                : true;
    }

    /**
     * Ordinal value based comparison: <i>greater than</i> {@code v1 > v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is greater than {@code v2} or {@code v1} is {@code null} 
     *         and {@code v2} is not {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean gt(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() > v2.ordinal() : true)
                : false;
    }

    /**
     * Ordinal value based comparison: <i>greater than or equal</i> {@code v1 >= v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is greater than or equal to {@code v2} or {@code v1} is not {@code null} 
     *         and {@code v2} is {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean ge(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() >= v2.ordinal() : true)
                : v2 == null;
    }

}
