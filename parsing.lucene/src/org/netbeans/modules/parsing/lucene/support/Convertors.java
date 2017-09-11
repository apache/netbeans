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
package org.netbeans.modules.parsing.lucene.support;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 * {@link Convertor} utilities.
 * @author Tomas Zezula
 * @since 2.24
 */
public final class Convertors {
    
    private static final Convertor<?,?> IDENTITY =
            new Convertor<Object, Object>() {
                @Override
                @CheckForNull
                public Object convert(@NullAllowed final Object p) {
                    return p;
                }
            };

    private Convertors() {}

    /**
     * Identity convertor.
     * Returns the identity function, function returning its parameter.
     * @param <T> type of the both parameter and result.
     * @return returns identity convertor.
     */
    @SuppressWarnings("unchecked")
    public static <T> Convertor<T,T> identity() {
        return (Convertor<T,T>) IDENTITY;
    }

    /**
     * Composite convertor.
     * Returns the composition of two functions F(p)->second(first(p)).
     * @param <P> the parameter type of the first {@link Convertor}
     * @param <I> the result type of the first {@link Convertor}
     * @param <R> the result type of the second {@link Convertor}
     * @param first the first {@link Convertor}
     * @param second the second {@link Convertor}
     * @return the composite {@link Convertor}
     */
    public static <P,I,R> Convertor <P,R> compose(
            @NonNull final Convertor<? super P, ? extends I> first,
            @NonNull final Convertor<? super I, ? extends R> second) {
        Parameters.notNull("first", first); //NOI18N
        Parameters.notNull("second", second); //NOI18N
        return new CompositeConvertor<P,I,R> (first, second);
    }

    /**
     * Returns a {@link Convertor} returning the first non null result of its delegates.
     * @param convertors the delegates
     * @return the {@link Convertor}
     * @since 2.33
     */
    @NonNull
    public static <P,R> Convertor<P,R> firstNonNull(@NonNull Iterable<? extends Convertor<? super P, ? extends R>> convertors) {
        Parameters.notNull("convertors", convertors);   //NOI18N
        return new FirstNonNull<>(convertors);
    }


    private static final class CompositeConvertor<P,I,R> implements Convertor<P, R> {

        private final Convertor<? super P, ? extends I> first;
        private final Convertor<? super I, ? extends R> second;

        CompositeConvertor(
            @NonNull final Convertor<? super P, ? extends I> first,
            @NonNull final Convertor<? super I, ? extends R> second) {
            this.first = first;
            this.second = second;
        }


        @Override
        @CheckForNull
        public R convert(@NullAllowed P p) {
            return second.convert(first.convert(p));
        }

    }

    private static final class FirstNonNull<P,R> implements Convertor<P,R> {

        private final Iterable<? extends Convertor<? super P, ? extends R>> delegates;

        FirstNonNull(Iterable<? extends Convertor<? super P, ? extends R>> delegates) {
            this.delegates = delegates;
        }

        @Override
        public R convert(P p) {
            for (Convertor<? super P, ? extends R> c : delegates) {
                final R r = c.convert(p);
                if (r != null) {
                    return r;
                }
            }
            return null;
        }
    }

}
