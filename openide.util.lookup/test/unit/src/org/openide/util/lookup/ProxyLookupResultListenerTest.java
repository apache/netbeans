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
