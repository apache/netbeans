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
package org.netbeans.modules.java.j2sedeploy.api;

/**
 *
 * @author Petr Somol
 * @author Tomas Zezula
 */
public final class J2SEDeployConstants {
    
    // Deploy panel component properties (to transfer FX listeners from FX project)
    public static final String PASS_OK_LISTENER = "pass.OK.listener"; // NOI18N
    public static final String PASS_STORE_LISTENER = "pass.Store.listener"; // NOI18N
    public static final String PASS_CLOSE_LISTENER = "pass.Close.listener"; // NOI18N

    private J2SEDeployConstants() {
        throw new IllegalStateException();
    }
}
