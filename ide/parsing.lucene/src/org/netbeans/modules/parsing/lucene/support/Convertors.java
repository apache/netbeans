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
