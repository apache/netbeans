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
package org.netbeans.modules.cordova.platforms.spi;

import org.netbeans.api.project.Project;
import org.openide.execution.ExecutorTask;

/**
 * Performs ant build. Implementations to be registered in global lookup.
 * @author Jan Becicka
 */
public interface BuildPerformer {
    public static final String BUILD_ANDROID = "build-android"; //NOI18N
    public static final String BUILD_IOS = "build-ios"; //NOI18N
    public static final String CLEAN_ANDROID = "clean-android"; //NOI18N
    public static final String CLEAN_IOS = "clean-ios"; //NOI18N
    public static final String RUN_ANDROID = "sim-android"; //NOI18N
    public static final String RUN_IOS = "sim-ios"; //NOI18N
    public static final String REBUILD_ANDROID = "rebuild-android"; //NOI18N
    public static final String REBUILD_IOS = "rebuild-ios"; //NOI18N
    
    public ExecutorTask perform(String target, Project p);
    
}
