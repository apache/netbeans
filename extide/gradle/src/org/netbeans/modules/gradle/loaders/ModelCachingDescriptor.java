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

import java.util.Set;
import org.gradle.tooling.model.Model;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.spi.loaders.GradlePluginProvider.GradleRuntime;

/**
 *
 * @author lkishalmi
 */
public interface ModelCachingDescriptor <T extends Model> {

    Class<T> getModelClass();

    Set<String> getTargets();

    GradleCommandLine gradleCommandLine(GradleRuntime rt);

    void onLoad(String target, T model);
    void onError(String target, Exception ex);

    boolean needsRefresh(String target);

}
