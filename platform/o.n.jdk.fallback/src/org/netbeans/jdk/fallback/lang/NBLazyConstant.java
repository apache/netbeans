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
package org.netbeans.jdk.fallback.lang;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delegates to JDK's LazyConstant and provides a fallback implementation if not available.
 * 
 * The fallback implementation simulates behavior of the JDk 27 LazyConstant spec.
 * 
 * Internal API, may be removed when no longer needed.
 * 
 * @author mbien
 */
public final class NBLazyConstant {

    private static final MethodHandle lazyConstantFactory;

    static {
        Logger log = Logger.getLogger(NBLazyConstant.class.getName());
        MethodHandle mh = null;
        try {
            if (Boolean.getBoolean("nb.jdk.LazyConstant.usefallback")) {
                mh = null;
                log.log(Level.INFO, "using fallback");
            } else if (Runtime.version().feature() >= 26) {
                Class<?> entryPoint = Class.forName("java.lang.LazyConstant");
                mh = MethodHandles.lookup().findStatic(entryPoint, "of", MethodType.methodType(entryPoint, Supplier.class))
                                           .asType(MethodType.methodType(Supplier.class, Supplier.class));
            } else if (Runtime.version().feature() == 25) {
                Class<?> entryPoint = Class.forName("java.lang.StableValue");
                mh = MethodHandles.lookup().findStatic(entryPoint, "supplier", MethodType.methodType(Supplier.class, Supplier.class));
            }
            // dryrun - just to be sure
            if (mh != null) {
                Supplier<?> probe = () -> true;
                ((Supplier<?>)mh.invokeExact(probe)).get();
            }
        } catch (Throwable ex) {
            mh = null;
            log.log(Level.FINE, "using fallback", ex);
        }
        lazyConstantFactory = mh;
        log.log(Level.FINE, () -> "impl=" + String.valueOf(lazyConstantFactory));
    }

    private NBLazyConstant() {}

    /**
     * Create a {@link Supplier} for a lazily initializing constant.
     * @param computingFunction Factory to create the constant, only called once.
     * @return Returns the constant, never null.
     */
    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> of(Supplier<? extends T> computingFunction) {
        Objects.requireNonNull(computingFunction);
        if (lazyConstantFactory != null) {
            try {
                return (Supplier<T>) lazyConstantFactory.invokeExact(computingFunction);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable ex) {
                throw new RuntimeException(ex); // shouldn't be reachable under regular circumstances
            }
        } else {
            return new DoubleCheckedFallback<>(computingFunction);
        }
    }

    /// simmulates JDK 27 spec, 25 and 26 are more permissive
    /// and may allow null or error recovery
    private static class DoubleCheckedFallback<T> implements Supplier<T> {

        private volatile T constant;
        private Supplier<? extends T> factory;

        private DoubleCheckedFallback(Supplier<? extends T> factory) {
            this.factory = factory;
        }

        @Override
        public T get() {
            T c = constant;
            if (c == null) {
                synchronized (this) {
                    if (factory == null) { // error state due to past supplier failure
                        throw new NoSuchElementException("Unable to access the constant because an Exception was thrown at initial computation");
                    }
                    c = constant;
                    if (c == null) {
                        try {
                            c = factory.get();
                            Objects.requireNonNull(c);
                        } catch (Throwable t) {
                            factory = null;
                            throw new NoSuchElementException(t);
                        }
                        constant = c;
                    }
                }
            }
            return c;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{factory=" + factory + ", constant=" + constant + '}';
        }
    }
}
