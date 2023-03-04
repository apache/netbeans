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
package org.netbeans.modules.glassfish.tooling.data.cloud;

/**
 * GlassFish Cloud User Account Entity.
 * <p/>
 * GlassFish Cloud User Account entity interface allows to use foreign
 * entity classes.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public interface GlassFishAccount {

    /**
     * Get GlassFish cloud user account name.
     * <p/>
     * This is display name given to the cluster.
     * <p/>
     * @return GlassFish cluster name.
     */
    public String getName();

    /**
     * Get GlassFish cloud account name.
     * <p/>
     * @return GlassFish cloud account name.
     */
    public String getAcount();

    /**
     * Get GlassFish cloud user name under account.
     * <p/>
     * @return GlassFish cloud user name under account.
     */
    public String getUserName();

    /**
     * Get GlassFish cloud URL.
     * <p/>
     * @return Cloud URL.
     */
    public String getUrl();

    /**
     * Get GlassFish cloud user password under account.
     * <p/>
     * @return GlassFish cloud user password under account.
     */
    public String getUserPassword();

    /**
     * Get GlassFish cloud entity reference.
     * <p/>
     * @return GlassFish cloud entity reference.
     */
    public GlassFishCloud getCloudEntity();

    }
