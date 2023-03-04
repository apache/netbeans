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

package org.openide.util.lookup;

import java.util.Arrays;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import static org.junit.Assert.*;

/**
 * @author MKlaehn
 */
@RunWith(Parameterized.class)
public class ProxyLookupResultListenerTest {

    private final List<Params> parameters;

    public ProxyLookupResultListenerTest(final List<Params> parameters) {
        this.parameters = parameters;
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        final List<Object[]> result = new ArrayList<Object[]>();

        // 0 to 4
        result.add(new Object[]{Arrays.asList(new Params(true, false, null))});
        result.add(new Object[]{Arrays.asList(new Params(true, false, null),
                    new Params(true, false, null))});
        result.add(new Object[]{Arrays.asList(new Params(true, false, null),
                    new Params(true, true, Boolean.TRUE))});
        result.add(new Object[]{Arrays.asList(new Params(true, false, null),
                    new Params(false, false, null))});
        result.add(new Object[]{Arrays.asList(new Params(true, false, null),
                    new Params(false, true, Boolean.TRUE))});

        // 5 to 9
        result.add(new Object[]{Arrays.asList(new Params(true, true, Boolean.TRUE))});
        result.add(new Object[]{Arrays.asList(new Params(true, true, Boolean.TRUE),
                    new Params(true, false, Boolean.FALSE))});
        result.add(new Object[]{Arrays.asList(new Params(true, true, Boolean.TRUE),
                    new Params(true, true, Boolean.TRUE))});
        result.add(new Object[]{Arrays.asList(new Params(true, true, Boolean.TRUE),
                    new Params(false, false, Boolean.FALSE))});
        result.add(new Object[]{Arrays.asList(new Params(true, true, Boolean.TRUE),
                    new Params(false, true, Boolean.TRUE))});
        // 10 to 14
        result.add(new Object[]{Arrays.asList(new Params(false, false, null))});
        result.add(new Object[]{Arrays.asList(new Params(false, false, null),
                    new Params(true, false, null))});
        result.add(new Object[]{Arrays.asList(new Params(false, false, null),
                    new Params(true, true, Boolean.TRUE))});
        result.add(new Object[]{Arrays.asList(new Params(false, false, null),
                    new Params(false, false, null))});
        result.add(new Object[]{Arrays.asList(new Params(false, false, null),
                    new Params(false, true, Boolean.TRUE))});
        // 15 to 19
        result.add(new Object[]{Arrays.asList(new Params(false, true, Boolean.TRUE))});
        result.add(new Object[]{Arrays.asList(new Params(false, true, Boolean.TRUE),
                    new Params(true, false, Boolean.FALSE))});
        result.add(new Object[]{Arrays.asList(new Params(false, true, Boolean.TRUE),
                    new Params(true, true, Boolean.TRUE))});
        result.add(new Object[]{Arrays.asList(new Params(false, true, Boolean.TRUE),
                    new Params(false, false, Boolean.FALSE))});
        result.add(new Object[]{Arrays.asList(new Params(false, true, Boolean.TRUE),
                    new Params(false, true, Boolean.TRUE))});
        return result;
    }

    @Test
    public void testListener() {
        final AtomicReference<Boolean> refContainsTarget = new AtomicReference<Boolean>(null);
        final AccessibleProxyLookup proxy = new AccessibleProxyLookup(Lookup.EMPTY);
        final Lookup.Result<Serializable> lookupResult = proxy.lookupResult(Serializable.class);

        lookupResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(final LookupEvent le) {
                refContainsTarget.set(!lookupResult.allInstances().isEmpty());
            }
        });

        for (int i = 0; i < parameters.size(); i++) {
            final Params params = parameters.get(i);

            if (params.checkSize) {
                proxy.lookupAll(Serializable.class).size();
            }

            proxy.changeLookups(createLookup(params.addTargetType));

            final StringBuilder sb = new StringBuilder("LookupListener set the value to something unexpected.\n");

            for (int idx = 0; idx < parameters.size(); idx++) {
                sb.append('[').append(idx).append("] = ").append(parameters.get(idx)).append('\n');
            }

            sb.append("Failed at [").append(i).append("]");

            assertEquals(sb.toString(), params.expected, refContainsTarget.get());
        }
    }

    private static Lookup createLookup(final boolean withTargetObject) {
        return Lookups.fixed(withTargetObject ? new Serializable() {
        } : new Object());
    }

    private static class AccessibleProxyLookup extends ProxyLookup {

        public AccessibleProxyLookup(final Lookup... lookups) {
            super(lookups);
        }

        public final void changeLookups(final Lookup... lookups) {
            setLookups(lookups);
        }

        @Override
        protected void beforeLookup(Template<?> template) {
            assertFalse("Don't hold lock on proxy", Thread.holdsLock(this));
        }
    }

    public static final class Params {

        private final boolean checkSize;
        private final boolean addTargetType;
        private final Boolean expected;

        public Params(final boolean checkSize, final boolean addTargetType, final Boolean expected) {
            this.checkSize = checkSize;
            this.addTargetType = addTargetType;
            this.expected = expected;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("Processing Params : {").
                    append("\n\tchecking contained size = ").
                    append(checkSize).
                    append("\n\tadding the target type = ").
                    append(addTargetType).
                    append("\n\texpected result = ").
                    append(String.valueOf(expected)).
                    append("\n}").
                    toString();
        }
    }
}
