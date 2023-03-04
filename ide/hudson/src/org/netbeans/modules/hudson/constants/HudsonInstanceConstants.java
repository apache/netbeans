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

package org.netbeans.modules.hudson.constants;

/**
 *
 * @author marigan
 */
public class HudsonInstanceConstants {

    private HudsonInstanceConstants() {}
    
    public static final String INSTANCE_NAME = "name"; // NOI18N
    
    public static final String INSTANCE_URL = "url"; // NOI18N
    
    public static final String INSTANCE_SYNC = "sync_time"; // NOI18N

    /**
     * preferred jobs for the instance, list of job names, separated by |
     */
    public static final String INSTANCE_PREF_JOBS = "pref_jobs"; // NOI18N

    /**
     * Nonsalient jobs for the instance, list of job names, separated by |
     */
    public static final String INSTANCE_SUPPRESSED_JOBS = "suppressed_jobs"; // NOI18N

    public static final String INSTANCE_PERSISTED = "persisted";        //NOI18N

    /**
     * True value, e.g. for {@link #INSTANCE_PERSISTED} key.
     */
    public static final String TRUE = "true";                           //NOI18N
    /**
     * False value, e.g. for {@link #INSTANCE_PERSISTED} key.
     */
    public static final String FALSE = "false";                         //NOI18N
}
