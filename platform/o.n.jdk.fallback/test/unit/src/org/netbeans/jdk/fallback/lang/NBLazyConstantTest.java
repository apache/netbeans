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

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class NBLazyConstantTest {

    // true if fallback or JDK 27 impl active
    private static final boolean JDK27_SPEC = Runtime.version().feature() >= 27 || Runtime.version().feature() < 25;

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

        // StableValue allows null, we use the LazyConstant JDK 27 spec
        // since its the least permissive
        assumeTrue("jdk 25 impl supports null", Runtime.version().feature() != 25);

        AtomicReference<Object> value = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        
        Supplier<Object> lazy = NBLazyConstant.of(() -> {
            calls.incrementAndGet();
            return value.get();
        });
        assertNotNull(lazy);

        try {
            lazy.get();
            fail();
        } catch (NoSuchElementException good) {
            assertTrue(JDK27_SPEC);
            assertEquals(NullPointerException.class, good.getCause().getClass());
        } catch (NullPointerException good) {
            assertFalse(JDK27_SPEC);
        }
        assertEquals(1, calls.get());

        try {
            lazy.get();
            fail();
        } catch (NoSuchElementException stillGood) {
            assertTrue(JDK27_SPEC);
            // no cause anymore
        } catch (NullPointerException stillGood) {
            assertFalse(JDK27_SPEC);
        }

        // JDK 25-26 spec has retries, we don't test those since fallback mimics JDK 27
        if (Runtime.version().feature() == 26) {
            return;
        }

        assertEquals(1, calls.get());

        value.set("good");

        // JDK 27+ spec -> no recovery from error state
        try {
            lazy.get();
            fail();
        } catch (NoSuchElementException stillGood) {
            assertTrue(JDK27_SPEC);
        } catch (NullPointerException stillGood) {
            assertFalse(JDK27_SPEC);
        }
        assertEquals(1, calls.get());

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
        } catch (NoSuchElementException good) {
            assertTrue(JDK27_SPEC);
            assertEquals(RuntimeException.class, good.getCause().getClass());
        } catch (RuntimeException good) {
            assertFalse(JDK27_SPEC);
            assertEquals(RuntimeException.class, good.getClass());
        }

        try {
            lazy.get();
            fail();
        } catch (NoSuchElementException good) {
            assertTrue(JDK27_SPEC);
            // no cause anymore
        } catch (RuntimeException good) {
            assertFalse(JDK27_SPEC);
            assertEquals(RuntimeException.class, good.getClass());
        }

        fail.set(false);

        // we don't test error recovery of 25 and 26
        if (!JDK27_SPEC) {
//            assertEquals("good", lazy.get());
//            assertEquals("good", lazy.get());
            return;
        }

        try {
            lazy.get();
            fail();
        } catch (RuntimeException stillInFailureState) {
            assertEquals(NoSuchElementException.class, stillInFailureState.getClass());
        }
        
    }
    
}
