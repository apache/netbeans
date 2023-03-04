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
package org.netbeans.modules.testng.api;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author lukas
 */
public final class TestNGSupport {

    private static Lookup.Result<TestNGSupportImplementation> implementations;
    /** Cache of all available TestNGSupportImplementation instances. */
    private static List<TestNGSupportImplementation> cache;

    public static enum Action {
        CREATE_TEST,
        RUN_FAILED,
        RUN_TESTMETHOD,
        RUN_TESTSUITE,
        DEBUG_TEST,
        DEBUG_TESTMETHOD,
        DEBUG_TESTSUITE
    }

    private TestNGSupport() {
    }

    /**
     * Look for instance of TestNGSupportImplementation supporting given project
     * in the default lookup
     *
     * @param p
     * @return TestNGSupportImplementation instance for given project; null if
     *      there's not any
     */
    public static final TestNGSupportImplementation findTestNGSupport(Project p) {
        for (TestNGSupportImplementation s: getInstances()) {
            for (Action a : Action.values()) {
                if (s.isActionSupported(a, p)) {
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Check if at least one of TestNGSupportImplementation instances
     * registered in the default lookup supports given project
     *
     * @param p project
     *
     * @return true if at least one instance of TestNGSupportImplementation
     *      supporting given project is found, false otherwise
     */
    public static final boolean isActionSupported(Action action, Project p) {
        for (TestNGSupportImplementation s: getInstances()) {
            if (s.isActionSupported(action, p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if at least one of TestNGSupportImplementation instances
     * registered in the default lookup supports given project
     *
     * @param activatedFOs
     *
     * @return true if at least one instance of TestNGSupportImplementation
     *      supporting given project is found, false otherwise
     */
    public static final boolean isSupportEnabled(FileObject[] activatedFOs) {
        for (TestNGSupportImplementation s: getInstances()) {
            if (s.isSupportEnabled(activatedFOs)) {
                return true;
            }
        }
        return false;
    }

    private static synchronized List<TestNGSupportImplementation> getInstances() {
        if (implementations == null) {
            implementations = Lookup.getDefault().lookup(new Lookup.Template<TestNGSupportImplementation>(TestNGSupportImplementation.class));
            implementations.addLookupListener(new LookupListener() {

                public void resultChanged(LookupEvent ev) {
                    synchronized (TestNGSupport.class) {
                        cache = null;
                    }
                }
            });
        }
        if (cache == null) {
            cache = new ArrayList<TestNGSupportImplementation>(implementations.allInstances());
        }
        return cache;
    }
}
