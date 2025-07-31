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
package org.netbeans.modules.gsf.testrunner.ui.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestMethodFinderImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * API to provide a list of {@link TestMethod}s found in sources under particular test roots.
 *
 * @author Dusan Balek
 * @since 1.27
 */
public final class TestMethodFinder {

    private static final List<BiConsumer<FileObject, Collection<TestMethodController.TestMethod>>> listeners = new ArrayList<>();
    private static final Set<TestMethodFinderImplementation> listenersAdded = Collections.newSetFromMap(new WeakHashMap<>());
    private static final BiConsumer<FileObject, Collection<TestMethodController.TestMethod>> primaryListener = (file, tests) -> {
        List<BiConsumer<FileObject, Collection<TestMethodController.TestMethod>>> listenersCopy;

        synchronized (TestMethodFinder.class) {
            listenersCopy = new ArrayList<>(listeners);
        }

        for (BiConsumer<FileObject, Collection<TestMethodController.TestMethod>> listener : listenersCopy) {
            listener.accept(file, tests);
        }
    };

    public static synchronized void addListener(BiConsumer<FileObject, Collection<TestMethodController.TestMethod>> listener) {
        listeners.add(listener);
    }

    /**
     * Provides a list of {@link TestMethod}s found in sources under particular test roots.
     *
     * @param testRoot root to search test methods for
     * @return map of test source files to tests methods found
     * @since 1.27
     */
    public static Map<FileObject, Collection<TestMethodController.TestMethod>> findTestMethods(FileObject testRoot) {
        for (TestMethodFinderImplementation impl : Lookup.getDefault().lookupAll(TestMethodFinderImplementation.class)) {
            Map<FileObject, Collection<TestMethodController.TestMethod>> result = impl.findTestMethods(testRoot);

            if (result != null) {
                synchronized (TestMethodFinder.class) {
                    if (listenersAdded.add(impl)) {
                        impl.addListener(primaryListener); //TODO: weak?
                    }
                }
                return result;
            }
        }
        return Collections.emptyMap();
    }

}
