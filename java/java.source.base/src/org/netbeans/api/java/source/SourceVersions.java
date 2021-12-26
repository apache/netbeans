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
package org.netbeans.api.java.source;

import javax.lang.model.SourceVersion;

/**
 * Utilities for dealing with {@link SourceVersion}s.
 * @author Michael Bien
 */
public final class SourceVersions {

    private SourceVersions() {}

    /**
     * Returns true if the feature version of {@link SourceVersion#latest()} equals or is greater than featureVersionToCheck.
     */
    public static boolean supports(int featureVersionToCheck) {
        return supports(SourceVersion.latest(), featureVersionToCheck);
    }

    /**
     * Returns true if the feature version of the given SourceVersion equals or is greater than featureVersionToCheck.
     */
    public static boolean supports(SourceVersion model, int featureVersionToCheck) {
        return featureOf(model) >= featureVersionToCheck;
    }

    /**
     * Returns the feature version of {@link SourceVersion#latest()}.
     * @see Runtime.Version#feature()
     */
    public static int feature() {
        return featureOf(SourceVersion.latest());
    }

    /**
     * Returns the feature version of the given SourceVersion.
     * @see Runtime.Version#feature() 
     */
    public static int featureOf(SourceVersion model) {
        // todo: in distant future we might be able to change this to model.runtimeVersion().feature();
        // see https://github.com/openjdk/jdk/pull/5973
        return model.ordinal();
    }

}
