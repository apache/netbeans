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

package org.netbeans.modules.php.api.framework;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered PHP frameworks.
 * <p>
 * The path is "{@value #FRAMEWORK_PATH}" on SFS.
 * @author Tomas Mysik
 */
public final class PhpFrameworks {

    public static final String FRAMEWORK_PATH = "PHP/Frameworks"; //NOI18N

    private static final Lookup.Result<PhpFrameworkProvider> FRAMEWORKS = Lookups.forPath(FRAMEWORK_PATH).lookupResult(PhpFrameworkProvider.class);

    private PhpFrameworks() {
    }

    /**
     * Get all registered {@link PhpFrameworkProvider}s.
     * @return a list of all registered {@link PhpFrameworkProvider}s; never null.
     */
    public static List<PhpFrameworkProvider> getFrameworks() {
        return new ArrayList<>(FRAMEWORKS.allInstances());
    }

    /**
     * Add {@link LookupListener listener} to be notified when frameworks change
     * (new framework added, existing removed).
     * <p>
     * To avoid memory leaks, do not forget to {@link #removeFrameworksListener(LookupListener) remove} the listener.
     * @param listener {@link LookupListener listener} to be added
     * @see #removeFrameworksListener(LookupListener)
     */
    public static void addFrameworksListener(LookupListener listener) {
        Parameters.notNull("listener", listener);
        FRAMEWORKS.addLookupListener(listener);
    }

    /**
     * Remove {@link LookupListener listener}.
     * @param listener {@link LookupListener listener} to be removed
     * @see #addFrameworksListener(LookupListener)
     */
    public static void removeFrameworksListener(LookupListener listener) {
        Parameters.notNull("listener", listener);
        FRAMEWORKS.removeLookupListener(listener);
    }
}
