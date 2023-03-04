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
package org.netbeans.api.intent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.netbeans.spi.intent.IntentHandlerRegistration;
import org.netbeans.spi.intent.Result;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class IntentTest {

    private static boolean handled = false;

    private static final String HANDLED_BY_TEST = "Greetings from Test";
    private static final String HANDLED_BY_NB = "Greetings from NB";
    private static final String HANDLED_BY_NB_PARAM
            = "Greetings from NB parametrized";
    private static final String HANDLED_BY_BROKEN = "";
    private static final String HANDLED_BY_BROKEN_SB = "";
    private static final String HANDLED_BY_SETBACK = "Greetings from setback";

    @Before
    public void setUp() {
        handled = false;
    }

    @Test
    public void testOpenUri() throws URISyntaxException, InterruptedException,
            ExecutionException {

        Future<Object> res3 = new Intent("TEST",
                new URI("scheme://x/y/z/")).execute();
        assertNotNull(res3.get());

        assertFalse(handled);
        Future<Object> res2 = new Intent(Intent.ACTION_VIEW,
                new URI("test://a/b/c/")).execute();
        Exception e = null;
        try {
            assertNotNull(res2.get());
        } catch (InterruptedException | ExecutionException ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void testSelectAppropriateHandler()
            throws URISyntaxException, InterruptedException,
            ExecutionException {

        Future<Object> x0 = new Intent("NONE",
                new URI("unsupported://resource")).execute();

        Exception e = null;
        try {
            x0.get();
        } catch (InterruptedException | ExecutionException ex) {
           e = ex;
        }
        assertTrue(e instanceof ExecutionException
                && (e.getCause() instanceof NoAvailableHandlerException));

        Future<Object> x1 = new Intent("NONE",
                new URI("broken://resource")).execute();
        try {
            x1.get();
        } catch (InterruptedException | ExecutionException ex) {
           e = ex;
        }
        assertTrue(e instanceof ExecutionException);

        Future<Object> x2 = new Intent("NONE",
                new URI("brokensb://resource")).execute();
        try {
            x2.get();
        } catch (InterruptedException | ExecutionException ex) {
           e = ex;
        }
        assertTrue(e instanceof ExecutionException);

        Future<Object> f0 = new Intent("TEST",
                new URI("unsupported://resource")).execute();
        assertEquals(HANDLED_BY_TEST, f0.get());

        Future<Object> f1 = new Intent(Intent.ACTION_VIEW,
                new URI("netbeans://resource")).execute();
        assertEquals(HANDLED_BY_NB, f1.get());

        Future<Object> f2 = new Intent(Intent.ACTION_VIEW,
                new URI("netbeans://resource?someParam=x")).execute();
        assertEquals(HANDLED_BY_NB, f2.get());

        Future<Object> f3 = new Intent(Intent.ACTION_VIEW,
                new URI("netbeans://resource?x=y&requiredParam=123")).execute();
        assertEquals(HANDLED_BY_NB_PARAM, f3.get());

        Future<Object> f4 = new Intent(Intent.ACTION_VIEW,
                new URI("setback://resource")).execute();
        assertEquals(HANDLED_BY_SETBACK, f4.get());
    }

    @Test
    public void testExecutionWithCallback() throws URISyntaxException, InterruptedException, ExecutionException {

        class CheckingCallback implements Callback {

            private final Semaphore s = new Semaphore(0);

            private Exception lastException = null;
            private Object lastResult = null;

            @Override
            public void success(Object result) {
                lastException = null;
                lastResult = result;
                s.release();
            }

            @Override
            public void failure(Exception exception) {
                lastException = exception;
                lastResult = null;
                s.release();
            }

            public void checkLastResult(Object expectedResult) {
                try {
                    s.acquire();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                assertEquals(expectedResult, lastResult);
            }

            public void checkLastFailure(Class<? extends Exception> ec) {
                try {
                    s.acquire();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                assertNotNull(lastException);
                assertEquals(ec, lastException.getClass());
            }
        }

        CheckingCallback cb = new CheckingCallback();

        new Intent("NONE", new URI("broken://resource")).execute(cb);
        cb.checkLastFailure(RuntimeException.class);

        new Intent("NONE", new URI("brokensb://resource")).execute(cb);
        cb.checkLastFailure(RuntimeException.class);

        new Intent("NONE", new URI("unsupported://resource")).execute(cb);
        cb.checkLastFailure(NoAvailableHandlerException.class);

        new Intent("TEST",
                new URI("unsupported://resource")).execute(cb);
        cb.checkLastResult(HANDLED_BY_TEST);

        new Intent(Intent.ACTION_VIEW,
                new URI("netbeans://resource")).execute(cb);
        cb.checkLastResult(HANDLED_BY_NB);

        new Intent(Intent.ACTION_VIEW,
                new URI("netbeans://resource?someParam=x")).execute(cb);
        cb.checkLastResult(HANDLED_BY_NB);

        new Intent(Intent.ACTION_VIEW,
                new URI("netbeans://resource?x=y&requiredParam=123")).execute(cb);
        cb.checkLastResult(HANDLED_BY_NB_PARAM);

        new Intent(Intent.ACTION_VIEW,
                new URI("setback://resource")).execute(cb);
        cb.checkLastResult(HANDLED_BY_SETBACK);
    }

    /**
     * Handler that claims to support all URI patterns, but that actually
     * accepts only scheme "test".
     *
     * @param intent
     * @return
     */
    @SuppressWarnings("PublicInnerClass")
    @IntentHandlerRegistration(
            displayName = "Test",
            position = 999,
            uriPattern = ".*",
            actions = "TEST")
    public static Object handleIntent(Intent intent) {
        return HANDLED_BY_TEST;
    }

    /**
     * Handler for URIs with scheme "netbeans".
     *
     * @param intent
     * @return
     */
    @SuppressWarnings("PublicInnerClass")
    @IntentHandlerRegistration(
            displayName = HANDLED_BY_NB,
            position = 998,
            uriPattern = "netbeans://.*",
            actions = "*")
    public static Object handleNetBeansIntent(Intent intent) {
        return HANDLED_BY_NB;
    }

    /**
     * Handler for URIs with scheme "netbeans" and parameter "requiredParam".
     *
     * @param intent
     * @return
     */
    @SuppressWarnings("PublicInnerClass")
    @IntentHandlerRegistration(
            displayName = HANDLED_BY_NB_PARAM,
            position = 997,
            uriPattern = "netbeans://.*[?&]requiredParam=.+",
            actions = {Intent.ACTION_VIEW, Intent.ACTION_EDIT})
    public static Object handleParametrizedNetBeansIntent(Intent intent) {
        return HANDLED_BY_NB_PARAM;
    }

    /**
     * Handler for URIs with scheme "broken".
     *
     * @param intent
     * @return
     */
    @SuppressWarnings("PublicInnerClass")
    @IntentHandlerRegistration(
            displayName = HANDLED_BY_BROKEN,
            position = 997,
            uriPattern = "broken://.*",
            actions = "*")
    public static Object handleBroken(Intent intent) {
        throw new RuntimeException("Intentionally broken");
    }

    /**
     * Handler for URIs with scheme "brokensb".
     *
     * @param intent
     * @param result
     */
    @SuppressWarnings("PublicInnerClass")
    @IntentHandlerRegistration(
            displayName = HANDLED_BY_BROKEN_SB,
            position = 997,
            uriPattern = "brokensb://.*",
            actions = "*")
    public static void handleBrokenSb(Intent intent, Result result) {
        result.setException(new RuntimeException("Intentionally broken"));
    }

    /**
     * Handler for URIs with scheme "setback".
     *
     * @param intent
     * @param result
     */
    @SuppressWarnings("PublicInnerClass")
    @IntentHandlerRegistration(
            displayName = HANDLED_BY_SETBACK,
            position = 997,
            uriPattern = "setback://.*",
            actions = "*")
    public static void handleSetBack(Intent intent, Result result) {
        result.setResult(HANDLED_BY_SETBACK);
    }
}
