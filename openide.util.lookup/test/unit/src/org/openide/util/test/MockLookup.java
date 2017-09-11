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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide.util.test;

import java.lang.reflect.Field;
import java.util.Collection;
import static junit.framework.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Mock implementation of system default lookup suitable for use in unit tests.
 * The initial value just contains classpath services.
 */
public class MockLookup extends ProxyLookup {

    private static MockLookup DEFAULT;
    private static boolean making = false;
    private static volatile boolean ready;

    static {
        making = true;
        try {
            System.setProperty("org.openide.util.Lookup", MockLookup.class.getName());
            if (Lookup.getDefault().getClass() != MockLookup.class) {
                // Someone else initialized lookup first. Try to force our way.
                Field defaultLookup = Lookup.class.getDeclaredField("defaultLookup");
                defaultLookup.setAccessible(true);
                defaultLookup.set(null, null);
                Field defaultLookupProvider = Lookup.class.getDeclaredField("defaultLookupProvider");
                defaultLookupProvider.setAccessible(true);
                defaultLookupProvider.set(null, null);
            }
            assertEquals(MockLookup.class, Lookup.getDefault().getClass());
        } catch (Exception x) {
            throw new ExceptionInInitializerError(x);
        } finally {
            making = false;
        }
    }

    /** Do not call this directly! */
    public MockLookup() {
        assertTrue(making);
        assertNull(DEFAULT);
        DEFAULT = this;
    }

    /**
     * Just ensures that this lookup is default lookup, but does not actually change its content.
     * Useful mainly if you have some test utility method which calls foreign code which might use default lookup,
     * and you want to ensure that any users of mock lookup will see the correct default lookup right away,
     * even if they have not yet called {@link #setLookup} or {@link #setInstances}.
     */
    public static void init() {
        if (!ready) {
            setInstances();
        }
    }

    /**
     * Sets the global default lookup with zero or more delegate lookups.
     * Caution: if you don't include Lookups.metaInfServices, you may have trouble,
     * e.g. {@link #makeScratchDir} will not work.
     * Most of the time you should use {@link #setInstances} instead.
     */
    public static void setLookup(Lookup... lookups) {
        ready = true;
        DEFAULT.setLookups(lookups);
    }

    /**
     * Sets the global default lookup with some fixed instances.
     * Will also include (at a lower priority) a {@link ClassLoader},
     * and services found from <code>META-INF/services/*</code> in the classpath.
     */
    public static void setInstances(Object... instances) {
        ClassLoader l = MockLookup.class.getClassLoader();
        setLookup(Lookups.fixed(instances), Lookups.metaInfServices(l), Lookups.singleton(l));
    }
    /**
     * Sets the global default lookup with some fixed instances and
     * content read from Services folder from system file system.
     * Will also include (at a lower priority) a {@link ClassLoader},
     * and services found from <code>META-INF/services/*</code> in the classpath.
     */
    public static void setLayersAndInstances(Object... instances) {
        ClassLoader l = MockLookup.class.getClassLoader();
        if (l != Lookup.getDefault().lookup(ClassLoader.class)) {
            setLookup(Lookups.fixed(instances), Lookups.metaInfServices(l), Lookups.singleton(l));
        }
        Lookup projects = Lookups.forPath("Services");
        Collection<?> initialize = projects.lookupAll(Object.class);
        //System.err.println("all: " + initialize);
        setLookup(Lookups.fixed(instances), Lookups.metaInfServices(l), Lookups.singleton(l), projects);
    }

}
