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
package org.netbeans.modules.javascript2.doc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.lookup.Lookups;

/**
 * This class should resolve JavaScript documentation types of files.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationResolver {

    private static final Logger LOG = Logger.getLogger(JsDocumentationResolver.class.getName());
    private static JsDocumentationResolver instance;

    private static final List<? extends JsDocumentationProvider> PROVIDERS = new ArrayList<JsDocumentationProvider>(
            Lookups.forPath(JsDocumentationSupport.DOCUMENTATION_PROVIDER_PATH).lookupResult(JsDocumentationProvider.class).allInstances());

    public static synchronized JsDocumentationResolver getDefault() {
        if (instance == null) {
            instance = new JsDocumentationResolver();
        }
        return instance;
    }

    /**
     * Finds and gets JsDocumentationProvider for given snapshot.
     * @param snapshot snapshot to be examined
     * @return provider, never {@code null}; as a fallback can return {@link JsDocumentationFallbackProvider}
     */
    public JsDocumentationProvider getDocumentationProvider(Snapshot snapshot) {
        return findBestMatchingProvider(snapshot);
    }

    private JsDocumentationProvider findBestMatchingProvider(Snapshot snapshot) {
        Set<String> allTags = JsDocumentationReader.getAllTags(snapshot);
        float max = -1.0f;
        JsDocumentationProvider bestProvider = null;
        for (JsDocumentationProvider jsDocumentationProvider : PROVIDERS) {
            float coverage = countTagsCoverageRatio(allTags, jsDocumentationProvider);
            if (coverage == 1.0) {
                return jsDocumentationProvider;
            } else {
                if (coverage > max) {
                    max = coverage;
                    bestProvider = jsDocumentationProvider;
                }
            }
        }
        return bestProvider != null ? bestProvider : new JsDocumentationFallbackProvider();
    }

    private float countTagsCoverageRatio(Set<String> tags, JsDocumentationProvider provider) {
        Set<String> unsupportedTags = new HashSet<String>(tags);
        unsupportedTags.removeAll(provider.getSupportedTags());
        if (unsupportedTags.isEmpty()) {
            return 1.0f;
        } else {
            float coverage = 1.0f - (1.0f / tags.size() * unsupportedTags.size());
            return coverage;
        }
    }
}
