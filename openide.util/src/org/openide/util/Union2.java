/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.util;

import java.io.Serializable;

/**
 * A union type which can contain one of two kinds of objects.
 * {@link Object#equals} and {@link Object#hashCode} treat this as a container,
 * not identical to the contained object, but the identity is based on the contained
 * object. The union is serialiable if its contained object is.
 * {@link Object#toString} delegates to the contained object.
 * @author Jesse Glick
 * @since org.openide.util 7.1
 */
public abstract class Union2<First,Second> implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    Union2() {}

    /**
     * Retrieve the union member of the first type.
     * @return the object of the first type
     * @throws IllegalArgumentException if the union really contains the second type
     */
    public abstract First first() throws IllegalArgumentException;

    /**
     * Retrieve the union member of the second type.
     * @return the object of the second type
     * @throws IllegalArgumentException if the union really contains the first type
     */
    public abstract Second second() throws IllegalArgumentException;

    /**
     * Check if the union contains the first type.
     * @return true if it contains the first type, false if it contains the second type
     */
    public abstract boolean hasFirst();

    /**
     * Check if the union contains the second type.
     * @return true if it contains the second type, false if it contains the first type
     */
    public abstract boolean hasSecond();

    @Override
    public abstract Union2<First,Second> clone();

    /**
     * Construct a union based on the first type.
     * @param first an object of the first type
     * @return a union containing that object
     */
    public static <First,Second> Union2<First,Second> createFirst(First first) {
        return new Union2First<First,Second>(first);
    }

    /**
     * Construct a union based on the second type.
     * @param second an object of the second type
     * @return a union containing that object
     */
    public static <First,Second> Union2<First,Second> createSecond(Second second) {
        return new Union2Second<First,Second>(second);
    }

    private static final class Union2First<First,Second> extends Union2<First,Second> {

        private static final long serialVersionUID = 1L;

        private final First first;

        public Union2First(First first) {
            this.first = first;
        }

        @Override
        public First first() throws IllegalArgumentException {
            return first;
        }

        @Override
        public Second second() throws IllegalArgumentException {
            throw new IllegalArgumentException();
        }

        @Override
        public boolean hasFirst() {
            return true;
        }

        @Override
        public boolean hasSecond() {
            return false;
        }

        @Override
        public String toString() {
            return String.valueOf(first);
        }

        @Override
        public boolean equals(Object obj) {
            return first != null ? (obj instanceof Union2First) && first.equals(((Union2First) obj).first) : obj == null;
        }

        @Override
        public int hashCode() {
            return first != null ? first.hashCode() : 0;
        }

        @Override
        public Union2<First,Second> clone() {
            return createFirst(first);
        }

    }

    private static final class Union2Second<First,Second> extends Union2<First,Second> {

        private static final long serialVersionUID = 1L;

        private final Second second;

        public Union2Second(Second second) {
            this.second = second;
        }

        @Override
        public First first() throws IllegalArgumentException {
            throw new IllegalArgumentException();
        }

        @Override
        public Second second() throws IllegalArgumentException {
            return second;
        }

        @Override
        public boolean hasFirst() {
            return false;
        }

        @Override
        public boolean hasSecond() {
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(second);
        }

        @Override
        public boolean equals(Object obj) {
            return second != null ? (obj instanceof Union2Second) && second.equals(((Union2Second) obj).second) : obj == null;
        }

        @Override
        public int hashCode() {
            return second != null ? second.hashCode() : 0;
        }

        @Override
        public Union2<First,Second> clone() {
            return createSecond(second);
        }

    }

}
