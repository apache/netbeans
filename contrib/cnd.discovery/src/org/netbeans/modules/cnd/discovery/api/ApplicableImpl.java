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

package org.netbeans.modules.cnd.discovery.api;

import java.util.List;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Position;

/**
 *
 */
public final class ApplicableImpl implements Applicable {
    private final String compiler;
    private final boolean applicable;
    private final List<String> errors;
    private final int weight;
    private final boolean sunStudio;
    private final List<String> dependencies;
    private final List<String> searchPaths;
    private final String sourceRoot;
    private final Position position;

    public ApplicableImpl(boolean applicable, List<String> errors,
            String compiler, int weight, boolean sunStudio,
            List<String> dependencies, List<String> searchPaths, String sourceRoot, Position position) {
        this.compiler = compiler;
        this.applicable = applicable;
        this.errors = errors;
        this.weight = weight;
        this.sunStudio = sunStudio;
        this.dependencies = dependencies;
        this.searchPaths = searchPaths;
        this.sourceRoot = sourceRoot;
        this.position = position;
    }

    @Override
    public boolean isApplicable() {
        return applicable;
    }

    @Override
    public String getCompilerName() {
        return compiler;
    }

    @Override
    public int getPriority() {
        return weight;
    }

    @Override
    public boolean isSunStudio() {
        return sunStudio;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    @Override
    public List<String> getSearchPaths() {
        return searchPaths;
    }

    @Override
    public String getSourceRoot() {
        return sourceRoot;
    }

    @Override
    public Position getMainFunction() {
        return position;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }

    public static Applicable getNotApplicable(List<String> errors) {
        return new ApplicableImpl(false, errors, null, 0, false, null, null, null, null);
    }
}
