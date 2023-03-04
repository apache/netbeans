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

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ReplaceTokenProviderMerger implements ReplaceTokenProvider {

    final Lookup lookup;

    public ReplaceTokenProviderMerger(Lookup lookup) {
        this.lookup = lookup;
    }

    @LookupMerger.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
    public static class Merger implements LookupMerger<ReplaceTokenProvider> {

        @Override
        public Class<ReplaceTokenProvider> getMergeableClass() {
            return ReplaceTokenProvider.class;
        }

        @Override
        public ReplaceTokenProvider merge(Lookup lookup) {
            return new ReplaceTokenProviderMerger(lookup);
        }

    }

    @Override
    public Set<String> getSupportedTokens() {
        Set<String> ret = new HashSet<>();
        for (ReplaceTokenProvider pvd : lookup.lookupAll(ReplaceTokenProvider.class)) {
            ret.addAll(pvd.getSupportedTokens());
        }
        return ret;
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup context) {
        Map<String, String> ret = new HashMap<>();
        for (ReplaceTokenProvider pvd : lookup.lookupAll(ReplaceTokenProvider.class)) {
            if (pvd != this) {
                ret.putAll(pvd.createReplacements(action, context));
            }
        }
        for (ReplaceTokenProvider pvd : context.lookupAll(ReplaceTokenProvider.class)) {
            if (pvd != this) {
                ret.putAll(pvd.createReplacements(action, context));
            }
        }
        return ret;
    }

}
