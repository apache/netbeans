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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class NBLazyConstantTest {

    @Test
    public void testFactory() {

        Supplier<Boolean> lazy = NBLazyConstant.of(() -> true);
        assertNotNull(lazy);
        assertTrue(lazy.get());

        String impl;
        if (Runtime.version().feature() >= 26) {
            impl = "LazyConstant";
        } else if (Runtime.version().feature() == 25) {
            impl = "StableSupplier";
        } else {
            impl = "DoubleCheckedFallback";
        }
        assertTrue(impl + " expected but got " + lazy.getClass(), lazy.getClass().getSimpleName().contains(impl));
    }

    @Test
    public void testConstant() {
        Supplier<Double> lazy = NBLazyConstant.of(() -> Math.random());
        assertNotNull(lazy);
        assertEquals(lazy.get(), lazy.get());
        assertEquals(lazy.get(), lazy.get());
        assertEquals(lazy.get(), lazy.get());
    }

    @Test
    public void testRequireNPEOnNullResult() {

        // StableValue allows null, we use the LazyConstant spec
        assumeTrue(Runtime.version().feature() != 25);

        AtomicReference<Object> value = new AtomicReference<>();
        
        Supplier<Object> lazy = NBLazyConstant.of(() -> value.get());
        assertNotNull(lazy);

        try {
            lazy.get();
            fail();
        } catch (NullPointerException good) {}

        try {
            lazy.get();
            fail();
        } catch (NullPointerException stillGood) {}

        value.set("good");

        // constant can be computed from now on
        assertEquals("good", lazy.get());
        assertEquals("good", lazy.get());
    }

    @Test
    public void testExceptionDuringCompute() {

        AtomicBoolean fail = new AtomicBoolean(true);
        
        Supplier<Object> lazy = NBLazyConstant.of(() -> {
            if (fail.get()) {
                throw new RuntimeException("can't compute");
            } else {
                return "good";
            }
        });
        assertNotNull(lazy);

        try {
            lazy.get();
            fail();
        } catch (RuntimeException good) {}

        try {
            lazy.get();
            fail();
        } catch (RuntimeException stillGood) {}

        fail.set(false);

        // constant can be computed from now on
        assertEquals("good", lazy.get());
        assertEquals("good", lazy.get());
    }
    
}
