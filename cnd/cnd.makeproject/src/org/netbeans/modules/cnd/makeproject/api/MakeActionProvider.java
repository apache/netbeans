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
package org.netbeans.modules.cnd.makeproject.api;

import org.netbeans.spi.project.ActionProvider;

/**
 *
 */
public interface MakeActionProvider extends ActionProvider {

    // Commands available from Make project
    public static final String COMMAND_BATCH_BUILD = "batch_build"; // NOI18N
    public static final String COMMAND_PRE_BUILD = "pre_build"; // NOI18N
    public static final String COMMAND_BUILD_PACKAGE = "build_packages"; // NOI18N
    public static final String COMMAND_CUSTOM_ACTION = "custom.action"; // NOI18N
    public static final String COMMAND_DEBUG_TEST = "debug.test"; // NOI18N
    public static final String COMMAND_DEBUG_STEP_INTO_TEST = "debug.stepinto.test"; // NOI18N
    
}
