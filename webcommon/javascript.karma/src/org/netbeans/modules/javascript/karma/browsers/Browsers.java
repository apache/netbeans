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

package org.netbeans.modules.javascript.karma.browsers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Browsers {

    private static final Logger LOGGER = Logger.getLogger(Browsers.class.getName());

    private static final Map<String, List<Browser>> BROWSERS;
    private static final List<String> SILENT_BROWSERS = Arrays.asList(
            "IE", // NOI18N
            "Safari", // NOI18N
            "PhantomJS" // NOI18N
    );

    static {
        List<Browser> allBrowsers = Arrays.asList(
                new ChromeBased(),
                new Firefox(),
                new OperaLegacy());
        BROWSERS = new ConcurrentHashMap<>();
        for (Browser browser : allBrowsers) {
            for (String identifier : browser.getIdentifiers()) {
                List<Browser> browsers = BROWSERS.get(identifier);
                if (browsers == null) {
                    browsers = new CopyOnWriteArrayList<>();
                    BROWSERS.put(identifier, browsers);
                }
                browsers.add(browser);
            }
        }
    }


    private Browsers() {
    }

    public static Collection<Browser> getBrowsers(String... identifiers) {
        return getBrowsers(Arrays.asList(identifiers));
    }

    public static Collection<Browser> getBrowsers(List<String> identifiers) {
        Set<Browser> result = new HashSet<>();
        for (String identifier : identifiers) {
            if (SILENT_BROWSERS.contains(identifier)) {
                continue;
            }
            List<Browser> browsers = BROWSERS.get(identifier);
            if (browsers == null) {
                LOGGER.log(Level.INFO, "Unknown karma browser: {0}", identifier);
                continue;
            }
            result.addAll(browsers);
        }
        return result;
    }

}
