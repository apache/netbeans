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
package org.netbeans.modules.gradle.loaders;

import org.netbeans.modules.gradle.api.NbGradleProject.Quality;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleLoadOptions {

    public static final GradleLoadOptions AIM_FALLBACK    = loadForQuality(Quality.FALLBACK);
    public static final GradleLoadOptions AIM_EVALUATED   = loadForQuality(Quality.EVALUATED);
    public static final GradleLoadOptions AIM_FULL        = loadForQuality(Quality.FULL);
    public static final GradleLoadOptions AIM_FULL_ONLINE = loadForQuality(Quality.FULL_ONLINE);

    public final String message;
    public final boolean ignoreCache;
    public final boolean interactive;
    public final boolean sync;
    public final boolean force;
    public final Quality aim;
    public final String[] args;

    private GradleLoadOptions(
            String message,
            boolean ignoreCache,
            boolean interactive,
            boolean sync,
            boolean force,
            Quality aim,
            String[] args) {
        this.message = message;
        this.ignoreCache = ignoreCache;
        this.interactive = interactive;
        this.sync = sync;
        this.force = force;
        this.aim = aim;
        this.args = args;
    }

    public GradleLoadOptions withMessage(String msg) {
        return new GradleLoadOptions( msg, ignoreCache, interactive, sync, force, aim, args);
    }

    public GradleLoadOptions ignoreCache() {
        return new GradleLoadOptions(message, true, interactive, sync, force, aim, args);
    }

    public GradleLoadOptions interactive() {
        return new GradleLoadOptions(message, ignoreCache, true, sync, force, aim, args);
    }

    public GradleLoadOptions sync() {
        return new GradleLoadOptions(message, ignoreCache, interactive, true, force, aim, args);
    }

    public GradleLoadOptions force() {
        return new GradleLoadOptions(message, ignoreCache, interactive, sync, true, aim, args);
    }

    public GradleLoadOptions withArgs(String... args) {
        return new GradleLoadOptions(message, ignoreCache, interactive, sync, force, aim, args);
    }

    public static GradleLoadOptions loadForQuality(Quality aim) {
        return new GradleLoadOptions("", false, false, false, false, aim, new String[0]);
    }
}
